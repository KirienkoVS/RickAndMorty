package com.example.rickandmorty.data.mapper

import com.example.rickandmorty.data.datasource.db.entity.CharacterData
import com.example.rickandmorty.data.datasource.db.entity.EpisodeData
import com.example.rickandmorty.data.datasource.db.entity.LocationData
import com.example.rickandmorty.domain.model.CharacterEntity
import com.example.rickandmorty.domain.model.EpisodeEntity
import com.example.rickandmorty.domain.model.LocationEntity

fun CharacterData.toEntity() = CharacterEntity(
    id = id,
    name = name,
    species = species,
    status = status,
    gender = gender,
    image = image,
    type = type,
    created = created,
    originName = originName,
    locationName = locationName,
    episode = episode
)

fun EpisodeData.toEntity() = EpisodeEntity(
    id = id,
    name = name,
    airDate = airDate,
    episodeNumber = episodeNumber,
    characters = characters,
    url = url,
    created = created
)

fun LocationData.toEntity() = LocationEntity(
    id = id,
    name = name,
    type = type,
    dimension = dimension,
    residents = residents,
    url = url,
    created = created
)
