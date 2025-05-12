package de.thm.ap.bilderraetsel

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import de.thm.ap.bilderraetsel.model.Question

/**
 *  Custom adapter for Question List implementing a custom onClicklistener [onQuestionClickListener]
 */
class QuestionListAdapter(private var onQuestionClickListener: OnQuestionClickListener) :
    RecyclerView.Adapter<QuestionListAdapter.QuestionListHolder>() {

    private var questionList = listOf<Question>()

    /**
     *  Set list of questions to [list]
     */
    fun setQuestionList(list: List<Question>){
        this.questionList = list
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuestionListHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.single_question_list_item, parent, false)
        return QuestionListHolder(itemView)
    }


    override fun onBindViewHolder(holder: QuestionListHolder, position: Int) {
        val currentItem = questionList[position]
        val imageURL = currentItem.image
        val question = currentItem.question

        holder.questionText.text = question
        Glide.with(holder.itemView.context).load(imageURL).centerCrop().into(holder.questionImage)

        holder.itemView.setOnClickListener{
            onQuestionClickListener.onItemClick(position)
        }

    }

    override fun getItemCount(): Int {
        return questionList.size
    }
    /**
     * Class to hold data from the layout
     */
    class QuestionListHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val questionImage: ImageView = itemView.findViewById(R.id.question_list_image)
        val questionText: TextView = itemView.findViewById(R.id.question_list_text)
    }

    /**
     * custom onClickListener
     */
    interface OnQuestionClickListener {
        fun onItemClick(position: Int)
    }
}