package com.example.rickandmorty.domain.model

data class EpisodeEntity(
    val id: Int,
    val name: String,
    val airDate: String,
    val episodeNumber: String,
    val characters: List<String>,
    val url: String,
    val created: String
)
