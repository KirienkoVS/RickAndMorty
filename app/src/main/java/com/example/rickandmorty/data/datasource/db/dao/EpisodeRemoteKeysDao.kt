package com.example.rickandmorty.data.datasource.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.rickandmorty.data.datasource.db.entity.EpisodeRemoteKeys

@Dao
interface EpisodeRemoteKeysDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertKeys(episodeRemoteKeys: List<EpisodeRemoteKeys>)

    @Query("SELECT * FROM episodes_remote_keys WHERE episodeId = :episodeId")
    suspend fun episodeIdRemoteKeys(episodeId: Int): EpisodeRemoteKeys?

    @Query("DELETE FROM episodes_remote_keys")
    suspend fun clearRemoteKeys()
}
