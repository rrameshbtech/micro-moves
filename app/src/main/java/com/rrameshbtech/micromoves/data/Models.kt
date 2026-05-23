package com.rrameshbtech.micromoves.data

data class Slide(
    val imageUri: String? = null,
    val durationMs: Long = 3000L,
    val description: String = ""
)

data class Exercise(
    val name: String,
    val description: String = "",
    val slides: List<Slide> = emptyList()
) {
    val totalDuration: Long get() = slides.sumOf { it.durationMs }
}

data class RoutineStep(
    val exercise: Exercise,
    val pauseAfterStep: Boolean = false
)

data class BreakRoutine(
    val steps: List<RoutineStep> = emptyList()
) {
    val totalDuration: Long get() = steps.sumOf { it.exercise.totalDuration }
}

sealed class BreakState {
    object Active : BreakState()
    object Paused : BreakState()
    data class PausedForOccurrence(val occurrences: Int) : BreakState()
}

data class BreakSchedule(
    val frequencyMinutes: Int = 15,
    val activeStartHour: Int = 9,
    val activeEndHour: Int = 17
)
