package com.example.rickandmorty.data.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.map
import com.example.rickandmorty.data.datasource.api.RickAndMortyApi
import com.example.rickandmorty.data.datasource.db.AppDatabase
import com.example.rickandmorty.data.datasource.db.entity.EpisodeData
import com.example.rickandmorty.data.mapper.toCharacterData
import com.example.rickandmorty.data.mapper.toEntity
import com.example.rickandmorty.data.mediator.EpisodeRemoteMediator
import com.example.rickandmorty.domain.model.CharacterEntity
import com.example.rickandmorty.domain.model.EpisodeEntity
import com.example.rickandmorty.domain.model.ResponseResult
import com.example.rickandmorty.domain.repository.EpisodeRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

@OptIn(ExperimentalPagingApi::class)
class EpisodeRepositoryImpl @Inject constructor(
    private val api: RickAndMortyApi,
    private val database: AppDatabase,
) : EpisodeRepository {

    override fun getEpisodes(queries: Map<String, String>): Flow<PagingData<EpisodeEntity>> {

        val name = if (queries.get("name").isNullOrBlank()) "empty" else "%${queries.get("name")}%"
        val episode = if (queries.get("episode").isNullOrBlank()) "empty" else "%${queries.get("episode")}%"

        fun pagingSourceFactory(): () -> PagingSource<Int, EpisodeData> {
            return {
                database.episodeDao().episodesByFilter(
                    name = if (name == "empty") null else name,
                    episode = if (episode == "empty") null else episode)
            }
        }

        return Pager(
            config = PagingConfig(pageSize = PAGE_SIZE),
            pagingSourceFactory = pagingSourceFactory(),
            remoteMediator = EpisodeRemoteMediator(queries, api, database)
        ).flow.map { pagingData -> pagingData.map { it.toEntity() } }

    }

    override suspend fun getEpisodeDetails(id: Int): EpisodeEntity {
        return withContext(Dispatchers.IO) {
            database.episodeDao().getEpisodeDetails(id).toEntity()
        }
    }

    override suspend fun getEpisodeCharacters(characterUrlList: List<String>): ResponseResult<List<CharacterEntity>> {
        var apiQuery = ""
        val dbQuery = mutableListOf<Int>()

        characterUrlList.forEach { characterUrl ->
            val characterID = characterUrl.substringAfterLast("/").toInt()
            apiQuery += "$characterID,"
            dbQuery.add(characterID)
        }

        return try {
            val dbResponse = withContext(Dispatchers.IO) {
                database.characterDao().getLocationOrEpisodeCharacters(dbQuery)
            }
            if (dbResponse.isEmpty()) {
                val apiResponse = api.requestSingleCharacter(apiQuery)
                if (apiResponse.isSuccessful) {
                    apiResponse.body()?.let { response ->
                        val characterData = response.map { it.toCharacterData() }
                        database.characterDao().insertCharacters(characterData)
                        ResponseResult.Success(characterData.map { it.toEntity() })
                    } ?: ResponseResult.Error(apiResponse.message())
                } else ResponseResult.Error(apiResponse.message())
            } else { ResponseResult.Success(dbResponse.map { it.toEntity() }) }
        } catch (exception: Exception) { ResponseResult.Error(exception.message) }
    }

    override fun searchEpisodes(query: String): Flow<PagingData<EpisodeEntity>> {
        val dbQuery = if (query.isBlank()) "empty" else "%${query}%"
        fun pagingSourceFactory(): () -> PagingSource<Int, EpisodeData> {
            return {
                database.episodeDao().episodesBySearch(query = if (dbQuery == "empty") null else dbQuery)
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
