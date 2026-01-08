package com.example.rickandmorty.data.datasource.db

import androidx.room.TypeConverter
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class Converters {

    @TypeConverter
    fun listToString(list: List<String>): String {
        return Json.encodeToString(list)
    }

    @TypeConverter
    fun stringToList(string: String): List<String> {
        return Json.decodeFromString(string)
    }
}
