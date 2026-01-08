package com.example.rickandmorty.data.datasource.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.rickandmorty.data.datasource.db.entity.LocationRemoteKeys

@Dao
interface LocationRemoteKeysDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertKeys(locationRemoteKeys: List<LocationRemoteKeys>)

    @Query("SELECT * FROM locations_remote_keys WHERE locationId = :locationId")
    suspend fun locationIdIdRemoteKeys(locationId: Int): LocationRemoteKeys?

    @Query("DELETE FROM locations_remote_keys")
    suspend fun clearRemoteKeys()
}
