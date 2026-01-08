package com.example.rickandmorty.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import com.example.rickandmorty.domain.model.CharacterEntity
import com.example.rickandmorty.domain.model.EpisodeEntity
import com.example.rickandmorty.domain.model.ResponseResult
import com.example.rickandmorty.domain.repository.EpisodeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EpisodeViewModel @Inject constructor(
    private val repository: EpisodeRepository,
) : ViewModel() {

    private val _isProgressBarVisible = MutableLiveData(true)
    val isProgressBarVisible: LiveData<Boolean> = _isProgressBarVisible

    fun setProgressBarVisibility(isVisible: Boolean) {
        _isProgressBarVisible.value = isVisible
    }
    /*--------------------------------------------------------------------------------------------------------------*/
    private val _queries = MutableLiveData(mutableMapOf("isRefresh" to "true"))
    val queries: LiveData<MutableMap<String, String>> = _queries

    fun setFilter(queries: MutableMap<String, String>) {
        _queries.value = queries
    }

    fun requestEpisodes(queries: Map<String, String>): Flow<PagingData<EpisodeEntity>> {
        return repository.getEpisodes(queries)
    }
    /*--------------------------------------------------------------------------------------------------------------*/
    private val _episodeDetails = MutableLiveData<EpisodeEntity>()
    val episodeDetails: LiveData<EpisodeEntity> = _episodeDetails

    fun requestEpisodeDetails(id: Int) {
        viewModelScope.launch {
            _episodeDetails.value = repository.getEpisodeDetails(id)
        }
    }
    /*--------------------------------------------------------------------------------------------------------------*/
    private val _episodeCharacters = MutableLiveData<ResponseResult<List<CharacterEntity>>>()
    val episodeCharacters: LiveData<ResponseResult<List<CharacterEntity>>> = _episodeCharacters

    fun requestEpisodeCharacters(characterUrlList: List<String>) {
        viewModelScope.launch {
            _episodeCharacters.value = repository.getEpisodeCharacters(characterUrlList)
        }
    }
    /*--------------------------------------------------------------------------------------------------------------*/
    fun searchEpisodes(query: String): Flow<PagingData<EpisodeEntity>> {
        return repository.searchEpisodes(query)
    }
}
