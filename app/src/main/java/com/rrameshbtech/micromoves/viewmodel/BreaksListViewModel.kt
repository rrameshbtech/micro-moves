package com.rrameshbtech.micromoves.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rrameshbtech.micromoves.data.Break
import com.rrameshbtech.micromoves.data.BreakState
import com.rrameshbtech.micromoves.data.local.MicroMovesDatabase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class BreaksListViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = MicroMovesDatabase.getDatabase(application).breakDao()

    val breaks: StateFlow<List<Break>> = dao.getAllBreaks()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    fun pauseBreak(
        breakItem: Break,
        state: BreakState = BreakState.PausedForOccurrences(occurrences = PAUSE_OCCURRENCES),
    ) = updateState(breakItem, state)

    fun resumeBreak(breakItem: Break) = updateState(breakItem, BreakState.Active)

    private fun updateState(breakItem: Break, state: BreakState) {
        viewModelScope.launch {
            dao.update(breakItem.copy(state = state, updatedAt = System.currentTimeMillis()))
        }
    }

    companion object {
        const val PAUSE_OCCURRENCES = 1
    }
}
