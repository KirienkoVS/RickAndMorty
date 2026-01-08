package com.example.rickandmorty.api

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@OptIn(InternalSerializationApi::class)
@Serializable
data class EpisodeRequestResponse(
    val results: List<EpisodeResponse>,
    val info: EpisodeInfo
)

@OptIn(InternalSerializationApi::class)
@Serializable
data class EpisodeResponse(
    val id: Int,
    val name: String,
    @SerialName("air_date")
    val airDate: String,
    val episode: String,
    val characters: List<String>,
    val url: String,
    val created: String
)

@OptIn(InternalSerializationApi::class)
@Serializable
data class EpisodeInfo(
    val next: String?,
    val prev: String?
)
