package com.rrameshbtech.micromoves.data.local

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class MicroMovesDBConverters {
    @TypeConverter
    fun fromStringList(value: List<String>?): String? {
        return Gson().toJson(value)
    }

    @TypeConverter
    fun toStringList(value: String?): List<String>? {
        val listType = object : TypeToken<List<String>>() {}.type
        // Ensure you handle empty/null cases to avoid crashes
        return Gson().fromJson(value, listType) ?: emptyList()
    }

    @TypeConverter
    fun fromLongList(value: List<Long>?): String? {
        return Gson().toJson(value)
    }

    @TypeConverter
    fun toLongList(value: String?): List<Long>? {
        val listType = object : TypeToken<List<Long>>() {}.type
        // Ensure you handle empty/null cases to avoid crashes
        return Gson().fromJson(value, listType) ?: emptyList()
    }
}