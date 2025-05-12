package de.thm.ap.bilderraetsel

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import de.thm.ap.bilderraetsel.model.Achievements

/**
 * Custom adapter for Achievements List
 */
class AchievementsAdapter() :
    RecyclerView.Adapter<AchievementsAdapter.AchievementsListHolder>() {

    private var achievementsList = listOf<Achievements>()
    private lateinit var firebaseFirestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private var userId : String =""

    /**
     * Set list of achievements to [list]
     */
    fun setAchievementsList(list: List<Achievements>){
        this.achievementsList = list
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AchievementsListHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.single_achievement_list_item, parent, false)
        return AchievementsListHolder(itemView)
    }


    override fun onBindViewHolder(holder: AchievementsListHolder, position: Int) {
        val currentItem = achievementsList[position]
        val imageURL = currentItem.image
        val title = currentItem.title
        val desc = currentItem.description

        holder.achievementsTitle.text = title
        holder.achievementsDesc.text = desc
        Glide.with(holder.itemView.context).load(imageURL).centerCrop()
            .into(holder.achievementsImage)

        firebaseFirestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        if (auth.currentUser != null) {
            userId = auth.currentUser!!.uid
        } else {
            Log.d("TAG", "Error getting userId")
        }

        firebaseFirestore.collection("Achievements").document(achievementsList[position].id)
            .collection("Obtained")
            .document(userId).get().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    if (task.result?.data?.get("bool") == true) {
                        holder.checkBox.setBackgroundResource(R.drawable.ic_baseline_check_box_48w)
                    } else {
                        holder.checkBox.setBackgroundResource(R.drawable.ic_baseline_check_box_outline_blank_48)
                    }
                }
            }
    }

    override fun getItemCount(): Int {
        return achievementsList.size
    }

    /**
     * Class to hold data from the layout
     */
    class AchievementsListHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val achievementsImage: ImageView = itemView.findViewById(R.id.achievement_list_image)
        val achievementsTitle: TextView = itemView.findViewById(R.id.achievement_list_title)
        val achievementsDesc: TextView = itemView.findViewById(R.id.achievement_list_description)
        val checkBox: ImageView = itemView.findViewById(R.id.achievements_checkbox)
    }

}