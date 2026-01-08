package com.example.rickandmorty.domain.repository

import androidx.paging.PagingData
import com.example.rickandmorty.domain.model.CharacterEntity
import com.example.rickandmorty.domain.model.LocationEntity
import com.example.rickandmorty.domain.model.ResponseResult
import kotlinx.coroutines.flow.Flow

interface LocationRepository {

    fun getLocations(queries: Map<String, String>): Flow<PagingData<LocationEntity>>

    suspend fun getLocationDetails(id: Int, name: String): ResponseResult<LocationEntity>

    suspend fun getLocationResidents(residentUrlList: List<String>): ResponseResult<List<CharacterEntity>>

    fun searchLocations(query: String): Flow<PagingData<LocationEntity>>
}
