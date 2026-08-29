package com.rrameshbtech.micromoves.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BreakRoutineToPlaybackItemsTest {

    private fun exercise(id: Long, name: String, slideCount: Int) = Exercise(
        id = id,
        name = name,
        slides = List(slideCount) { Slide(description = "step $it") },
    )

    private fun routine(vararg exercises: Exercise) = BreakRoutine(
        breakItem = Break(name = "Test Break"),
        steps = exercises.mapIndexed { index, exercise -> ResolvedRoutineStep(exercise, index, false) },
    )

    @Test
    fun noIntroBeforeFirstExercise() {
        val items = routine(exercise(1, "Neck Stretches", 3)).toPlaybackItems()

        assertTrue(items.first() is BreakPlaybackItem.SlideItem)
    }

    @Test
    fun oneIntroPerSubsequentExercise() {
        val items = routine(
            exercise(1, "Neck Stretches", 2),
            exercise(2, "Shoulder Rolls", 3),
            exercise(3, "Stand & Walk", 1),
        ).toPlaybackItems()

        val intros = items.filterIsInstance<BreakPlaybackItem.ExerciseIntro>()
        assertEquals(listOf("Shoulder Rolls", "Stand & Walk"), intros.map { it.exerciseName })
    }

    @Test
    fun congratsIsAlwaysLast() {
        val items = routine(exercise(1, "Neck Stretches", 2)).toPlaybackItems()

        assertTrue(items.last() is BreakPlaybackItem.Congrats)
    }

    @Test
    fun slideCountMatchesSumOfExerciseSlides() {
        val items = routine(
            exercise(1, "Neck Stretches", 2),
            exercise(2, "Shoulder Rolls", 3),
        ).toPlaybackItems()

        assertEquals(5, items.count { it is BreakPlaybackItem.SlideItem })
    }
}
