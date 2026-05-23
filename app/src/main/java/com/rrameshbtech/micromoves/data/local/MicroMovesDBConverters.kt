package com.rrameshbtech.micromoves.data.local

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.rrameshbtech.micromoves.data.BreakRoutine
import com.rrameshbtech.micromoves.data.BreakState

class MicroMovesDBConverters {

    @TypeConverter
    fun fromBreakState(state: BreakState): String = when (state) {
        is BreakState.Active -> "ACTIVE"
        is BreakState.Paused -> "PAUSED"
        is BreakState.PausedForOccurrence -> "PAUSED_FOR_OCCURRENCE:${state.occurrences}"
    }

    @TypeConverter
    fun toBreakState(value: String): BreakState = when {
        value == "ACTIVE" -> BreakState.Active
        value == "PAUSED" -> BreakState.Paused
        value.startsWith("PAUSED_FOR_OCCURRENCE:") ->
            BreakState.PausedForOccurrence(value.substringAfter(":").toInt())
        else -> BreakState.Active
    }

    @TypeConverter
    fun fromBreakRoutine(routine: BreakRoutine): String = Gson().toJson(routine)

    @TypeConverter
    fun toBreakRoutine(value: String): BreakRoutine =
        Gson().fromJson(value, BreakRoutine::class.java) ?: BreakRoutine()
}
