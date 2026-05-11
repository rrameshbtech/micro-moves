package com.rrameshbtech.micromoves.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalTime

/**
 * Core Break object representing a scheduled exercise break.
 * This contains all the definition and configuration for a break type.
 */
@Entity(tableName = "breaks")
data class Break(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    /** Name of the break (e.g., "Palming Eye Exercise") */
    val name: String,

    /** Description explaining health benefit of this break */
    val description: String = "",

    /** Frequency in minutes between occurrences */
    val frequency: Int = 15,

    /** Start hour (0-23) when the break is active */
    val activeStartHour: Int = 9,

    /** End hour (0-23) when the break is active */
    val activeEndHour: Int = 17,

    /** Whether this break is globally enabled */
    val enabled: Boolean = true,

    /** List of slide IDs that belong to this break */
    val slideIds: List<Long> = emptyList(),

    /** Created timestamp */
    val createdAt: Long = System.currentTimeMillis(),

    /** Last modified timestamp */
    val updatedAt: Long = System.currentTimeMillis(),

    /* TODO: To be removed after fixing the data modeling */
    val isPaused: Boolean = false,
    val pausedForCycles: Int = 0,
    val minutesUntilNext: Int = 0,
) {
    fun getActiveTimeRange(): Pair<Int, Int> = Pair(activeStartHour, activeEndHour)
}

/**
 * Individual slide/step within a break sequence.
 * Each slide represents a single instruction with optional visual guidance.
 */
@Entity(tableName = "slides")
data class Slide(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    /** ID of the break this slide belongs to */
    val breakId: Long,

    /** Instruction text for this step (e.g., "Look away from screen") */
    val instructionText: String,

    /** Optional URI to image showing the exercise pose/position */
    val imageUri: String? = null,

    /** Duration in milliseconds to display this slide */
    val durationMs: Long = 3000L,

    /** Order/position of this slide within the break */
    val order: Int = 0,

    /** Created timestamp */
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Tracks the active state of a break at runtime.
 * This is separate from Break to distinguish between definition and state.
 */
@Entity(tableName = "active_breaks")
data class ActiveBreak(
    @PrimaryKey
    val breakId: Long,

    /** Reference to the Break definition */
    val breakName: String,

    /** Whether this break is currently paused */
    val isPaused: Boolean = false,

    /** Number of cycles this break has been paused for */
    val pausedForCycles: Int = 0,

    /** Minutes until the next occurrence of this break */
    val minutesUntilNext: Int = 0,

    /** Last time this break was triggered (milliseconds since epoch) */
    val lastTriggeredAt: Long? = null,

    /** Total number of times this break has been triggered */
    val triggerCount: Int = 0,

    /** Timestamp when this active break record was created */
    val createdAt: Long = System.currentTimeMillis(),

    /** Last updated timestamp */
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Detailed schedule information for a break.
 * Provides convenience methods for time-based logic.
 */
data class BreakSchedule(
    val breakId: Long,
    val breakName: String,
    val frequency: Int, // in minutes
    val activeStartHour: Int,
    val activeEndHour: Int,
    val minutesUntilNext: Int,
    val isPaused: Boolean = false,
    val pausedCycles: Int = 0
) {
    val isWithinActiveHours: Boolean
        get() {
            val currentHour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
            return currentHour in activeStartHour until activeEndHour
        }

    val formattedTimeUntilNext: String
        get() {
            return when {
                minutesUntilNext < 60 -> "in $minutesUntilNext mins"
                else -> {
                    val hours = minutesUntilNext / 60
                    "in $hours hrs"
                }
            }
        }

    val statusText: String
        get() {
            return if (isPaused) {
                "Paused for $pausedCycles cycles"
            } else {
                formattedTimeUntilNext
            }
        }
}

/**
 * Combined view of a Break with its current ActiveBreak state.
 * This is what UI screens typically work with.
 */
data class BreakWithState(
    val breakDef: Break,
    val activeBreak: ActiveBreak,
    val slides: List<Slide> = emptyList()
) {
    val isActive: Boolean = !activeBreak.isPaused
    val timeDisplay: String = activeBreak.minutesUntilNext.let {
        when {
            it < 60 -> "in $it mins"
            else -> "in ${it / 60} hrs"
        }
    }
}


