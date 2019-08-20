package de.codereddev.wordup

import androidx.recyclerview.widget.RecyclerView
import de.codereddev.wordup.dao.model.Sound

abstract class AbstractSoundListAdapter(
    protected val clickListener: SoundClickListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    protected val soundList = mutableListOf<Sound>()

    override fun getItemCount(): Int {
        return soundList.size
    }

    /**
     * Swap the current sound list.
     *
     * @param newList The list of new sounds to display.
     */
    abstract fun swapSoundList(newList: List<Sound>)
}