package com.example.rickandmorty.api

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.Serializable

@OptIn(InternalSerializationApi::class)
@Serializable
data class CharacterRequestResponse(
    val results: List<CharacterInfo>,
    val info: Info
)

@OptIn(InternalSerializationApi::class)
@Serializable
data class CharacterInfo(
    val id: Int,
    val name: String,
    val species: String,
    val status: String,
    val gender: String,
    val image: String,
    val type: String,
    val url: String,
    val created: String,
    val origin: Origin,
    val location: Location,
    val episode: List<String>
)

@OptIn(InternalSerializationApi::class)
@Serializable
data class Info(
    val next: String?,
    val prev: String?
)

@OptIn(InternalSerializationApi::class)
@Serializable
data class Origin(
    val name: String,
    val url: String
)

@OptIn(InternalSerializationApi::class)
@Serializable
data class Location(
    val name: String,
    val url: String
)
