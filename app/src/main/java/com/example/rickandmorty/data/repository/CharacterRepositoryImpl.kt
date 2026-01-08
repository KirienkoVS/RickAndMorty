package com.example.rickandmorty.data.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.map
import com.example.rickandmorty.data.datasource.api.RickAndMortyApi
import com.example.rickandmorty.data.datasource.db.AppDatabase
import com.example.rickandmorty.data.datasource.db.entity.CharacterData
import com.example.rickandmorty.data.mapper.toEntity
import com.example.rickandmorty.data.mapper.toEpisodeData
import com.example.rickandmorty.data.mediator.CharacterRemoteMediator
import com.example.rickandmorty.domain.model.CharacterEntity
import com.example.rickandmorty.domain.model.EpisodeEntity
import com.example.rickandmorty.domain.model.ResponseResult
import com.example.rickandmorty.domain.repository.CharacterRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

@OptIn(ExperimentalPagingApi::class)
class CharacterRepositoryImpl @Inject constructor(
    private val api: RickAndMortyApi,
    private val database: AppDatabase,
) : CharacterRepository {

    override fun getCharacters(queries: Map<String, String>): Flow<PagingData<CharacterEntity>> {

        val name = if (queries.get("name").isNullOrBlank()) "empty" else "%${queries.get("name")}%"
        val type = if (queries.get("type").isNullOrBlank()) "empty" else "%${queries.get("type")}%"
        val species = if (queries.get("species").isNullOrBlank()) "empty" else queries.get("species")
        val status = if (queries.get("status").isNullOrBlank()) "empty" else queries.get("status")
        val gender = if (queries.get("gender").isNullOrBlank()) "empty" else queries.get("gender")

        fun pagingSourceFactory(): () -> PagingSource<Int, CharacterData> {
            return {
                database.characterDao().charactersByFilter(
                    name = if (name == "empty") null else name,
                    species = if (species == "empty") null else species,
                    status = if (status == "empty") null else status,
                    gender = if (gender == "empty") null else gender,
                    type = if (type == "empty") null else type)
            }
        }

        return Pager(
            config = PagingConfig(pageSize = PAGE_SIZE),
            pagingSourceFactory = pagingSourceFactory(),
            remoteMediator = CharacterRemoteMediator(queries, api, database)
        ).flow.map { pagingData -> pagingData.map { it.toEntity() } }

    }

    override suspend fun getCharacterDetails(id: Int): CharacterEntity {
        return withContext(Dispatchers.IO) {
            database.characterDao().getCharacterDetails(id).toEntity()
        }
    }

    override suspend fun getCharacterEpisodes(episodeUrlList: List<String>): ResponseResult<List<EpisodeEntity>> {
        var apiQuery = ""
        val dbQuery = mutableListOf<Int>()

        episodeUrlList.forEach { episodeUrl ->
            val episodeID = episodeUrl.substringAfterLast("/").toInt()
            apiQuery += "$episodeID,"
            dbQuery.add(episodeID)
        }

        return try {
            val dbResponse = withContext(Dispatchers.IO) { database.episodeDao().getCharacterEpisodes(dbQuery) }
            if (dbResponse.isEmpty()) {
                val apiResponse = api.requestSingleEpisode(apiQuery)
                if (apiResponse.isSuccessful) {
                    apiResponse.body()?.let { response ->
                        val episodeData = response.map { it.toEpisodeData() }
                        database.episodeDao().insertEpisodes(episodeData)
                        ResponseResult.Success(episodeData.map {  it.toEntity() })
                    } ?: ResponseResult.Error(apiResponse.message())
                } else ResponseResult.Error(apiResponse.message())
            } else ResponseResult.Success(dbResponse.map { it.toEntity() })
        } catch (exception: Exception) {
            ResponseResult.Error(exception.message)
        }
    }

    override fun searchCharacters(query: String): Flow<PagingData<CharacterEntity>> {
        val dbQuery = if (query.isBlank()) "empty" else "%${query}%"
        fun pagingSourceFactory(): () -> PagingSource<Int, CharacterData> {
            return {
                database.characterDao().charactersBySearch(query = if (dbQuery == "empty") null else dbQuery)
            }
        }
        return Pager(
            config = PagingConfig(pageSize = PAGE_SIZE),
            pagingSourceFactory = pagingSourceFactory(),
        ).flow.map { pagingData -> pagingData.map { it.toEntity() } }
    }

    companion object {
        const val PAGE_SIZE = 20
    }
}
