package com.rrameshbtech.micromoves.data

import org.junit.Assert.assertEquals
import org.junit.Test

class ResolvedStepsSnapshotTest {

    private fun exercise(id: Long, name: String) = Exercise(id = id, name = name)

    @Test
    fun preservesSnapshotOrderRegardlessOfMapOrder() {
        val exercisesById = mapOf(
            3L to exercise(3, "Third"),
            1L to exercise(1, "First"),
            2L to exercise(2, "Second"),
        )

        val steps = listOf(1L, 2L, 3L).toResolvedSteps(exercisesById)

        assertEquals(listOf("First", "Second", "Third"), steps.map { it.exercise.name })
        assertEquals(listOf(0, 1, 2), steps.map { it.position })
    }

    @Test
    fun dropsIdMissingFromMap() {
        val exercisesById = mapOf(1L to exercise(1, "First"))

        val steps = listOf(1L, 2L, 3L).toResolvedSteps(exercisesById)

        assertEquals(listOf("First"), steps.map { it.exercise.name })
    }
}
