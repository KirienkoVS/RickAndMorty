package com.example.rickandmorty.ui.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.rickandmorty.R
import com.example.rickandmorty.databinding.LocationDetailsFragmentBinding
import com.example.rickandmorty.domain.model.LocationEntity
import com.example.rickandmorty.domain.model.ResponseResult
import com.example.rickandmorty.ui.adapter.LocationDetailsAdapter
import com.example.rickandmorty.ui.viewmodel.LocationViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LocationDetailsFragment : Fragment() {

    private var _binding: LocationDetailsFragmentBinding? = null
    private val binding get() = _binding!!

    private val viewModel: LocationViewModel by viewModels()

    private var locationID = 0
    private var locationName = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?,
    ): View {
        _binding = LocationDetailsFragmentBinding.inflate(inflater, container, false)

        arguments?.apply {
            locationID = this.getInt(LOCATION_ID)
            locationName = this.getString(LOCATION_NAME) ?: error("Should provide location name")
        }

        viewModel.requestLocationDetails(locationID, locationName)

        showProgressBar()
        displayLocationDetails()

        return binding.root
    }

    private fun displayLocationDetails() {
        viewModel.locationDetails.observe(viewLifecycleOwner) { response ->
            when (response) {
                is ResponseResult.Success -> {
                    response.data?.let { location ->
                        setUpViews(location)
                        setUpRecyclerView(location)
                    } ?: Toast.makeText(activity, "${response.message}", Toast.LENGTH_SHORT).show()
                }
                is ResponseResult.Error -> {
                    showInternetConnectionError()
                    retryGetLocationDetails()
                }
            }
        }
    }

    private fun setUpViews(location: LocationEntity) {
        with(binding) {
            locationId.text = location.id.toString()
            locationName.text = location.name
            locationType.text = location.type.ifBlank { "unknown" }
            locationDimension.text = location.dimension.ifBlank { "unknown" }
            locationCreated.text = location.created.subSequence(0, 10)
        }
    }

    private fun setUpRecyclerView(location: LocationEntity) {
        viewModel.requestLocationResidents(location.residents)
        viewModel.locationResidents.observe(viewLifecycleOwner) { response ->
            when (response) {
                is ResponseResult.Success -> {
                    response.data?.let { characterList ->
                        if (characterList.isNotEmpty()) {
                            val adapter = LocationDetailsAdapter()
                            adapter.residentsList = characterList
                            binding.recyclerView.adapter = adapter
                            viewModel.setProgressBarVisibility(false)
                        } else showEmptyLocationError()
                    } ?: Toast.makeText(activity, "${response.message}", Toast.LENGTH_SHORT).show()
                }
                is ResponseResult.Error -> {
                    showInternetConnectionError()
                    retryGetLocationResidents(location)
                }
            }
        }
    }

    private fun showProgressBar() {
        viewModel.isProgressBarVisible.observe(viewLifecycleOwner) { isVisible ->
            when (isVisible) {
                true -> binding.progressBar.visibility = View.VISIBLE
                false -> binding.progressBar.visibility = View.GONE
            }
        }
    }

    private fun showInternetConnectionError() {
        binding.emptyLocation.apply {
            visibility = View.VISIBLE
            text = resources.getString(R.string.no_results)
        }
        binding.retryButton.visibility = View.VISIBLE
        viewModel.setProgressBarVisibility(false)
    }

    private fun showEmptyLocationError() {
        binding.emptyLocation.apply {
            visibility = View.VISIBLE
            text = resources.getString(R.string.empty_location)
        }
        viewModel.setProgressBarVisibility(false)
    }

    private fun retryGetLocationDetails() {
        binding.retryButton.setOnClickListener {
            it.visibility = View.GONE
            binding.emptyLocation.visibility = View.GONE
            viewModel.setProgressBarVisibility(true)
            viewModel.requestLocationDetails(locationID, locationName)
        }
    }

    private fun retryGetLocationResidents(location: LocationEntity) {
        binding.retryButton.setOnClickListener {
            it.visibility = View.GONE
            binding.emptyLocation.visibility = View.GONE
            viewModel.setProgressBarVisibility(true)
            if (location.residents.isEmpty()) {
                showEmptyLocationError()
            } else viewModel.requestLocationResidents(location.residents)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    companion object {
        const val LOCATION_ID = "locationID"
        const val LOCATION_NAME = "locationName"
    }
}
