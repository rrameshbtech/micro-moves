package com.rrameshbtech.micromoves.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rrameshbtech.micromoves.data.evaluateForWatcherTick
import com.rrameshbtech.micromoves.data.local.MicroMovesDatabase
import com.rrameshbtech.micromoves.data.local.createBreakOccurrenceSnapshot
import java.time.LocalDateTime
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val database = MicroMovesDatabase.getDatabase(application)
    private val breakDao = database.breakDao()

    /** Oldest not-yet-completed occurrence across all breaks — drives auto-show/FIFO draining. */
    val pendingOccurrenceId: StateFlow<Long?> = database.breakOccurrenceDao()
        .getOldestPendingFlow()
        .map { it?.id }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    init {
        viewModelScope.launch {
            while (true) {
                tick()
                delay(WATCHER_TICK_INTERVAL_MILLIS)
            }
        }
    }

    /** Long-press-to-open flow: snapshots now, returns the created occurrence's id. */
    suspend fun launchBreakManually(breakId: Long): Long? = database.createBreakOccurrenceSnapshot(breakId)?.id

    private suspend fun tick() {
        val now = LocalDateTime.now()
        val nowMillis = System.currentTimeMillis()
        breakDao.getActiveBreaks().first().forEach { breakItem ->
            val result = breakItem.evaluateForWatcherTick(now, nowMillis)
            result.updatedBreak?.let { breakDao.update(it) }
            result.firedBreakId?.let { database.createBreakOccurrenceSnapshot(it) }
        }
    }

    companion object {
        private const val WATCHER_TICK_INTERVAL_MILLIS = 30_000L
    }
}
