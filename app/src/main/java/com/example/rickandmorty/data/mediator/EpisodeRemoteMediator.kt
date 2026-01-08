package com.example.rickandmorty.data.mediator

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.example.rickandmorty.data.datasource.api.RickAndMortyApi
import com.example.rickandmorty.data.datasource.db.AppDatabase
import com.example.rickandmorty.data.datasource.db.entity.EpisodeRemoteKeys
import com.example.rickandmorty.data.datasource.db.entity.EpisodeData
import retrofit2.HttpException
import java.io.IOException

@OptIn(ExperimentalPagingApi::class)
class EpisodeRemoteMediator(
    private val queries: Map<String, String>,
    private val api: RickAndMortyApi,
    private val database: AppDatabase
) : RemoteMediator<Int, EpisodeData>() {

    override suspend fun initialize(): InitializeAction {
        return if (queries.get("isRefresh") == "true") {
            InitializeAction.LAUNCH_INITIAL_REFRESH
        } else InitializeAction.SKIP_INITIAL_REFRESH
    }

    override suspend fun load(loadType: LoadType, state: PagingState<Int, EpisodeData>): MediatorResult {
        val page: Int = when (loadType) {
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

            val response = api.requestEpisodes(
                name = queries.get("name"),
                episode = queries.get("episode"),
                page
            )

            val episodesInfo = response.results
            val endOfPaginationReached = response.info.next == null

            database.withTransaction {

                if (loadType == LoadType.REFRESH) {
                    database.episodeDao().clearEpisodes()
                    database.episodeRemoteKeysDao().clearRemoteKeys()
                }

                val prevKey = if (page == FIRST_PAGE_INDEX) null else page - 1
                val nextKey = if (endOfPaginationReached) null else page + 1

                val keys = episodesInfo.map {
                    EpisodeRemoteKeys(episodeId = it.id, prevKey = prevKey, nextKey = nextKey)
                }

                val episodes = episodesInfo.map {
                    EpisodeData(
                        id = it.id, name = it.name, airDate = it.airDate, episodeNumber = it.episode,
                        characters = it.characters, url = it.url, created = it.created,
                    )
                }

                database.episodeDao().insertEpisodes(episodes)
                database.episodeRemoteKeysDao().insertKeys(keys)
            }
            return MediatorResult.Success(endOfPaginationReached = endOfPaginationReached)
        } catch (exception: IOException) {
            return MediatorResult.Error(exception)
        } catch (exception: HttpException) {
            return MediatorResult.Error(exception)
        }
    }

    // LoadType.REFRESH
    private suspend fun getRemoteKeyClosestToCurrentPosition(state: PagingState<Int, EpisodeData>): EpisodeRemoteKeys? {
        return state.anchorPosition?.let { position ->
            state.closestItemToPosition(position)?.id?.let { episodeId ->
                database.episodeRemoteKeysDao().episodeIdRemoteKeys(episodeId)
            }
        }
    }

    // LoadType.PREPEND
    private suspend fun getRemoteKeyForFirstItem(state: PagingState<Int, EpisodeData>): EpisodeRemoteKeys? {
        return state.pages.firstOrNull { it.data.isNotEmpty() }?.data?.firstOrNull()
            ?.let { episode ->
                database.episodeRemoteKeysDao().episodeIdRemoteKeys(episode.id)
            }
    }

    // LoadType.APPEND
    private suspend fun getRemoteKeyForLastItem(state: PagingState<Int, EpisodeData>): EpisodeRemoteKeys? {
        return state.pages.lastOrNull { it.data.isNotEmpty() }?.data?.lastOrNull()
            ?.let { episode ->
                database.episodeRemoteKeysDao().episodeIdRemoteKeys(episode.id)
            }
    }

    companion object {
        private const val FIRST_PAGE_INDEX = 1
    }

}
