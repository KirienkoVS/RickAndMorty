package com.example.rickandmorty.di

import android.content.Context
import androidx.room.Room
import com.example.rickandmorty.data.datasource.db.AppDatabase
import com.example.rickandmorty.data.datasource.db.dao.CharacterDao
import com.example.rickandmorty.data.datasource.db.dao.CharacterRemoteKeysDao
import com.example.rickandmorty.data.datasource.db.dao.EpisodeDao
import com.example.rickandmorty.data.datasource.db.dao.EpisodeRemoteKeysDao
import com.example.rickandmorty.data.datasource.db.dao.LocationDao
import com.example.rickandmorty.data.datasource.db.dao.LocationRemoteKeysDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    fun provideCharacterDao(database: AppDatabase): CharacterDao {
        return database.characterDao()
    }
    @Provides
    fun characterRemoteKeysDao(database: AppDatabase): CharacterRemoteKeysDao {
        return database.characterRemoteKeysDao()
    }


    @Provides
    fun provideEpisodeDao(database: AppDatabase): EpisodeDao {
        return database.episodeDao()
    }
    @Provides
    fun episodeRemoteKeysDao(database: AppDatabase): EpisodeRemoteKeysDao {
        return database.episodeRemoteKeysDao()
    }


    @Provides
    fun provideLocationDao(database: AppDatabase): LocationDao {
        return database.locationDao()
    }
    @Provides
    fun locationRemoteKeysDao(database: AppDatabase): LocationRemoteKeysDao {
        return database.locationRemoteKeysDao()
    }


    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "AppDatabase.db"
        ).build()
    }
}
