package com.rrameshbtech.micromoves.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rrameshbtech.micromoves.data.local.MicroMovesDatabase
import com.rrameshbtech.micromoves.data.local.createBreakOccurrenceSnapshot
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val database = MicroMovesDatabase.getDatabase(application)

    /** Oldest not-yet-completed occurrence across all breaks — drives auto-show/FIFO draining. */
    val pendingOccurrenceId: StateFlow<Long?> = database.breakOccurrenceDao()
        .getOldestPendingFlow()
        .map { it?.id }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    /** Long-press-to-open flow: snapshots now, returns the created occurrence's id. */
    suspend fun launchBreakManually(breakId: Long): Long? = database.createBreakOccurrenceSnapshot(breakId)?.id
}
