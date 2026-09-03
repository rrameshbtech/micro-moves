package com.rrameshbtech.micromoves.data.local

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.rrameshbtech.micromoves.data.BreakState
import com.rrameshbtech.micromoves.data.DaysOfWeek
import com.rrameshbtech.micromoves.data.ExerciseOutcome
import com.rrameshbtech.micromoves.data.Slide

class MicroMovesDBConverters {

    @TypeConverter
    fun fromBreakState(state: BreakState): String = when (state) {
        is BreakState.Active -> "ACTIVE"
        is BreakState.PausedForOccurrences -> "PAUSED_FOR_OCCURRENCES:${state.occurrences}"
        is BreakState.PausedUntil -> "PAUSED_UNTIL:${state.timestampMillis}"
    }

    @TypeConverter
    fun toBreakState(value: String): BreakState = when {
        value == "ACTIVE" -> BreakState.Active
        value.startsWith("PAUSED_FOR_OCCURRENCES:") ->
            BreakState.PausedForOccurrences(value.substringAfter(":").toInt())
        value.startsWith("PAUSED_UNTIL:") ->
            BreakState.PausedUntil(value.substringAfter(":").toLong())
        else -> BreakState.Active
    }

    @TypeConverter
    fun fromExerciseOutcome(outcome: ExerciseOutcome): String = when (outcome) {
        ExerciseOutcome.Completed -> "COMPLETED"
        ExerciseOutcome.Skipped -> "SKIPPED"
        ExerciseOutcome.Paused -> "PAUSED"
    }

    @TypeConverter
    fun toExerciseOutcome(value: String): ExerciseOutcome = when (value) {
        "SKIPPED" -> ExerciseOutcome.Skipped
        "PAUSED" -> ExerciseOutcome.Paused
        else -> ExerciseOutcome.Completed
    }

    @TypeConverter
    fun fromDaysOfWeek(daysOfWeek: DaysOfWeek): Int = daysOfWeek.toBitmask()

    @TypeConverter
    fun toDaysOfWeek(mask: Int): DaysOfWeek = DaysOfWeek.fromBitmask(mask)

    @TypeConverter
    fun fromSlideList(slides: List<Slide>): String = Gson().toJson(slides)

    @TypeConverter
    fun toSlideList(value: String): List<Slide> =
        Gson().fromJson(value, object : TypeToken<List<Slide>>() {}.type) ?: emptyList()

    @TypeConverter
    fun fromLongList(ids: List<Long>): String = Gson().toJson(ids)

    @TypeConverter
    fun toLongList(value: String): List<Long> =
        Gson().fromJson(value, object : TypeToken<List<Long>>() {}.type) ?: emptyList()
}
