package de.codereddev.wordupexample.view

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import de.codereddev.wordup.WordUp
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

        soundListAdapter.itemClickListener = object : SoundListAdapter.ItemClickListener {
            override fun onItemClicked(sound: Sound) {
                viewModel.onSoundClick(sound)
            }

            override fun onItemLongClick(sound: Sound, view: View) {
                val popup = PopupMenu(context!!, view)
                popup.inflate(R.menu.sound_options_menu)
                popup.setOnMenuItemClickListener {
                    viewModel.onMenuItemSelected(sound, it.itemId)
                    true
                }
                popup.show()
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
            when (it) {
                SoundListViewModel.Event.PERMISSION_WRITE_SETTINGS -> {
                    val intent = Intent().apply {
                        action = android.provider.Settings.ACTION_MANAGE_WRITE_SETTINGS
                        data = Uri.parse("package:${context!!.packageName}")
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    startActivity(intent)
                }
                SoundListViewModel.Event.SYSTEM_SOUND_SET -> {
                    Toast.makeText(context, R.string.system_sound_set, Toast.LENGTH_SHORT).show()
                }
                SoundListViewModel.Event.SOUND_SAVED -> {
                    val text = getString(R.string.sound_saved, WordUp.config.directory)
                    Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
                }
                else -> {
                }
            }
        })

        viewModel.intents.observe(viewLifecycleOwner, Observer {
            context!!.startActivity(Intent.createChooser(it, getString(R.string.share_sound_via)))
        })

        viewModel.permissionRequests.observe(viewLifecycleOwner, Observer {
            if (ContextCompat.checkSelfPermission(
                    context!!,
                    it.permission
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                viewModel.onPermissionResult(it, true)
            } else {
                ActivityCompat.requestPermissions(activity!!, arrayOf(it.permission), it.action)
            }
        })
    }
}