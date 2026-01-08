package com.example.rickandmorty.data.datasource.db.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.rickandmorty.data.datasource.db.entity.EpisodeData

@Dao
interface EpisodeDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEpisodes(episodeData: List<EpisodeData>)

    @Query("SELECT * FROM episodes WHERE " +
            "(name LIKE :name OR :name IS NULL) AND " +
            "(episodeNumber LIKE :episode OR :episode IS NULL)")
    fun episodesByFilter(name: String?, episode: String?): PagingSource<Int, EpisodeData>

    @Query("SELECT * FROM episodes WHERE id = :id")
    fun getEpisodeDetails(id: Int): EpisodeData

    @Query("SELECT * FROM episodes WHERE id IN (:id)")
    fun getCharacterEpisodes(id: List<Int>): List<EpisodeData>

    @Query("DELETE FROM episodes")
    suspend fun clearEpisodes()

    @Query("SELECT * FROM episodes WHERE " +
            "(name LIKE :query OR :query IS NULL) OR " +
            "(episodeNumber LIKE :query OR :query IS NULL)")
    fun episodesBySearch(query: String?): PagingSource<Int, EpisodeData>

}
