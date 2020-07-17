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
import de.codereddev.wordup.database.Word
import de.codereddev.wordupexample.R
import de.codereddev.wordupexample.view.adapter.WordListAdapter
import de.codereddev.wordupexample.viewmodel.WordListViewModel
import kotlinx.android.synthetic.main.fragment_word_list.view.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class WordListFragment : Fragment() {
    private val viewModel: WordListViewModel by viewModel()
    private val wordListAdapter = WordListAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_word_list, container, false)

        wordListAdapter.itemClickListener = object : WordListAdapter.ItemClickListener {
            override fun onItemClicked(word: Word) {
                viewModel.onWordClick(word)
            }

            override fun onItemLongClick(word: Word, view: View) {
                val popup = PopupMenu(requireContext(), view)
                popup.inflate(R.menu.word_options_menu)
                popup.setOnMenuItemClickListener {
                    viewModel.onMenuItemSelected(word, it.itemId)
                    true
                }
                popup.show()
            }
        }

        rootView.word_list_rv.apply {
            adapter = wordListAdapter
            layoutManager = GridLayoutManager(context, 3)
        }

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.wordList.observe(viewLifecycleOwner, Observer {
            wordListAdapter.setDataset(it)
        })

        viewModel.events.observe(viewLifecycleOwner, Observer {
            when (it) {
                WordListViewModel.Event.PERMISSION_WRITE_SETTINGS -> {
                    val intent = Intent().apply {
                        action = android.provider.Settings.ACTION_MANAGE_WRITE_SETTINGS
                        data = Uri.parse("package:${requireContext().packageName}")
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    startActivity(intent)
                }
                WordListViewModel.Event.SYSTEM_SOUND_SET -> {
                    Toast.makeText(context, R.string.system_sound_set, Toast.LENGTH_SHORT).show()
                }
                WordListViewModel.Event.WORD_SAVED -> {
                    val text = getString(R.string.word_saved, WordUp.config.directory)
                    Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
                }
                else -> {
                }
            }
        })

        viewModel.intents.observe(viewLifecycleOwner, Observer {
            requireContext().startActivity(
                Intent.createChooser(
                    it,
                    getString(R.string.share_word_via)
                )
            )
        })

        viewModel.permissionRequests.observe(viewLifecycleOwner, Observer {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    it.permission
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                viewModel.onPermissionResult(it, true)
            } else {
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(it.permission),
                    it.action
                )
            }
        })
    }
}
