package com.example.rickandmorty.domain.repository

import androidx.paging.PagingData
import com.example.rickandmorty.domain.model.CharacterEntity
import com.example.rickandmorty.domain.model.EpisodeEntity
import com.example.rickandmorty.domain.model.ResponseResult
import kotlinx.coroutines.flow.Flow

interface CharacterRepository {

    fun getCharacters(queries: Map<String, String>): Flow<PagingData<CharacterEntity>>

    suspend fun getCharacterDetails(id: Int): CharacterEntity

    suspend fun getCharacterEpisodes(episodeUrlList: List<String>): ResponseResult<List<EpisodeEntity>>

    fun searchCharacters(query: String): Flow<PagingData<CharacterEntity>>
}
