package de.codereddev.wordup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import de.codereddev.wordup.dao.model.Sound

abstract class BaseSoundFragment : Fragment(), SoundView, SoundClickListener {

    protected lateinit var soundPresenter: SoundPresenter

    protected var soundListAdapter: AbstractSoundListAdapter = SoundListAdapter(this)

    protected var soundPlayer: SoundPlayer = SoundPlayerImpl()

    protected var layoutManager: RecyclerView.LayoutManager = GridLayoutManager(context, 6)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_base_sound, container, false)

        // TODO: Toolbar?

        val recyclerView = rootView.findViewById<RecyclerView>(R.id.soundRv)
        recyclerView.layoutManager = layoutManager

        // TODO: soundListAdapter
        recyclerView.adapter = soundListAdapter

        // TODO: Establish data?
        soundPresenter.retrieveSoundList()

        return rootView
    }

    override fun onDestroy() {
        super.onDestroy()
        soundPlayer.release()
    }

    override fun onReceiveSoundList(soundList: List<Sound>) {
        soundListAdapter.swapSoundList(soundList)
    }

    override fun onSoundClick(sound: Sound) {
        soundPlayer.play(context!!, sound)
        // TODO: Status change
    }

    override fun onSoundLongClick(sound: Sound) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}