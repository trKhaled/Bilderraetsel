package de.thm.ap.bilderraetsel

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.StorageReference
import de.thm.ap.bilderraetsel.model.Module
import de.thm.ap.bilderraetsel.model.Question
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream

/**
 * A simple [Fragment] subclass for question detail view
 * Use the [QuestionDetailFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class QuestionDetailFragment : Fragment() {
    private lateinit var firebaseFirestore: FirebaseFirestore

    private var collectionId: String = ""
    private var questionId: String = ""

    private lateinit var questionImage: ImageView
    private lateinit var questionTitle: TextView
    private lateinit var optionA: Button
    private lateinit var optionB: Button
    private lateinit var optionC: Button
    private lateinit var optionD: Button
    private lateinit var question: Question
    private lateinit var change: Button
    private lateinit var navController: NavController
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_question_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        firebaseFirestore = FirebaseFirestore.getInstance()
        navController = Navigation.findNavController(view)
        questionImage = view.findViewById(R.id.question_detail_image)
        questionTitle = view.findViewById(R.id.question_detail_title)
        optionA = view.findViewById(R.id.question_detail_optionA)
        optionB = view.findViewById(R.id.question_detail_optionB)
        optionC = view.findViewById(R.id.question_detail_optionC)
        optionD = view.findViewById(R.id.question_detail_optionD)
        change = view.findViewById(R.id.change_question)
        // Get question id
        collectionId = QuestionDetailFragmentArgs.fromBundle(requireArguments()).collectionId
        // Get question id
        questionId = QuestionDetailFragmentArgs.fromBundle(requireArguments()).questionId


        firebaseFirestore.collection("ModuleList").document(collectionId).collection("Questions").document(questionId)
            .get().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // retrieving all questions in a collection
                    question = task.result?.toObject(Question::class.java)!!
                    loadUI()
                } else {
                    questionTitle.text = "Error retrieving from database!"
                }
            }
            change.setOnClickListener {
                val action = QuestionDetailFragmentDirections.actionQuestionDetailFragmentToQuestionFormFragment()
                action.questionId = questionId
                action.collectionId = collectionId
                navController.navigate(action)
            }
        }

    /**
     * assigns data to UI
     */
    private fun loadUI(){
        // Load image of the question
        Glide.with(this).load(question.image).into(questionImage)
        questionTitle.text = question.question
        optionA.text = question.optionA
        optionB.text = question.optionB
        optionC.text = question.optionC
        optionD.text = question.optionD
    }
}