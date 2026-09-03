package com.rrameshbtech.micromoves.scheduling

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.rrameshbtech.micromoves.data.local.MicroMovesDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/** Handles the notification's "Skip" action — marks the occurrence skipped without opening the app. */
class SkipActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val occurrenceId = intent.getLongExtra(BreakNotifier.EXTRA_BREAK_OCCURRENCE_ID, -1L)
        if (occurrenceId == -1L) return
        val pendingResult = goAsync()
        val appContext = context.applicationContext
        CoroutineScope(Dispatchers.IO).launch {
            try {
                MicroMovesDatabase.getDatabase(appContext).breakOccurrenceDao()
                    .markSkipped(occurrenceId, System.currentTimeMillis())
                BreakNotifier.refresh(appContext)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
