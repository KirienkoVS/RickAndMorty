package com.example.rickandmorty.domain.model

data class CharacterEntity(
    val id: Int,
    val name: String,
    val species: String,
    val status: String,
    val gender: String,
    val image: String,
    val type: String,
    val created: String,
    val originName: String,
    val locationName: String,
    val episode: List<String>
)
