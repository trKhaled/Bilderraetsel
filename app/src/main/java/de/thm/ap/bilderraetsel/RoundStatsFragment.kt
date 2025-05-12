package de.thm.ap.bilderraetsel

import android.animation.ObjectAnimator
import android.content.ContentValues.TAG
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


/**
 * A simple [Fragment] subclass for round stat view
 * Use the [RoundStatsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class RoundStatsFragment : Fragment() {

    private lateinit var navController: NavController
    private lateinit var firebaseFirestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private var questionId: String = ""
    private var userId: String =""
    private var c :String=""
    private lateinit var progressBar: ProgressBar
    private var parsedInt:Int? =0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_round_stats, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        firebaseFirestore = FirebaseFirestore.getInstance()
        navController = Navigation.findNavController(view)
        val correct = view.findViewById<TextView>(R.id.correct_text)
        val wrong = view.findViewById<TextView>(R.id.wrong_text)
        val unanswered = view.findViewById<TextView>(R.id.unanswered_text)
        val score = view.findViewById<TextView>(R.id.score_text)
        val result = view.findViewById<TextView>(R.id.timer)
        progressBar = view.findViewById(R.id.round_progress_bar)

        auth = FirebaseAuth.getInstance()
        if (auth.currentUser != null) {
            userId = auth.currentUser!!.uid
        } else {
            navController.navigate(R.id.moduleFragment)
        }

        questionId = RoundStatsFragmentArgs.fromBundle(requireArguments()).questionId

        firebaseFirestore.collection("ModuleList").document(questionId).collection("Results")
            .document(userId).get().addOnCompleteListener { task ->
            if (task.isSuccessful) {

                val document = task.result
                correct.text = document?.data?.get("correct").toString()
                wrong.text = document?.data?.get("wrong").toString()
                unanswered.text = document?.data?.get("didNotAnswer").toString()
                score.text = document?.data?.get("score").toString()+"%"
                result.text=document?.data?.get("score").toString()+"%"

                when {
                    document?.data?.get("score").toString().toInt() < 40 -> {
                        score.setTextColor(Color.parseColor("#FF0000"))
                        result.setTextColor(Color.parseColor("#FF0000"))
                    }
                    document?.data?.get("score").toString().toInt() in 40..70 -> {
                        score.setTextColor(Color.parseColor("#FFFF00"))
                        result.setTextColor(Color.parseColor("#FFFF00"))
                    }
                    document?.data?.get("score").toString().toInt() >= 80 -> {
                        score.setTextColor(Color.parseColor("#00FF00"))
                        result.setTextColor(Color.parseColor("#00FF00"))
                    }
                }

                 c = document?.data?.get("score").toString()
                //println("------------------->11111"+c)
                parsedInt = c.toIntOrNull()
                //println("------------------->222222"+parsedInt)
                if (parsedInt != null) {
                    progressBar.max=100
                    ObjectAnimator.ofInt(progressBar,"progress", parsedInt!!)
                        .setDuration(2000)
                        .start()
                }


            } else {
                Log.d(TAG, "Cached get failed: ", task.exception)
            }

        }


        val backBtn: Button = view.findViewById(R.id.back_to_module_btn)

        backBtn.setOnClickListener{
            navController.navigate(R.id.moduleFragment)

        }

    }
}