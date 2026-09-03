package com.rrameshbtech.micromoves.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rrameshbtech.micromoves.data.AlertSettings
import com.rrameshbtech.micromoves.data.Break
import com.rrameshbtech.micromoves.data.BreakSchedule
import com.rrameshbtech.micromoves.data.local.MicroMovesDatabase
import com.rrameshbtech.micromoves.scheduling.BreakAlarmScheduler
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CustomizeBreaksViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = MicroMovesDatabase.getDatabase(application).breakDao()

    val breaks: StateFlow<List<Break>> = dao.getAllBreaks()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    fun setEnabled(breakItem: Break, enabled: Boolean) {
        viewModelScope.launch {
            dao.update(breakItem.copy(enabled = enabled, updatedAt = System.currentTimeMillis()))
            BreakAlarmScheduler.rearm(getApplication())
        }
    }

    fun updateSettings(breakItem: Break, schedule: BreakSchedule, alertSettings: AlertSettings) {
        viewModelScope.launch {
            dao.update(
                breakItem.copy(schedule = schedule, alertSettings = alertSettings, updatedAt = System.currentTimeMillis())
            )
            BreakAlarmScheduler.rearm(getApplication())
        }
    }
}
