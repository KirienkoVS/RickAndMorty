package com.example.rickandmorty.data.datasource.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.rickandmorty.data.datasource.db.dao.CharacterDao
import com.example.rickandmorty.data.datasource.db.entity.CharacterRemoteKeys
import com.example.rickandmorty.data.datasource.db.dao.CharacterRemoteKeysDao
import com.example.rickandmorty.data.datasource.db.dao.EpisodeDao
import com.example.rickandmorty.data.datasource.db.entity.EpisodeRemoteKeys
import com.example.rickandmorty.data.datasource.db.dao.EpisodeRemoteKeysDao
import com.example.rickandmorty.data.datasource.db.dao.LocationDao
import com.example.rickandmorty.data.datasource.db.entity.LocationRemoteKeys
import com.example.rickandmorty.data.datasource.db.dao.LocationRemoteKeysDao
import com.example.rickandmorty.data.datasource.db.entity.CharacterData
import com.example.rickandmorty.data.datasource.db.entity.EpisodeData
import com.example.rickandmorty.data.datasource.db.entity.LocationData

@Database(
    entities = [
        CharacterData::class, CharacterRemoteKeys::class,
        EpisodeData::class, EpisodeRemoteKeys::class,
        LocationData::class, LocationRemoteKeys::class
               ], version = 1, exportSchema = false
)

@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun characterDao(): CharacterDao
    abstract fun characterRemoteKeysDao(): CharacterRemoteKeysDao

    abstract fun episodeDao(): EpisodeDao
    abstract fun episodeRemoteKeysDao(): EpisodeRemoteKeysDao

    abstract fun locationDao(): LocationDao
    abstract fun locationRemoteKeysDao(): LocationRemoteKeysDao
}
