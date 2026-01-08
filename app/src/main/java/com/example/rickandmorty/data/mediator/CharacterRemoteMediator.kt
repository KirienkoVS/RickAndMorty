package com.example.rickandmorty.data.mediator

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.example.rickandmorty.data.datasource.api.RickAndMortyApi
import com.example.rickandmorty.data.datasource.db.AppDatabase
import com.example.rickandmorty.data.datasource.db.entity.CharacterRemoteKeys
import com.example.rickandmorty.data.datasource.db.entity.CharacterData
import retrofit2.HttpException
import java.io.IOException

@OptIn(ExperimentalPagingApi::class)
class CharacterRemoteMediator(
    private val queries: Map<String, String>,
    private val api: RickAndMortyApi,
    private val database: AppDatabase
    ) : RemoteMediator<Int, CharacterData>() {

    override suspend fun initialize(): InitializeAction {
        return if (queries.get("isRefresh") == "true") {
            InitializeAction.LAUNCH_INITIAL_REFRESH
        } else InitializeAction.SKIP_INITIAL_REFRESH
    }

    override suspend fun load(loadType: LoadType, state: PagingState<Int, CharacterData>): MediatorResult {
        val page: Int = when(loadType) {
            LoadType.REFRESH -> {
                val remoteKeys = getRemoteKeyClosestToCurrentPosition(state)
                remoteKeys?.nextKey?.minus(1) ?: FIRST_PAGE_INDEX
            }
            LoadType.PREPEND -> {
                val remoteKeys = getRemoteKeyForFirstItem(state)
                remoteKeys?.prevKey ?: return MediatorResult.Success(endOfPaginationReached = remoteKeys != null)
            }
            LoadType.APPEND -> {
                val remoteKeys = getRemoteKeyForLastItem(state)
                remoteKeys?.nextKey ?: return MediatorResult.Success(endOfPaginationReached = remoteKeys != null)
            }
        }

        try {
            val response = api.requestCharacters(
                name = queries.get("name"),
                species = queries.get("species"),
                status = queries.get("status"),
                gender = queries.get("gender"),
                type = queries.get("type"),
                page
            )

            val charactersInfo = response.results
            val endOfPaginationReached = response.info.next == null

            database.withTransaction {

                if (loadType == LoadType.REFRESH) {
                    database.characterRemoteKeysDao().clearRemoteKeys()
                    database.characterDao().clearCharacters()
                }

                val prevKey = if (page == FIRST_PAGE_INDEX) null else page - 1
                val nextKey = if (endOfPaginationReached) null else page + 1

                val keys = charactersInfo.map {
                    CharacterRemoteKeys(characterId = it.id, prevKey = prevKey, nextKey = nextKey)
                }

                val characters = charactersInfo.map {
                    CharacterData(
                        id = it.id, name = it.name, species = it.species, status = it.status, gender = it.gender,
                        image = it.image, type = it.type, created = it.created, originName = it.origin.name,
                        locationName = it.location.name, episode = it.episode
                    )
                }

                database.characterRemoteKeysDao().insertKeys(keys)
                database.characterDao().insertCharacters(characters)
            }
            return MediatorResult.Success(endOfPaginationReached = endOfPaginationReached)
        } catch (exception: IOException) {
            return MediatorResult.Error(exception)
        } catch (exception: HttpException) {
            return MediatorResult.Error(exception)
        }
    }

    // LoadType.REFRESH
    private suspend fun getRemoteKeyClosestToCurrentPosition(state: PagingState<Int, CharacterData>): CharacterRemoteKeys? {
        return state.anchorPosition?.let { position ->
            state.closestItemToPosition(position)?.id?.let { characterId ->
                database.characterRemoteKeysDao().characterIdRemoteKeys(characterId)
            }
        }
    }

    // LoadType.PREPEND
    private suspend fun getRemoteKeyForFirstItem(state: PagingState<Int, CharacterData>): CharacterRemoteKeys? {
        return state.pages.firstOrNull { it.data.isNotEmpty() }?.data?.firstOrNull()
            ?.let { character ->
                database.characterRemoteKeysDao().characterIdRemoteKeys(character.id)
            }
    }

    // LoadType.APPEND
    private suspend fun getRemoteKeyForLastItem(state: PagingState<Int, CharacterData>): CharacterRemoteKeys? {
        return state.pages.lastOrNull { it.data.isNotEmpty() }?.data?.lastOrNull()
            ?.let { character ->
                database.characterRemoteKeysDao().characterIdRemoteKeys(character.id)
            }
    }

    companion object {
        private const val FIRST_PAGE_INDEX = 1
    }
}
