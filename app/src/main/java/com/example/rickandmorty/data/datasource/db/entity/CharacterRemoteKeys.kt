package com.example.rickandmorty.data.datasource.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "characters_remote_keys")
data class CharacterRemoteKeys(
    @PrimaryKey val characterId: Int,
    val prevKey: Int?,
    val nextKey: Int?
)
