package com.example.rickandmorty.data.datasource.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "locations_remote_keys")
data class LocationRemoteKeys(
    @PrimaryKey val locationId: Int,
    val prevKey: Int?,
    val nextKey: Int?
)
