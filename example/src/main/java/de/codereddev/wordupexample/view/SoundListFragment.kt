package de.codereddev.wordupexample.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import de.codereddev.wordup.model.database.Sound
import de.codereddev.wordupexample.R
import de.codereddev.wordupexample.view.adapter.SoundListAdapter
import de.codereddev.wordupexample.viewmodel.SoundListViewModel
import kotlinx.android.synthetic.main.fragment_sound_list.view.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class SoundListFragment : Fragment() {
    private val viewModel: SoundListViewModel by viewModel()
    private val soundListAdapter = SoundListAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_sound_list, container, false)

        rootView.add_sound_btn.setOnClickListener {
            viewModel.addSound()
        }

        soundListAdapter.itemClickListener = object : SoundListAdapter.ItemClickListener {
            override fun onItemClicked(sound: Sound) {
                viewModel.deleteSound(sound)
            }
        }

        rootView.sound_list_rv.apply {
            adapter = soundListAdapter
            layoutManager = GridLayoutManager(context, 3)
        }

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.soundList.observe(viewLifecycleOwner, Observer {
            soundListAdapter.setDataset(it)
        })

        viewModel.events.observe(viewLifecycleOwner, Observer {
            val eventMessage = when (it) {
                SoundListViewModel.Event.INSERT -> "Inserted sound"
                else -> "Deleted sound"
            }
            Toast.makeText(context, eventMessage, Toast.LENGTH_SHORT).show()
        })
    }
}
