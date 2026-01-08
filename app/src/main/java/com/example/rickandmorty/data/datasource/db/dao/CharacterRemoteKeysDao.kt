package com.example.rickandmorty.data.datasource.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.rickandmorty.data.datasource.db.entity.CharacterRemoteKeys

@Dao
interface CharacterRemoteKeysDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertKeys(characterRemoteKeys: List<CharacterRemoteKeys>)

    @Query("SELECT * FROM characters_remote_keys WHERE characterId = :characterId")
    suspend fun characterIdRemoteKeys(characterId: Int): CharacterRemoteKeys?

    @Query("DELETE FROM characters_remote_keys")
    suspend fun clearRemoteKeys()
}
