package com.example.rickandmorty.domain.repository

import androidx.paging.PagingData
import com.example.rickandmorty.domain.model.CharacterEntity
import com.example.rickandmorty.domain.model.EpisodeEntity
import com.example.rickandmorty.domain.model.ResponseResult
import kotlinx.coroutines.flow.Flow

interface EpisodeRepository {

    fun getEpisodes(queries: Map<String, String>): Flow<PagingData<EpisodeEntity>>

    suspend fun getEpisodeDetails(id: Int): EpisodeEntity

    suspend fun getEpisodeCharacters(characterUrlList: List<String>): ResponseResult<List<CharacterEntity>>

    fun searchEpisodes(query: String): Flow<PagingData<EpisodeEntity>>
}
