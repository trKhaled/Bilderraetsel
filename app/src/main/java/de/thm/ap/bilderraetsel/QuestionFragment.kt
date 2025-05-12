package de.thm.ap.bilderraetsel

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import de.thm.ap.bilderraetsel.model.AllTimeStats
import de.thm.ap.bilderraetsel.model.Question
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

/**
 * A simple [Fragment] subclass for question view
 * Use the [QuestionFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class QuestionFragment : Fragment(), View.OnClickListener {

    private var TAG = "READ_QUESTION_DATA"
    private lateinit var firebaseFirestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private var moduleId: String = ""
    private var questionList: MutableList<Question> = ArrayList()
    private var selectedQuestions: MutableList<Question> = ArrayList()
    private var buttonList: MutableList<Button> = ArrayList()
    private lateinit var questionTitle: TextView
    private lateinit var streakCounter: TextView
    private lateinit var questionImage: ImageView
    private lateinit var timer: TextView
    private lateinit var optionA: Button
    private lateinit var optionB: Button
    private lateinit var optionC: Button
    private lateinit var optionD: Button
    private lateinit var nextBtn: Button
    private lateinit var countDown: CountDownTimer
    private lateinit var progressBar: ProgressBar
    private var allowAnswer = false
    private var position = 0
    private var correctAnswer = 0
    private var wrongAnswer = 0
    private var didNotAnswer = 0
    private var userId: String = ""
    private var score = 0
    private lateinit var navController: NavController
    private lateinit var viewModel: ModuleViewModel
    private val CHANNEL_ID = "notification_channel"
    private val notificationId = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createNotificationChannel()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_question, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        firebaseFirestore = FirebaseFirestore.getInstance()
        navController = Navigation.findNavController(view)
        viewModel = ViewModelProvider(this)[ModuleViewModel::class.java]
        auth = FirebaseAuth.getInstance()
        if (auth.currentUser != null) {
            userId = auth.currentUser!!.uid
        } else {
            navController.navigate(R.id.moduleFragment)
        }
        // Get module id
        moduleId = QuestionFragmentArgs.fromBundle(requireArguments()).questionId

        // Bind data
        questionTitle = view.findViewById(R.id.question_title)
        streakCounter = view.findViewById(R.id.streak_counter)
        questionImage = view.findViewById(R.id.question_image_form)
        timer = view.findViewById(R.id.timer)
        optionA = view.findViewById(R.id.optionA)
        optionB = view.findViewById(R.id.optionB)
        optionC = view.findViewById(R.id.optionC)
        optionD = view.findViewById(R.id.optionD)
        nextBtn = view.findViewById(R.id.next_question_btn)
        progressBar = view.findViewById(R.id.round_progress_bar)

        firebaseFirestore.collection("ModuleList").document(moduleId).collection("Questions")
            .get().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // retrieving all questions in a collection
                    questionList = task.result?.toObjects(Question::class.java)!!.toMutableList()
                    correctAnswersCounter()
                    addUserIdToAchievements()
                    removeCorrectlyAnsweredQuestions(object : QuestionListCallback {
                        override fun onCallback(value: MutableList<Question>) {
                            fillQuestionFields(position)
                        }
                    })
                    loadQuestion()
                } else {
                    questionTitle.text = "Error retrieving from database!"
                }
            }

        // add all buttons into list for later use in checkAnswer function
        buttonList = mutableListOf(optionA, optionB, optionC, optionD)
        // setup onClickListeners for all option buttons
        for (btn in buttonList) {
            btn.setOnClickListener(this)
        }

        nextBtn.setOnClickListener(this)
    }

    private fun loadQuestion() {
        enableButtons()
    }

    /**
     * adds document with id equal to [userId] to the each achievement under collection Obtained with field called [bool] of type boolean
     */
    private fun addUserIdToAchievements() {
        val achievementsMap = HashMap<String, Any>()
        achievementsMap["bool"] = false
        viewModel.achievements.observe(viewLifecycleOwner, { achievements ->
            achievements.forEach {
                firebaseFirestore.collection("Achievements").document(it.id)
                    .collection("Obtained").document(userId).get()
                    .addOnCompleteListener { task ->
                        if (task.result?.exists() == false) {
                            firebaseFirestore.collection("Achievements").document(it.id)
                                .collection("Obtained").document(userId).set(achievementsMap)
                        }
                    }
            }
        })
    }

    /**
     * adds document with id equal to [userId], if this doesn't exist, to each question under collection CorrectAnswers
     * with field called [count] of type int
     */
    private fun correctAnswersCounter() {
        val counterMap = HashMap<String, Any>()
        counterMap["count"] = 0
        questionList.forEach { question ->
            firebaseFirestore.collection("ModuleList").document(moduleId)
                .collection("Questions").document(question.id)
                .collection("CorrectAnswers").document(userId).get().addOnCompleteListener { task ->
                    if (task.result?.exists() == false) {
                        firebaseFirestore.collection("ModuleList").document(moduleId)
                            .collection("Questions").document(question.id)
                            .collection("CorrectAnswers").document(userId).set(counterMap)
                    }
                }
        }
    }

    /**
     * callback interface to wait for firestore query to complete
     */
    interface QuestionListCallback {
        fun onCallback(value: MutableList<Question>)
    }

    /**
     * iterates through 10 questions and check if user answered it less than 6 times correctly;
     * if so add it to [selectedQuestions], else don't
     */
    private fun removeCorrectlyAnsweredQuestions(myCallback: QuestionListCallback) {
        var size = 0
        questionList.forEach { question ->
            firebaseFirestore.collection("ModuleList").document(moduleId)
                .collection("Questions").document(question.id)
                .collection("CorrectAnswers").document(userId).get().addOnCompleteListener { task ->
                    if (task.result?.exists() == true) {
                        val document = task.result
                        if (document?.data?.get("count").toString().toInt() < 6 && size < 10) {
                            selectedQuestions.add(question)
                            size++
                        }
                    } else if(size < 10){
                        selectedQuestions.add(question)
                        size++
                    }
                    myCallback.onCallback(selectedQuestions)
                }
        }

    }

    /**
     * add value of question to their fields accordingly
     */
    private fun fillQuestionFields(pos: Int) {
        // Load image of the question
        Glide.with(this).load(selectedQuestions[pos].image).into(questionImage)
        // Load question
        questionTitle.text = selectedQuestions[pos].question
        // Load button text
        optionA.text = selectedQuestions[pos].optionA
        optionB.text = selectedQuestions[pos].optionB
        optionC.text = selectedQuestions[pos].optionC
        optionD.text = selectedQuestions[pos].optionD
        Log.d(TAG, " this is the list $pos")

        allowAnswer = true
    }

    /**
     * enables all the buttons
     */
    private fun enableButtons() {
        startTimer()
        for (btn in buttonList) {
            btn.visibility = View.VISIBLE
            btn.isEnabled = true
        }
        hideNextBtn()
    }

    /**
     * if quizz reaches last question then [nextBtn] text will be set to show results
     */
    private fun showNextBtn() {
        if (position == 9) {
            nextBtn.text = "Show Results"
        }
        nextBtn.visibility = View.VISIBLE
        nextBtn.isEnabled = true
    }

    /**
     * hides [nextBtn]
     */
    private fun hideNextBtn() {
        nextBtn.visibility = View.INVISIBLE
        nextBtn.isEnabled = false
    }

    /**
     * starts countdown timer (10s)
     */
    private fun startTimer() {
        progressBar.visibility = View.VISIBLE
        val time: Long = 10

        // Countdown timer
        countDown = object : CountDownTimer((time) * 1000, 10) {
            override fun onTick(millisUntilFinished: Long) {

                timer.text = ((millisUntilFinished / 1000).toString())

                val percent: Long = millisUntilFinished / (time * 10)
                progressBar.progress = percent.toInt()
            }

            override fun onFinish() {
                allowAnswer = false
                didNotAnswer++
                streakCounter.text = "0"
                showNextBtn()
            }
        }
        countDown.start()
    }


    override fun onClick(v: View?) {
        when (v?.id) {
            optionA.id -> checkAnswer(optionA)
            optionB.id -> checkAnswer(optionB)
            optionC.id -> checkAnswer(optionC)
            optionD.id -> checkAnswer(optionD)
            nextBtn.id -> {
                if (position == 9) {
                    showResults()
                } else {
                    startTimer()
                    position++
                    resetButtonsColor()
                    fillQuestionFields(position)
                }
            }
        }
    }

    /**
     * add results to database according to the [userId]
     */
    private fun showResults() {
        score = (correctAnswer * 100) / 10
        val results = HashMap<String, Any>()
        results["correct"] = correctAnswer
        results["wrong"] = wrongAnswer
        results["didNotAnswer"] = didNotAnswer
        results["score"] = score
        unlockMasterAchievement()
        firebaseFirestore.collection("ModuleList").document(moduleId).collection("Results")
            .document(userId).set(results).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val action =
                        QuestionFragmentDirections.actionQuestionFragmentToRoundStatsFragment()
                    action.questionId = moduleId
                    navController.navigate(action)
                } else {
                    Log.d("TAG", "Error trying to navigate to stats fragment")
                }
            }

        addAllTimeResults()

    }

    /**
     * adds sum of results to the database according to [userId]
     */
    private fun addAllTimeResults() {

        var stats: AllTimeStats?

        firebaseFirestore.collection("ModuleList").document(moduleId).collection("stats")
            .document(userId)
            .get().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    stats = task.result?.toObject(AllTimeStats::class.java)
                    if (stats != null) {
                        stats!!.correct = stats!!.correct + correctAnswer.toInt()
                        stats!!.notAnswered = stats!!.notAnswered + didNotAnswer.toInt()
                        stats!!.score = stats!!.score + score.toInt()
                        stats!!.wrong = stats!!.wrong + wrongAnswer.toInt()
                        stats!!.gamesNum = stats!!.gamesNum + 1
                        firebaseFirestore.collection("ModuleList").document(moduleId)
                            .collection("stats").document(userId).set(stats!!)
                    } else {
                        stats = AllTimeStats(correctAnswer, didNotAnswer, score, wrongAnswer, 1)
                        firebaseFirestore.collection("ModuleList").document(moduleId)
                            .collection("stats").document(userId).set(stats!!)
                        unlockNewbieAchievement()
                    }
                } else {
                    Log.d("TAG", "Error retrieving from database!")
                }
            }

    }

    /**
     * unlocks achievement called newbie
     */
    private fun unlockNewbieAchievement(){
        firebaseFirestore.collection("Achievements")
            .document("JV6U1Ix1uErPqCt91Yxz").collection("Obtained")
            .document(userId).update("bool", true)
        sendNotification("JV6U1Ix1uErPqCt91Yxz")
    }
    /**
     * unlocks achievement called master
     */
    private fun unlockMasterAchievement(){
        if (correctAnswer == 10){
            firebaseFirestore.collection("Achievements")
                .document("f3CucgzJakFXt0jkRbqX").collection("Obtained")
                .document(userId).update("bool", true)
            sendNotification("f3CucgzJakFXt0jkRbqX")
        }
    }

    /**
     * resets color of button to default
     */
    private fun resetButtonsColor() {
        for (btn in buttonList) {
            btn.background = ResourcesCompat.getDrawable(resources, R.drawable.button_corner, null)
        }
        hideNextBtn()
    }

    /**
     * check if answer if correct or not
     * colors change according to their correctness
     */
    private fun checkAnswer(button: Button) {
        if (allowAnswer) {
            if (button.text.trim() == selectedQuestions[position].answer.trim()) {
                // correct answer; increment counter by one; change button color
                streakCounter.text = (streakCounter.text.toString().toInt() + 1).toString()
                correctAnswer++

                button.background =
                    ResourcesCompat.getDrawable(resources, R.drawable.correct_answer, null)
                firebaseFirestore.collection("ModuleList").document(moduleId)
                    .collection("Questions").document(selectedQuestions[position].id)
                    .collection("CorrectAnswers").document(userId)
                    .update("count", FieldValue.increment(1))
            } else {
                // wrong answer; reset counter to 0
                streakCounter.text = "0"
                wrongAnswer++
                button.background =
                    ResourcesCompat.getDrawable(resources, R.drawable.wrong_answer, null)
                for (btn in buttonList) {
                    if (btn.text.trim() == selectedQuestions[position].answer.trim()) {
                        btn.background =
                            ResourcesCompat.getDrawable(resources, R.drawable.correct_answer, null)
                    }
                }
            }

            when {
                streakCounter.text.toString().toInt() < 4 -> {
                    streakCounter.setTextColor(Color.parseColor("#FF0000"))
                }
                streakCounter.text.toString().toInt() in 4..7 -> {
                    streakCounter.setTextColor(Color.parseColor("#FFFF00"))
                }
                streakCounter.text.toString().toInt() >= 8 -> {
                    streakCounter.setTextColor(Color.parseColor("#00FF00"))
                }
            }
            allowAnswer = false
            // stop countdown
            countDown.cancel()
            showNextBtn()
        }
    }

    /**
     * creates a notification channel
     */
    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "notif title"
            val descriptionText = "notif desc"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(requireContext(), NotificationManager::class.java) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * sends a notification to the user with the details of the unlocked achievement
     */
    private fun sendNotification(achievementId : String){
        val intent = Intent(requireContext(), MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(requireContext(),0,intent,0)


        viewModel.achievements.observe(viewLifecycleOwner, { list ->
            list.forEach { if (it.id == achievementId){
                val builder = NotificationCompat.Builder(requireContext(), CHANNEL_ID)
                    .setSmallIcon(R.drawable.logo)
                    .setContentTitle(it.title + " achievement unlocked!")
                    .setContentText(it.description)
                    .setContentIntent(pendingIntent)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setAutoCancel(true)
                with(NotificationManagerCompat.from(requireContext())) {
                    notify(notificationId, builder.build())
                }
            }
            }
        })

    }

}