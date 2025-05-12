package de.thm.ap.bilderraetsel

import android.content.ContentValues
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

/**
 * A simple [Fragment] subclass for detail view of a specific module
 * Use the [DetailsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class DetailsFragment : Fragment() {

    private lateinit var viewModel: ModuleViewModel
    private lateinit var navController: NavController
    private var pos: Int = 0
    private var questionId: String = ""
    private lateinit var startBtn: Button
    private lateinit var checkBtn: Button
    private var questionSum: Long = 0
    private lateinit var firebaseFirestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private var userId: String = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Check position of selected module
        pos = DetailsFragmentArgs.fromBundle(requireArguments()).position
        Log.d("POS_LOG", "Position : $pos")

        navController = Navigation.findNavController(view)

        viewModel = ViewModelProvider(this)[ModuleViewModel::class.java]
        firebaseFirestore = FirebaseFirestore.getInstance()

        val detailsTitle = view.findViewById<TextView>(R.id.details_title)
        val detailsDesc = view.findViewById<TextView>(R.id.details_desc)
        val detailsLevelText = view.findViewById<TextView>(R.id.details_level_text)
        val detailsQuestionsText = view.findViewById<TextView>(R.id.details_questions_text)
        val detailsImage = view.findViewById<ImageView>(R.id.details_image)

        val detailsScore = view.findViewById<TextView>(R.id.details_score_text)
        startBtn = view.findViewById(R.id.start_btn)
        checkBtn = view.findViewById(R.id.check_btn)

        // start the quizz
        startBtn.setOnClickListener {
            val action = DetailsFragmentDirections.actionDetailFragmentToQuestionFragment()
            action.questionId = questionId
            navController.navigate(action)
        }
        // check list of all questions
        checkBtn.setOnClickListener {
            val action = DetailsFragmentDirections.actionDetailFragmentToQuestionListFragment()
            action.questionListId = questionId
            navController.navigate(action)
        }

        auth = FirebaseAuth.getInstance()
        if (auth.currentUser != null) {
            userId = auth.currentUser!!.uid
        } else {
            navController.navigate(R.id.moduleFragment)
        }


        viewModel.module.observe(viewLifecycleOwner, Observer {
            when (it[pos].level) {
                "Easy" -> {
                    detailsLevelText.setTextColor(Color.parseColor("#00FF00"))
                }
                "Intermediate" -> {
                    detailsLevelText.setTextColor(Color.parseColor("#FFFF00"))
                }
                "Hard" -> {
                    detailsLevelText.setTextColor(Color.parseColor("#FF0000"))
                }
            }

            detailsTitle.text = it[pos].name
            detailsDesc.text = it[pos].description
            detailsLevelText.text = it[pos].level
            detailsQuestionsText.text = it[pos].questions.toString()
            Glide.with(requireContext()).load(it[pos].image).centerCrop().into(detailsImage)
            // Retrieve question Id value
            questionId = it[pos].id
            questionSum = it[pos].questions
            firebaseFirestore.collection("ModuleList").document(it[pos].id).collection("Results")
                .document(userId).get().addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val document = task.result
                        if (document?.data?.get("score") == null) {
                            detailsScore.text = "0%"
                        } else {
                            detailsScore.text = document.data?.get("score").toString().plus("%")
                        }
                        when {
                            document?.data?.get("score") == null -> {
                                detailsScore.setTextColor(Color.parseColor("#FF0000"))
                            }
                            document.data?.get("score").toString().toInt() < 40 -> {
                                detailsScore.setTextColor(Color.parseColor("#FF0000"))
                            }
                            document.data?.get("score").toString().toInt() in 40..70 -> {
                                detailsScore.setTextColor(Color.parseColor("#FFFF00"))
                            }
                            document.data?.get("score").toString().toInt() >= 80 -> {
                                detailsScore.setTextColor(Color.parseColor("#00FF00"))
                            }
                        }
                    } else {
                        Log.d(ContentValues.TAG, "Cached get failed: ", task.exception)
                    }
                }
        })
    }
}