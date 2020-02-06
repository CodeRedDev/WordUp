package de.codereddev.wordupexample.view.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import de.codereddev.wordup.model.database.Sound
import de.codereddev.wordupexample.R

class SoundListAdapter : RecyclerView.Adapter<SoundListAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.sound_name)
    }

    interface ItemClickListener {
        fun onItemClicked(sound: Sound)

        fun onItemLongClick(sound: Sound, view: View)
    }

    private val soundList: MutableList<Sound> = mutableListOf()
    var itemClickListener: ItemClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.sound_item, parent, false)
        return ViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return soundList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val sound = soundList[position]
        holder.name.text = sound.name

        holder.itemView.setOnClickListener {
            itemClickListener?.onItemClicked(sound)
        }

        holder.itemView.setOnLongClickListener {
            itemClickListener?.onItemLongClick(sound, it)
            true
        }
    }

    fun setDataset(list: List<Sound>) {
        soundList.clear()
        soundList.addAll(list)
        notifyDataSetChanged()
    }
}
