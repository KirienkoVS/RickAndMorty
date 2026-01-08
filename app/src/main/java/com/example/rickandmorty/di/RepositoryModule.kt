package com.example.rickandmorty.di

import com.example.rickandmorty.data.repository.CharacterRepositoryImpl
import com.example.rickandmorty.data.repository.EpisodeRepositoryImpl
import com.example.rickandmorty.data.repository.LocationRepositoryImpl
import com.example.rickandmorty.domain.repository.CharacterRepository
import com.example.rickandmorty.domain.repository.EpisodeRepository
import com.example.rickandmorty.domain.repository.LocationRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun bindCharacterRepository(impl: CharacterRepositoryImpl): CharacterRepository

    @Binds
    abstract fun bindEpisodeRepository(impl: EpisodeRepositoryImpl): EpisodeRepository

    @Binds
    abstract fun bindLocationRepository(impl: LocationRepositoryImpl): LocationRepository
}
