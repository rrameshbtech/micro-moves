package com.rrameshbtech.micromoves.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rrameshbtech.micromoves.data.AlertSettings
import com.rrameshbtech.micromoves.data.Break
import com.rrameshbtech.micromoves.data.BreakSchedule
import com.rrameshbtech.micromoves.data.Exercise
import com.rrameshbtech.micromoves.data.local.MicroMovesDatabase
import com.rrameshbtech.micromoves.data.local.createBreak
import com.rrameshbtech.micromoves.scheduling.BreakAlarmScheduler
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class CreateBreakViewModel(application: Application) : AndroidViewModel(application) {
    private val database = MicroMovesDatabase.getDatabase(application)

    val catalog: StateFlow<List<Exercise>> = database.exerciseDao().getAllExercises()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    suspend fun createBreak(
        name: String,
        description: String,
        schedule: BreakSchedule,
        alertSettings: AlertSettings,
        exerciseIds: List<Long>,
    ): Long {
        val breakId = database.createBreak(
            Break(name = name, description = description, schedule = schedule, alertSettings = alertSettings),
            exerciseIds,
        )
        BreakAlarmScheduler.rearm(getApplication())
        return breakId
    }
}
