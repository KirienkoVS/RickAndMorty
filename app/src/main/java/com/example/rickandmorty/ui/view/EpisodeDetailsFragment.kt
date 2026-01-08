package com.example.rickandmorty.ui.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.rickandmorty.databinding.EpisodeDetailsFragmentBinding
import com.example.rickandmorty.domain.model.EpisodeEntity
import com.example.rickandmorty.domain.model.ResponseResult
import com.example.rickandmorty.ui.adapter.EpisodeDetailsAdapter
import com.example.rickandmorty.ui.viewmodel.EpisodeViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EpisodeDetailsFragment : Fragment() {

    private var _binding: EpisodeDetailsFragmentBinding? = null
    private val binding get() = _binding!!

    private val viewModel: EpisodeViewModel by viewModels()

    private var episodeID = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?,
    ): View {
        _binding = EpisodeDetailsFragmentBinding.inflate(inflater, container, false)

        episodeID = arguments?.getInt(EPISODE_ID) ?: error("Should provide episode ID")
        viewModel.requestEpisodeDetails(episodeID)

        showProgressBar()
        displayEpisodeDetails()

        return binding.root
    }

    private fun displayEpisodeDetails() {
        viewModel.episodeDetails.observe(viewLifecycleOwner) { episode ->
            if (episode != null) {
                setUpViews(episode)
                setUpRecyclerView(episode)
            }
        }
    }

    private fun setUpViews(episode: EpisodeEntity) {
        with(binding) {
            episodeId.text = episode.id.toString()
            episodeName.text = episode.name
            episodeNumber.text = episode.episodeNumber
            episodeDate.text = episode.airDate
            episodeCreated.text = episode.created.subSequence(0, 10)
        }
    }

    private fun setUpRecyclerView(episode: EpisodeEntity) {
        viewModel.requestEpisodeCharacters(episode.characters)
        viewModel.episodeCharacters.observe(viewLifecycleOwner) { response ->
            when (response) {
                is ResponseResult.Success -> {
                    response.data?.let { characterList ->
                        val recyclerViewAdapter = EpisodeDetailsAdapter()
                        recyclerViewAdapter.characterList = characterList
                        binding.recyclerView.adapter = recyclerViewAdapter
                        viewModel.setProgressBarVisibility(false)
                    } ?: Toast.makeText(activity, "${response.message}", Toast.LENGTH_SHORT).show()
                }
                is ResponseResult.Error -> Toast.makeText(activity, "${response.message}", Toast.LENGTH_SHORT).show()
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

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    companion object {
        const val EPISODE_ID = "episodeID"
    }
}
