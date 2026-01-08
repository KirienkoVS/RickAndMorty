package com.example.rickandmorty.data.mapper

import com.example.rickandmorty.data.datasource.db.entity.CharacterData
import com.example.rickandmorty.data.datasource.db.entity.EpisodeData
import com.example.rickandmorty.data.model.CharacterInfo
import com.example.rickandmorty.data.model.EpisodeResponse

fun CharacterInfo.toCharacterData(): CharacterData = CharacterData(
    id = id,
    name = name,
    species = species,
    status = status,
    gender = gender,
    image = image,
    type = type,
    created = created,
    originName = origin.name,
    locationName = location.name,
    episode = episode
)

fun EpisodeResponse.toEpisodeData(): EpisodeData = EpisodeData(
    id = id,
    name = name,
    airDate = airDate,
    episodeNumber = episode,
    characters = characters,
    url = url,
    created = created
)
