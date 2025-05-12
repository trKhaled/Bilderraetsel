package de.thm.ap.bilderraetsel

import android.animation.ObjectAnimator
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import de.thm.ap.bilderraetsel.model.AllTimeStats
import de.thm.ap.bilderraetsel.model.Module

/**
 * A simple [Fragment] subclass for sum of all stats of one spectific user
 * Use the [AllStatsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AllStatsFragment : Fragment() {

    private lateinit var navController: NavController
    private lateinit var firebaseFirestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private var userId: String = ""
    private lateinit var progressBar: ProgressBar
    private var parsedInt: Int? = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_all_stats, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        firebaseFirestore = FirebaseFirestore.getInstance()
        navController = Navigation.findNavController(view)
        // Bind data from layout
        val correct = view.findViewById<TextView>(R.id.correct_text)
        val wrong = view.findViewById<TextView>(R.id.wrong_text)
        val unanswered = view.findViewById<TextView>(R.id.unanswered_text)
        val score = view.findViewById<TextView>(R.id.score_text)
        val result = view.findViewById<TextView>(R.id.timer)
        val games = view.findViewById<TextView>(R.id.textView6)
        progressBar = view.findViewById(R.id.round_progress_bar)
        auth = FirebaseAuth.getInstance()
        if (auth.currentUser != null) {
            userId = auth.currentUser!!.uid
        } else {
            navController.navigate(R.id.moduleFragment)
        }

        // retrieve stats from firestore according to userId and increment them accordingly
        firebaseFirestore.collection("ModuleList").get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val modules = task.result?.toObjects(Module::class.java)
                val allModules: AllTimeStats = AllTimeStats()
                var newStat: AllTimeStats?
                Log.d("TAG", "modules = $modules")
                modules?.forEach { module ->
                    firebaseFirestore.collection("ModuleList").document(module.id)
                        .collection("stats").document(userId).get()
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {

                                newStat = task.result?.toObject(AllTimeStats::class.java)
                                // if stats already exists increment with past scores
                                if (newStat != null) {
                                    allModules.gamesNum = allModules.gamesNum + newStat!!.gamesNum
                                    allModules.wrong = allModules.wrong + newStat!!.wrong
                                    allModules.score = allModules.score + newStat!!.score
                                    allModules.notAnswered = allModules.notAnswered + newStat!!.notAnswered
                                    allModules.correct = allModules.correct + newStat!!.correct
                                } else {
                                    allModules.gamesNum = allModules.gamesNum
                                    allModules.wrong = allModules.wrong
                                    allModules.score = allModules.score
                                    allModules.notAnswered = allModules.notAnswered
                                    allModules.correct = allModules.correct
                                }
                            }
                            Log.d("TAG", "All Modules ===== $allModules")
                            correct.text = allModules.correct.toString()
                            wrong.text = allModules.wrong.toString()
                            unanswered.text = allModules.notAnswered.toString()
                            val scorez = allModules.score.toString()
                            score.text = scorez
                            games.text = allModules.gamesNum.toString()
                            if (allModules.gamesNum != 0) {
                                result.text =
                                    (scorez.toInt() / allModules.gamesNum.toInt()).toString()
                            } else {
                                result.text = "0"
                            }
                            when {
                                // color changes according to score
                                scorez.toInt() < 40 -> {
                                    score.setTextColor(Color.parseColor("#FF0000"))
                                    result.setTextColor(Color.parseColor("#FF0000"))
                                }
                                scorez.toInt() in 40..70 -> {
                                    score.setTextColor(Color.parseColor("#FFFF00"))
                                    result.setTextColor(Color.parseColor("#FFFF00"))
                                }
                                scorez.toInt() >= 80 -> {
                                    score.setTextColor(Color.parseColor("#00FF00"))
                                    result.setTextColor(Color.parseColor("#00FF00"))
                                }
                            }

                            parsedInt = scorez.toIntOrNull()
                            if (parsedInt != null) {
                                progressBar.max=100
                                ObjectAnimator.ofInt(progressBar,"progress", parsedInt!!)
                                    .setDuration(2000)
                                    .start()
                            }
                        }
                }
            }
        }


    }
}