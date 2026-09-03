package com.rrameshbtech.micromoves.scheduling

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.rrameshbtech.micromoves.data.evaluateForWatcherTick
import com.rrameshbtech.micromoves.data.local.MicroMovesDatabase
import com.rrameshbtech.micromoves.data.local.createBreakOccurrenceSnapshots
import java.time.LocalDateTime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/** Fired by the [BreakAlarmScheduler]-armed alarm — the only place breaks actually fire. */
class BreakAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        val appContext = context.applicationContext
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val database = MicroMovesDatabase.getDatabase(appContext)
                val now = LocalDateTime.now()
                val nowMillis = System.currentTimeMillis()
                database.breakDao().getActiveBreaks().first().forEach { breakItem ->
                    val result = breakItem.evaluateForWatcherTick(now, nowMillis)
                    result.updatedBreak?.let { database.breakDao().update(it) }
                    if (result.firedOccurrences.isNotEmpty()) {
                        database.createBreakOccurrenceSnapshots(breakItem.id, result.firedOccurrences)
                    }
                }
                BreakAlarmScheduler.rearm(appContext)
                BreakNotifier.refresh(appContext)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
