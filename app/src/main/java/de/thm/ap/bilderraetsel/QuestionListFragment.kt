package de.thm.ap.bilderraetsel

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import de.thm.ap.bilderraetsel.model.Question


/**
 * A simple [Fragment] subclass for question list view
 * Use the [QuestionListFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class QuestionListFragment : Fragment(), QuestionListAdapter.OnQuestionClickListener {

    private lateinit var questionListView: RecyclerView
    private lateinit var adapter: QuestionListAdapter
    private lateinit var navController: NavController
    private lateinit var firebaseFirestore: FirebaseFirestore
    private var questionId: String = ""
    private var questionList: MutableList<Question> = ArrayList()
    private lateinit var newQuestionButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_question_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        firebaseFirestore = FirebaseFirestore.getInstance()
        navController = Navigation.findNavController(view)
        newQuestionButton = view.findViewById(R.id.new_question_button)
        questionListView = view.findViewById(R.id.question_list) as RecyclerView
        adapter = QuestionListAdapter(this)

        // Get question id
        questionId = QuestionListFragmentArgs.fromBundle(requireArguments()).questionListId
        questionListView.layoutManager = LinearLayoutManager(this.requireContext())
        // fixed size for each element
        questionListView.hasFixedSize()

        firebaseFirestore.collection("ModuleList").document(questionId).collection("Questions")
            .get().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // retrieving all questions in a collection
                    questionList = task.result?.toObjects(Question::class.java)!!.toMutableList()
                    adapter.setQuestionList(questionList)
                    questionListView.adapter = adapter
            }
        }
        newQuestionButton.setOnClickListener{
            val action = QuestionListFragmentDirections.actionQuestionListFragmentToQuestionFormFragment()
            action.collectionId = questionId
            navController.navigate(action)
        }
    }

    override fun onItemClick(position: Int) {
        val action = QuestionListFragmentDirections.actionQuestionListFragmentToQuestionDetailFragment()
        action.questionId = questionList[position].id
        action.collectionId = questionId
        navController.navigate(action)
    }
}
