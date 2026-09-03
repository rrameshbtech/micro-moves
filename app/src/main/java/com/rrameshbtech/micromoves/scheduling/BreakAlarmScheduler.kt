package com.rrameshbtech.micromoves.scheduling

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.rrameshbtech.micromoves.MainActivity
import com.rrameshbtech.micromoves.data.earliestAlarmWakeTime
import com.rrameshbtech.micromoves.data.local.MicroMovesDatabase
import java.time.LocalDateTime
import java.time.ZoneId
import kotlinx.coroutines.flow.first

private const val REQUEST_CODE = 1001

/** Arms a single background alarm for the earliest break due across the whole app. */
object BreakAlarmScheduler {

    suspend fun rearm(context: Context) {
        val database = MicroMovesDatabase.getDatabase(context)
        val activeBreaks = database.breakDao().getActiveBreaks().first()
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pendingIntent = alarmPendingIntent(context)

        val wakeTime = activeBreaks.earliestAlarmWakeTime(LocalDateTime.now())
        // SCHEDULE_EXACT_ALARM is a Settings-granted toggle on API 31+, not auto-granted like
        // USE_EXACT_ALARM — MainActivity prompts for it, but until it's actually granted,
        // setAlarmClock throws instead of silently degrading, so skip scheduling rather than crash.
        val canScheduleExact = Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()
        if (wakeTime == null || !canScheduleExact) {
            alarmManager.cancel(pendingIntent)
            return
        }

        val triggerAtMillis = wakeTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            .coerceAtLeast(System.currentTimeMillis())
        alarmManager.setAlarmClock(AlarmManager.AlarmClockInfo(triggerAtMillis, showIntent(context)), pendingIntent)
    }

    private fun alarmPendingIntent(context: Context): PendingIntent =
        PendingIntent.getBroadcast(
            context, REQUEST_CODE, Intent(context, BreakAlarmReceiver::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

    private fun showIntent(context: Context): PendingIntent =
        PendingIntent.getActivity(
            context, REQUEST_CODE, Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
}
