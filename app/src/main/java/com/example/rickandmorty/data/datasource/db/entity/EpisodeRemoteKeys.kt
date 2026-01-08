package com.example.rickandmorty.data.datasource.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "episodes_remote_keys")
data class EpisodeRemoteKeys(
    @PrimaryKey val episodeId: Int,
    val prevKey: Int?,
    val nextKey: Int?
)
