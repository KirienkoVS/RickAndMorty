package com.example.rickandmorty.api

import com.example.rickandmorty.model.LocationData
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
