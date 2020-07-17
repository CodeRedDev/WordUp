package de.codereddev.wordupexample.view.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import de.codereddev.wordup.model.database.Word
import de.codereddev.wordupexample.R

class WordListAdapter : RecyclerView.Adapter<WordListAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.word_name)
    }

    interface ItemClickListener {
        fun onItemClicked(word: Word)

        fun onItemLongClick(word: Word, view: View)
    }

    private val wordList: MutableList<Word> = mutableListOf()
    var itemClickListener: ItemClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.word_item, parent, false)
        return ViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return wordList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val word = wordList[position]
        holder.name.text = word.name

        holder.itemView.setOnClickListener {
            itemClickListener?.onItemClicked(word)
        }

        holder.itemView.setOnLongClickListener {
            itemClickListener?.onItemLongClick(word, it)
            true
        }
    }

    fun setDataset(list: List<Word>) {
        wordList.clear()
        wordList.addAll(list)
        notifyDataSetChanged()
    }
}
