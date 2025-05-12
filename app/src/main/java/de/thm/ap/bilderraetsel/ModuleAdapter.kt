package de.thm.ap.bilderraetsel

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import de.thm.ap.bilderraetsel.model.Module

/**
 *  Custom adapter for Achievements List implementing a custom onClicklistener [onModuleClickListener]
 */
class ModuleAdapter(private var onModuleClickListener: OnModuleClickListener) :
    RecyclerView.Adapter<ModuleAdapter.ModuleViewHolder>() {

    private var moduleList = listOf<Module>()

    /**
     *  Set list of modules to [list]
     */
    fun setModuleList(list: List<Module>){
        this.moduleList = list
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ModuleViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.single_list_item, parent, false)
        return ModuleViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ModuleViewHolder, position: Int) {
        val currentItem = moduleList[position]
        val imageURL = currentItem.image
        var desc = currentItem.description

        when (currentItem.level) {
            "Easy" -> {
                holder.listLevel.setTextColor(Color.parseColor("#00FF00"))
            }
            "Intermediate" -> {
                holder.listLevel.setTextColor(Color.parseColor("#FFFF00"))
            }
            "Hard" -> {
                holder.listLevel.setTextColor(Color.parseColor("#FF0000"))
            }
        }
        if(desc.length > 150){
            desc = desc.substring(0,150)
        }
        holder.listTitle.text = currentItem.name
        holder.listDesc.text = desc.plus("...")
        holder.listLevel.text = currentItem.level
        Glide.with(holder.itemView.context).load(imageURL).centerCrop().into(holder.listImage)
        holder.listBtn.setOnClickListener {
            onModuleClickListener.onItemClick(position)
        }
        holder.changeBtn.setOnClickListener {
            onModuleClickListener.onChangeBtnClick(position)
        }
    }

    override fun getItemCount(): Int {
        return moduleList.size
    }

    /**
     * Class to hold data from the layout
     */
    class ModuleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val listImage: ImageView = itemView.findViewById(R.id.moduleImage)
        val listTitle: TextView = itemView.findViewById(R.id.list_title)
        val listDesc: TextView = itemView.findViewById(R.id.list_desc)
        val listLevel: TextView = itemView.findViewById(R.id.list_level)
        val listBtn: Button = itemView.findViewById(R.id.list_btn)
        val changeBtn: Button = itemView.findViewById(R.id.change_btn)
    }

    /**
     * custom onClickListener
     */
    interface OnModuleClickListener {
        fun onItemClick(position: Int)
        fun onChangeBtnClick(position: Int)
        fun onNewModuleBtnClick()
    }
}
