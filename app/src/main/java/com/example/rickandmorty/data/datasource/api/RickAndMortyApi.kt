package com.example.rickandmorty.data.datasource.api

import com.example.rickandmorty.data.model.CharacterInfo
import com.example.rickandmorty.data.model.CharacterRequestResponse
import com.example.rickandmorty.data.model.EpisodeRequestResponse
import com.example.rickandmorty.data.model.EpisodeResponse
import com.example.rickandmorty.data.model.LocationRequestResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface RickAndMortyApi {

    @GET("character/")
    suspend fun requestCharacters(
        @Query("name") name: String?,
        @Query("species") species: String?,
        @Query("status") status: String?,
        @Query("gender") gender: String?,
        @Query("type") type: String?,
        @Query("page") page: Int?
    ): CharacterRequestResponse

    @GET("character/{id}")
    suspend fun requestSingleCharacter(
        @Path("id") id: String
    ): Response<List<CharacterInfo>>


    @GET("episode/")
    suspend fun requestEpisodes(
        @Query("name") name: String?,
        @Query("episode") episode: String?,
        @Query("page") page: Int?
    ): EpisodeRequestResponse

    @GET("episode/{id}")
    suspend fun requestSingleEpisode(
        @Path("id") id: String
    ): Response<List<EpisodeResponse>>


    @GET("location/")
    suspend fun requestLocations(
        @Query("name") name: String?,
        @Query("type") type: String?,
        @Query("dimension") dimension: String?,
        @Query("page") page: Int?
    ): Response<LocationRequestResponse>
}
