package com.example.rickandmorty.ui.view

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Bundle
import android.view.*
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.forEach
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.rickandmorty.R
import com.example.rickandmorty.databinding.CharactersFragmentBinding
import com.example.rickandmorty.ui.utils.initLoadStateAdapter
import com.example.rickandmorty.ui.utils.isOnline
import com.example.rickandmorty.ui.utils.restoreEditTextText
import com.example.rickandmorty.ui.adapter.CharacterPagingAdapter
import com.example.rickandmorty.ui.viewmodel.CharacterViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CharactersFragment : Fragment() {

    private var _binding: CharactersFragmentBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CharacterViewModel by viewModels()

    private lateinit var recyclerView: RecyclerView
    private lateinit var pagingAdapter: CharacterPagingAdapter
    private lateinit var characterFilterMap: MutableMap<String, String>
    private lateinit var emptyTextView: TextView
    private lateinit var swipeRefresh: SwipeRefreshLayout

    private var isOnline = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        characterFilterMap = mutableMapOf()
        characterFilterMap.put("isRefresh", "true")
        viewModel.setFilter(characterFilterMap)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = CharactersFragmentBinding.inflate(inflater, container, false)

        isOnline = isOnline(requireContext())

        bindViews()
        initPagingAdapter()
        initSwipeToRefresh()
        initLoadStateAdapter(
            isOnline, emptyTextView, viewLifecycleOwner, recyclerView, activity, swipeRefresh, pagingAdapter
        )

        return binding.root
    }

    override fun onPause() {
        super.onPause()
        characterFilterMap.put("isRefresh", "false")
        viewModel.setFilter(characterFilterMap)
    }

    private fun bindViews() {
        recyclerView = binding.characterRecyclerview
        emptyTextView = binding.emptyTextView
        swipeRefresh = binding.swipeRefresh
    }

    private fun initPagingAdapter() {
        pagingAdapter = CharacterPagingAdapter()
        displayCharacters()
    }

    private fun initSwipeToRefresh() {
        swipeRefresh.setOnRefreshListener { pagingAdapter.refresh() }
    }

    private fun displayCharacters() {
        viewModel.queries.observe(viewLifecycleOwner) { queries ->
            viewLifecycleOwner.lifecycleScope.launch {
                viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                    viewModel.requestCharacters(queries).collectLatest {
                        pagingAdapter.submitData(it)
                    }
                }
            }
        }
    }

    private fun displayFoundCharacters(search: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                viewModel.searchCharacters(search).collectLatest {
                    pagingAdapter.submitData(it)
                }
            }
        }
    }

    @SuppressLint("InflateParams")
    private fun showFilterDialog() {
        val inflater = requireActivity().layoutInflater
        val filterLayout = inflater.inflate(R.layout.characters_filter, null)
        val customTitle = inflater.inflate(R.layout.dialog_title, null)
        val nameEditText = filterLayout.findViewById<EditText>(R.id.name_edit_text)
        val typeEditText = filterLayout.findViewById<EditText>(R.id.type_edit_text)
        val dialog = MaterialAlertDialogBuilder(requireContext())

        val checkBoxList = getCheckBoxes(filterLayout)
        val checkBoxGroups = groupCheckBoxes(checkBoxList)
        val editTextList = listOf<EditText>(nameEditText, typeEditText)

        restoreEditTextText(editTextList, characterFilterMap)
        restoreCheckboxesFlags(checkBoxList)
        preventMultipleCheckBoxSelections(checkBoxGroups)

        // dialog builder
        with(dialog) {
            setView(filterLayout)
            setCustomTitle(customTitle)
            setCancelable(false)
            setPositiveButton("Apply") { _, _ ->
                checkBoxList.filter { it.isChecked }.forEach {
                    characterFilterMap.put(it.transitionName, it.text.toString())
                }
                editTextList.filter { it.text.isNotBlank() }.forEach {
                    characterFilterMap.put(it.transitionName, it.text.toString())
                }
                characterFilterMap.put("isRefresh", "true")
                viewModel.setFilter(characterFilterMap)
            }
            setNegativeButton("Cancel") { _, _ -> }
            setNeutralButton("Clear", null)
            create().apply {
                setOnShowListener {
                    getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener {
                        characterFilterMap.clear()
                        editTextList.forEach { it.text.clear() }
                        checkBoxList.forEach { it.isChecked = false }
                    }
                }
            }
        }.show()
    }

    private fun getCheckBoxes(filterLayout: View): List<CheckBox> {
        val checkBoxList = mutableListOf<CheckBox>()
        filterLayout.findViewById<ConstraintLayout>(R.id.constraint_layout).forEach {
            if (it is CheckBox) {
                checkBoxList.add(it)
            }
        }
        return checkBoxList
    }

    private fun groupCheckBoxes(checkBoxList: List<CheckBox>): List<List<CheckBox>> {
        val statusCheckBoxGroup = mutableListOf<CheckBox>()
        val genderCheckBoxGroup = mutableListOf<CheckBox>()
        val speciesCheckBoxGroup = mutableListOf<CheckBox>()

        checkBoxList.forEach {
            when (it.transitionName) {
                "status" -> statusCheckBoxGroup.add(it)
                "gender" -> genderCheckBoxGroup.add(it)
                "species" -> speciesCheckBoxGroup.add(it)
            }
        }
        return listOf(statusCheckBoxGroup, genderCheckBoxGroup, speciesCheckBoxGroup)
    }

    private fun preventMultipleCheckBoxSelections(checkBoxGroups: List<List<CheckBox>>) {
        checkBoxGroups.forEach { checkBoxGroup ->
            checkBoxGroup.forEach { checkBox ->
                checkBox.setOnClickListener {
                    if (checkBox.isChecked) {
                        checkBoxGroup.filterNot { it.id == checkBox.id }.onEach { it.isChecked = false }
                    }
                }
            }
        }
    }

    private fun restoreCheckboxesFlags(checkBoxList: List<CheckBox>) {
        checkBoxList.forEach { checkBox ->
            characterFilterMap.entries.forEach { filter ->
                if (checkBox.text == filter.value && checkBox.transitionName == filter.key) {
                    checkBox.isChecked = true
                }
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            R.id.filter -> {
                showFilterDialog()
                true
            }
            R.id.menu_refresh -> {
                pagingAdapter.refresh()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.filter_menu, menu)

        val filterItem = menu.findItem(R.id.filter)
        val searchItem = menu.findItem(R.id.search_action)
        val searchView = searchItem.actionView as SearchView
        val searchViewCloseButton = searchView.findViewById<ImageView>(androidx.appcompat.R.id.search_close_btn)

        changeFilterIcon(filterItem)

        searchViewCloseButton.setOnClickListener {
            searchView.apply {
                onActionViewCollapsed()
                searchItem.collapseActionView()
            }
            displayCharacters()
        }

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                if (newText.isNotBlank()) {
                    displayFoundCharacters(newText.lowercase())
                }
                return false
            }
        })
    }

    private fun changeFilterIcon(filterItem: MenuItem) {
        viewModel.queries.observe(viewLifecycleOwner) { filterMap ->
            var isFilterEmpty = true
            filterMap.values.forEach {
                if (it.isNotBlank() && it != "true" && it != "false") {
                    isFilterEmpty = false
                }
            }
            when(isFilterEmpty) {
                false -> filterItem.setIcon(R.drawable.ic_filter_list_off)
                true -> filterItem.setIcon(R.drawable.ic_filter)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
