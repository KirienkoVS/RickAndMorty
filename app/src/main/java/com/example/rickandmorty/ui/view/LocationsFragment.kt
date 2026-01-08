package com.example.rickandmorty.ui.view

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.rickandmorty.R
import com.example.rickandmorty.databinding.LocationsFragmentBinding
import com.example.rickandmorty.ui.utils.initLoadStateAdapter
import com.example.rickandmorty.ui.utils.isOnline
import com.example.rickandmorty.ui.utils.restoreEditTextText
import com.example.rickandmorty.ui.adapter.LocationPagingAdapter
import com.example.rickandmorty.ui.viewmodel.LocationViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LocationsFragment : Fragment()  {

    private var _binding: LocationsFragmentBinding? = null
    private val binding get() = _binding!!

    private val viewModel: LocationViewModel by viewModels()

    private lateinit var recyclerView: RecyclerView
    private lateinit var pagingAdapter: LocationPagingAdapter
    private lateinit var locationFilterMap: MutableMap<String, String>
    private lateinit var emptyTextView: TextView
    private lateinit var swipeRefresh: SwipeRefreshLayout

    private var isOnline = true

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d("LocationsFragment", "onCreate")
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        locationFilterMap = mutableMapOf()
        locationFilterMap.put("isRefresh", "true")
        viewModel.setFilter(locationFilterMap)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        Log.d("LocationsFragment", "onCreateView")
        _binding = LocationsFragmentBinding.inflate(inflater, container, false)

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
        locationFilterMap.put("isRefresh", "false")
        viewModel.setFilter(locationFilterMap)
    }

    private fun bindViews() {
        recyclerView = binding.locationRecyclerview
        emptyTextView = binding.emptyTextView
        swipeRefresh = binding.swipeRefresh
    }

    private fun initPagingAdapter() {
        pagingAdapter = LocationPagingAdapter()
        displayLocations()
    }

    private fun initSwipeToRefresh() {
        binding.swipeRefresh.setOnRefreshListener { pagingAdapter.refresh() }
    }

    private fun displayLocations() {
        viewModel.queries.observe(viewLifecycleOwner) { queries ->
            viewLifecycleOwner.lifecycleScope.launch {
                viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                    viewModel.requestLocations(queries).collectLatest {
                        pagingAdapter.submitData(it)
                    }
                }
            }
        }
    }

    private fun displayFoundLocations(search: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                viewModel.searchLocations(search).collectLatest {
                    pagingAdapter.submitData(it)
                }
            }
        }
    }

    @SuppressLint("InflateParams")
    private fun showFilterDialog() {
        val inflater = requireActivity().layoutInflater
        val filterLayout = inflater.inflate(R.layout.location_filter, null)
        val customTitle = inflater.inflate(R.layout.dialog_title, null)
        val nameEditText = filterLayout.findViewById<EditText>(R.id.location_name_edit_text)
        val typeEditText = filterLayout.findViewById<EditText>(R.id.location_type_edit_text)
        val dimensionEditText = filterLayout.findViewById<EditText>(R.id.location_dimension_edit_text)
        val dialog = MaterialAlertDialogBuilder(requireContext())

        val editTextList = mutableListOf<EditText>(nameEditText, typeEditText, dimensionEditText)
        restoreEditTextText(editTextList, locationFilterMap)

        // dialog builder
        with(dialog) {
            setView(filterLayout)
            setCustomTitle(customTitle)
            setPositiveButton("Apply") { _, _ ->
                editTextList.filter { it.text.isNotBlank() }.forEach {
                    locationFilterMap.put(it.transitionName, it.text.toString())
                }
                locationFilterMap.put("isRefresh", "true")
                viewModel.setFilter(locationFilterMap)
            }
            setNegativeButton("Cancel") { _, _ -> }
            setNeutralButton("Clear", null)
            create().apply {
                setOnShowListener {
                    getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener {
                        locationFilterMap.clear()
                        editTextList.forEach { it.text.clear() }
                    }
                }
            }
        }.show()
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
            displayLocations()
        }

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                if (newText.isNotBlank()) {
                    displayFoundLocations(newText.lowercase())
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
                true -> filterItem.setIcon(R.drawable.ic_filter)
                false -> filterItem.setIcon(R.drawable.ic_filter_list_off)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
