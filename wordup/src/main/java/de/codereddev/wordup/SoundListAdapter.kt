package de.codereddev.wordup

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import de.codereddev.wordup.dao.model.Sound
import kotlin.random.Random

class SoundListAdapter(
    clickListener: SoundClickListener
) : AbstractSoundListAdapter(clickListener) {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val buttonImg = itemView.findViewById<ImageView>(R.id.buttonImg)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.sound_item, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        // TODO: Right codestyle?
        holder as ViewHolder

        //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.

        // TODO: Img resource in config
        // TODO: Random img resources in config?

        // Set the sounds button image
        // TODO: Many calls to onBindView --> Performance?
        if (WordUp.config.soundImgResourceArray != null) {
            WordUp.config.soundImgResourceArray!!.let {
                holder.buttonImg.setImageResource(it[Random.nextInt(0, it.size)])
            }
        } else {
            if (WordUp.config.soundImgResource == null)
                throw IllegalArgumentException("WordUp requires at least a single drawable resource to display as sound button image!")

            holder.buttonImg.setImageResource(WordUp.config.soundImgResource!!)
        }

        holder.itemView.setOnClickListener { clickListener.onSoundClick(soundList[position]) }
        holder.itemView.setOnLongClickListener {
            clickListener.onSoundLongClick(soundList[position])
            return@setOnLongClickListener true
        }
    }

    override fun swapSoundList(newList: List<Sound>) {
        soundList.clear()
        soundList.addAll(newList)
        notifyDataSetChanged()
    }

    override fun onAttachedToRecyclerView(recyclerView: androidx.recyclerview.widget.RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)

        (recyclerView.layoutManager as GridLayoutManager).spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {

            override fun getSpanSize(position: Int): Int {

                if (soundList.size % 3 == 1 && position == soundList.size - 1)
                    return 6
                if (soundList.size % 3 == 2 && (position == soundList.size - 1 || position == soundList.size - 2))
                    return 3

                return 2
            }
        }
    }

}