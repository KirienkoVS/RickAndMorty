package com.example.rickandmorty.data.characters

import androidx.paging.*
import com.example.rickandmorty.api.RickAndMortyApi
import com.example.rickandmorty.data.ResponseResult
import com.example.rickandmorty.db.AppDatabase
import com.example.rickandmorty.model.CharacterData
import com.example.rickandmorty.model.EpisodeData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

@OptIn(ExperimentalPagingApi::class)
class CharacterRepository @Inject constructor(
    private val api: RickAndMortyApi,
    private val database: AppDatabase,
) {

    fun getCharacters(queries: Map<String, String>): Flow<PagingData<CharacterData>> {

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
        ).flow

    }

    suspend fun getCharacterDetails(id: Int): CharacterData {
        return withContext(Dispatchers.IO) {
            database.characterDao().getCharacterDetails(id)
        }
    }

    suspend fun getCharacterEpisodes(episodeUrlList: List<String>): ResponseResult<List<EpisodeData>> {
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
                        val episodeData = response.map {
                            EpisodeData(id = it.id, name = it.name, airDate = it.airDate, episodeNumber = it.episode,
                                characters = it.characters, url = it.url, created = it.created)
                        }
                        database.episodeDao().insertEpisodes(episodeData)
                        ResponseResult.Success(episodeData)
                    } ?: ResponseResult.Error(apiResponse.message())
                } else ResponseResult.Error(apiResponse.message())
            } else ResponseResult.Success(dbResponse)
        } catch (exception: Exception) {
            ResponseResult.Error(exception.message)
        }
    }

    fun searchCharacters(query: String): Flow<PagingData<CharacterData>> {
        val dbQuery = if (query.isBlank()) "empty" else "%${query}%"
        fun pagingSourceFactory(): () -> PagingSource<Int, CharacterData> {
            return {
                database.characterDao().charactersBySearch(query = if (dbQuery == "empty") null else dbQuery)
            }
        }
        return Pager(
            config = PagingConfig(pageSize = PAGE_SIZE),
            pagingSourceFactory = pagingSourceFactory(),
        ).flow
    }

    companion object {
        const val PAGE_SIZE = 20
    }
}
