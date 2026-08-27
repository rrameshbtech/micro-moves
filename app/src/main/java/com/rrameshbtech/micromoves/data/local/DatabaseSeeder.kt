package com.rrameshbtech.micromoves.data.local

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.rrameshbtech.micromoves.data.Break
import com.rrameshbtech.micromoves.data.BreakSchedule
import com.rrameshbtech.micromoves.data.DaysOfWeek
import com.rrameshbtech.micromoves.data.Exercise
import com.rrameshbtech.micromoves.data.RoutineStep
import com.rrameshbtech.micromoves.data.Slide

private data class SlideSeedItem(
    val description: String = "",
    val durationMs: Long = 3000L,
    val imageUri: String? = null,
)

private data class ScheduleSeedItem(
    val frequencyMinutes: Int = 15,
    val activeStartHour: Int = 9,
    val activeEndHour: Int = 17,
    val daysOfWeekMask: Int = DaysOfWeek.EVERY_DAY.toBitmask(),
)

private data class ExerciseSeedItem(
    val id: Long,
    val name: String,
    val description: String = "",
    val slides: List<SlideSeedItem> = emptyList(),
    val suggestedSchedule: ScheduleSeedItem = ScheduleSeedItem(),
)

private data class BreakSeedItem(
    val name: String,
    val frequencyMinutes: Int = 15,
    val activeStartHour: Int = 9,
    val activeEndHour: Int = 17,
    val daysOfWeekMask: Int = DaysOfWeek.EVERY_DAY.toBitmask(),
    val exerciseIds: List<Long> = emptyList(),
)

private fun ScheduleSeedItem.toBreakSchedule() = BreakSchedule(
    frequencyMinutes = frequencyMinutes,
    activeStartHour = activeStartHour,
    activeEndHour = activeEndHour,
    daysOfWeek = DaysOfWeek.fromBitmask(daysOfWeekMask),
)

private fun SlideSeedItem.toSlide() = Slide(imageUri = imageUri, durationMs = durationMs, description = description)

private fun ExerciseSeedItem.toExercise() = Exercise(
    id = id,
    name = name,
    description = description,
    slides = slides.map { it.toSlide() },
    suggestedSchedule = suggestedSchedule.toBreakSchedule(),
)

private fun BreakSeedItem.toBreak() = Break(
    name = name,
    schedule = BreakSchedule(
        frequencyMinutes = frequencyMinutes,
        activeStartHour = activeStartHour,
        activeEndHour = activeEndHour,
        daysOfWeek = DaysOfWeek.fromBitmask(daysOfWeekMask),
    ),
)

object DatabaseSeeder {

    suspend fun seed(context: Context, exerciseDao: ExerciseDao, breakDao: BreakDao, routineStepDao: RoutineStepDao) {
        val gson = Gson()

        val exerciseJson = context.assets.open("exercises_catalog.json").bufferedReader().use { it.readText() }
        val exerciseType = object : TypeToken<List<ExerciseSeedItem>>() {}.type
        val exerciseItems: List<ExerciseSeedItem> = gson.fromJson(exerciseJson, exerciseType)
        exerciseDao.insertAll(exerciseItems.map { it.toExercise() })

        val breakJson = context.assets.open("init_breaks.json").bufferedReader().use { it.readText() }
        val breakType = object : TypeToken<List<BreakSeedItem>>() {}.type
        val breakItems: List<BreakSeedItem> = gson.fromJson(breakJson, breakType)

        breakItems.forEach { item ->
            val breakId = breakDao.insert(item.toBreak())
            val steps = item.exerciseIds.mapIndexed { index, exerciseId ->
                RoutineStep(breakId = breakId, exerciseId = exerciseId, position = index)
            }
            routineStepDao.insertAll(steps)
        }
    }
}
