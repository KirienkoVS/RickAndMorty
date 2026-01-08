package com.example.rickandmorty.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import com.example.rickandmorty.domain.model.CharacterEntity
import com.example.rickandmorty.domain.model.LocationEntity
import com.example.rickandmorty.domain.model.ResponseResult
import com.example.rickandmorty.domain.repository.LocationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LocationViewModel @Inject constructor(
    private val repository: LocationRepository,
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

    fun requestLocations(queries: Map<String, String>): Flow<PagingData<LocationEntity>> {
        return repository.getLocations(queries)
    }
    /*--------------------------------------------------------------------------------------------------------------*/
    private val _locationDetails = MutableLiveData<ResponseResult<LocationEntity>>()
    val locationDetails: LiveData<ResponseResult<LocationEntity>> = _locationDetails

    fun requestLocationDetails(id: Int, name: String) {
        viewModelScope.launch {
            _locationDetails.value = repository.getLocationDetails(id, name)
        }
    }
    /*--------------------------------------------------------------------------------------------------------------*/
    private val _locationResidents = MutableLiveData<ResponseResult<List<CharacterEntity>>>()
    val locationResidents: LiveData<ResponseResult<List<CharacterEntity>>> = _locationResidents

    fun requestLocationResidents(characterUrlList: List<String>) {
        viewModelScope.launch {
            _locationResidents.value = repository.getLocationResidents(characterUrlList)
        }
    }
    /*--------------------------------------------------------------------------------------------------------------*/
    fun searchLocations(query: String): Flow<PagingData<LocationEntity>> {
        return repository.searchLocations(query)
    }
}
