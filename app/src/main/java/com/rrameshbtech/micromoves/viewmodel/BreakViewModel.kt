package com.rrameshbtech.micromoves.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.rrameshbtech.micromoves.data.BreakOccurrence
import com.rrameshbtech.micromoves.data.BreakPlaybackItem
import com.rrameshbtech.micromoves.data.ExerciseOccurrence
import com.rrameshbtech.micromoves.data.ExerciseOutcome
import com.rrameshbtech.micromoves.data.local.MicroMovesDatabase
import com.rrameshbtech.micromoves.data.local.getBreakRoutine
import com.rrameshbtech.micromoves.data.toPlaybackItems
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class BreakUiState {
    object Loading : BreakUiState()
    object NotFound : BreakUiState()
    data class Playing(val item: BreakPlaybackItem, val elapsedMs: Long, val durationMs: Long) : BreakUiState()
}

class BreakViewModel(application: Application, private val breakId: Long) : AndroidViewModel(application) {

    private val database = MicroMovesDatabase.getDatabase(application)

    private val _uiState = MutableStateFlow<BreakUiState>(BreakUiState.Loading)
    val uiState: StateFlow<BreakUiState> = _uiState.asStateFlow()

    private var items: List<BreakPlaybackItem> = emptyList()
    private var breakOccurrenceId = 0L
    private var currentIndex = 0
    private var elapsedMs = 0L
    private var exerciseElapsedMs = 0L

    init {
        viewModelScope.launch { startPlayback() }
    }

    private suspend fun startPlayback() {
        val routine = database.getBreakRoutine(breakId)
        if (routine == null || routine.steps.isEmpty()) {
            _uiState.value = BreakUiState.NotFound
            return
        }
        items = routine.toPlaybackItems()
        breakOccurrenceId = database.breakOccurrenceDao()
            .insert(BreakOccurrence(breakId = breakId, triggeredAt = System.currentTimeMillis()))
        runTicker()
    }

    private suspend fun runTicker() {
        while (currentIndex <= items.lastIndex) {
            val item = items[currentIndex]
            val durationMs = durationForItem(item)
            _uiState.value = BreakUiState.Playing(item, elapsedMs, durationMs)

            if (item is BreakPlaybackItem.Congrats) return

            if (elapsedMs >= durationMs) {
                if (item is BreakPlaybackItem.SlideItem && item.isLastSlideOfExercise) {
                    recordExerciseOccurrence(item.exerciseId, item.exerciseIndex, ExerciseOutcome.Completed)
                }
                delay(INTER_SLIDE_PAUSE_MS)
                moveToIndex(currentIndex + 1)
                continue
            }

            delay(TICK_MS)
            elapsedMs += TICK_MS
            if (item is BreakPlaybackItem.SlideItem) exerciseElapsedMs += TICK_MS
        }
    }

    /** Long-press-anywhere callback: ends the current exercise early and jumps to the next one. */
    fun skipCurrentExercise() {
        val item = (_uiState.value as? BreakUiState.Playing)?.item as? BreakPlaybackItem.SlideItem ?: return
        viewModelScope.launch {
            recordExerciseOccurrence(item.exerciseId, item.exerciseIndex, ExerciseOutcome.Skipped)
            val nextIndex = items
                .drop(currentIndex + 1)
                .indexOfFirst { it is BreakPlaybackItem.ExerciseIntro || it is BreakPlaybackItem.Congrats }
                .let { relativeIndex -> if (relativeIndex == -1) items.lastIndex else currentIndex + 1 + relativeIndex }
            moveToIndex(nextIndex)
        }
    }

    private fun moveToIndex(index: Int) {
        currentIndex = index
        elapsedMs = 0L
        exerciseElapsedMs = 0L
    }

    private suspend fun recordExerciseOccurrence(exerciseId: Long, exerciseIndex: Int, outcome: ExerciseOutcome) {
        database.exerciseOccurrenceDao().insert(
            ExerciseOccurrence(
                breakOccurrenceId = breakOccurrenceId,
                exerciseId = exerciseId,
                position = exerciseIndex,
                outcome = outcome,
                durationMs = exerciseElapsedMs,
            )
        )
    }

    private fun durationForItem(item: BreakPlaybackItem): Long = when (item) {
        is BreakPlaybackItem.SlideItem -> item.slide.durationMs
        is BreakPlaybackItem.ExerciseIntro -> INTRO_DURATION_MS
        BreakPlaybackItem.Congrats -> Long.MAX_VALUE
    }

    companion object {
        private const val TICK_MS = 100L
        private const val INTER_SLIDE_PAUSE_MS = 1_000L
        private const val INTRO_DURATION_MS = 3_000L

        fun factory(application: Application, breakId: Long): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    BreakViewModel(application, breakId) as T
            }
    }
}
