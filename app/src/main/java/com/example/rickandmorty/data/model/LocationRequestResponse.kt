package com.example.rickandmorty.data.model

import com.example.rickandmorty.data.datasource.db.entity.LocationData
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.Serializable

@OptIn(InternalSerializationApi::class)
@Serializable
data class LocationRequestResponse(
    val results: List<LocationData>,
    val info: LocationInfo
)

@OptIn(InternalSerializationApi::class)
@Serializable
data class LocationInfo(
    val next: String?,
    val prev: String?
)
