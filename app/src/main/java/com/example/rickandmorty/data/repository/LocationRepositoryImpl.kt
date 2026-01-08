package com.example.rickandmorty.data.repository

import android.util.Log
import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.map
import com.example.rickandmorty.data.datasource.api.RickAndMortyApi
import com.example.rickandmorty.data.datasource.db.AppDatabase
import com.example.rickandmorty.data.datasource.db.entity.LocationData
import com.example.rickandmorty.data.mapper.toCharacterData
import com.example.rickandmorty.data.mapper.toEntity
import com.example.rickandmorty.data.mediator.LocationRemoteMediator
import com.example.rickandmorty.domain.model.CharacterEntity
import com.example.rickandmorty.domain.model.LocationEntity
import com.example.rickandmorty.domain.model.ResponseResult
import com.example.rickandmorty.domain.repository.LocationRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

@OptIn(ExperimentalPagingApi::class)
class LocationRepositoryImpl @Inject constructor(
    private val api: RickAndMortyApi,
    private val database: AppDatabase,
) : LocationRepository {

    override fun getLocations(queries: Map<String, String>): Flow<PagingData<LocationEntity>> {

        val name = if (queries.get("name").isNullOrBlank()) "empty" else "%${queries.get("name")}%"
        val type = if (queries.get("type").isNullOrBlank()) "empty" else "%${queries.get("type")}%"
        val dimension = if (queries.get("dimension").isNullOrBlank()) "empty" else "%${queries.get("dimension")}%"

        fun pagingSourceFactory(): () -> PagingSource<Int, LocationData> {
            return {
                database.locationDao().locationsByFilter(
                    name = if (name == "empty") null else name,
                    type = if (type == "empty") null else type,
                    dimension = if (dimension == "empty") null else dimension
                )
            }
        }

        return Pager(
            config = PagingConfig(pageSize = PAGE_SIZE),
            pagingSourceFactory = pagingSourceFactory(),
            remoteMediator = LocationRemoteMediator(queries, api, database)
        ).flow.map { pagingData -> pagingData.map { it.toEntity() } }

    }

    override suspend fun getLocationDetails(id: Int, name: String): ResponseResult<LocationEntity> {
        return try {
            val dbResponse = withContext(Dispatchers.IO) {
                database.locationDao().getLocationDetails(id, name)
            }
            if (dbResponse == null) {
                val apiResponse = api.requestLocations(name = name, type = "", dimension = "", page = 0)
                if (apiResponse.isSuccessful) {
                    apiResponse.body()?.let {
                        database.locationDao().insertLocations(it.results)
                        ResponseResult.Success(data = it.results.first().toEntity())
                    } ?: ResponseResult.Error(apiResponse.message())
                } else ResponseResult.Error(apiResponse.message())
            } else {
                ResponseResult.Success(dbResponse.toEntity())
            }
        } catch (exception: Exception) {
            ResponseResult.Error(exception.message)
        }
    }

    override suspend fun getLocationResidents(residentUrlList: List<String>): ResponseResult<List<CharacterEntity>> {
        Log.d("LocationRepository", "residentUrlList: $residentUrlList")
        var apiQuery = ""
        val dbQuery = mutableListOf<Int>()

        residentUrlList.forEach { residentUrl ->
            val residentID = residentUrl.substringAfterLast("/").toInt()
            apiQuery += "$residentID,"
            dbQuery.add(residentID)
        }

        return try {
            val dbResponse = withContext(Dispatchers.IO) {
                database.characterDao().getLocationOrEpisodeCharacters(dbQuery)
            }
            Log.d("LocationRepository", "dbResponse: $dbResponse")
            if (dbResponse.isEmpty()) {
                val apiResponse = api.requestSingleCharacter(apiQuery)
                if (apiResponse.isSuccessful) {
                    apiResponse.body()?.let { response ->
                        val characterData = response.map { it.toCharacterData() }
                        database.characterDao().insertCharacters(characterData)
                        Log.d("LocationRepository", "apiResponse: $apiResponse")
                        ResponseResult.Success(characterData.map { it.toEntity() })
                    } ?: ResponseResult.Error(apiResponse.message())
                } else ResponseResult.Error(apiResponse.message())
            } else {
                ResponseResult.Success(dbResponse.map { it.toEntity() })
            }
        } catch (exception: Exception) {
            ResponseResult.Error(exception.message)
        }
    }

    override fun searchLocations(query: String): Flow<PagingData<LocationEntity>> {
        val dbQuery = if (query.isBlank()) "empty" else "%${query}%"
        fun pagingSourceFactory(): () -> PagingSource<Int, LocationData> {
            return {
                database.locationDao().locationsBySearch(query = if (dbQuery == "empty") null else dbQuery)
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
