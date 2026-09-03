package com.rrameshbtech.micromoves.scheduling

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.rrameshbtech.micromoves.MainActivity
import com.rrameshbtech.micromoves.R
import com.rrameshbtech.micromoves.data.AlertSettings
import com.rrameshbtech.micromoves.data.local.MicroMovesDatabase
import kotlinx.coroutines.flow.first

private const val NOTIFICATION_ID = 1
private const val CONTENT_REQUEST_CODE = 100
private const val SKIP_REQUEST_CODE = 101
private const val TIMEOUT_MILLIS = 5 * 60 * 1000L
private val VIBRATION_PATTERN = longArrayOf(0, 300, 200, 300)

/**
 * Posts/updates/cancels the single app-wide break-alert notification. Always one active
 * notification at a fixed [NOTIFICATION_ID] — refreshing just replaces its content in place.
 */
object BreakNotifier {

    const val EXTRA_BREAK_OCCURRENCE_ID = "com.rrameshbtech.micromoves.EXTRA_BREAK_OCCURRENCE_ID"

    /** Once a break's slides actually start playing, the alert has done its job — clear it. */
    fun cancel(context: Context) {
        NotificationManagerCompat.from(context).cancel(NOTIFICATION_ID)
    }

    suspend fun refresh(context: Context) {
        val database = MicroMovesDatabase.getDatabase(context)
        val notificationManager = NotificationManagerCompat.from(context)
        val pendingCount = database.breakOccurrenceDao().getPendingCount()
        if (pendingCount == 0) {
            notificationManager.cancel(NOTIFICATION_ID)
            return
        }
        val oldest = database.breakOccurrenceDao().getOldestPendingFlow().first() ?: return
        val breakItem = database.breakDao().getBreakById(oldest.breakId) ?: return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val contentIntent = contentPendingIntent(context, oldest.id)
        val notification = NotificationCompat.Builder(context, channelIdFor(context, breakItem.alertSettings))
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Time for a break: ${breakItem.name}")
            .setContentText(if (pendingCount > 1) "and ${pendingCount - 1} more waiting" else "Tap to start")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setOnlyAlertOnce(true)
            .setTimeoutAfter(TIMEOUT_MILLIS)
            .setContentIntent(contentIntent)
            .setFullScreenIntent(contentIntent, true)
            .addAction(R.drawable.ic_notification, "Start", contentIntent)
            .addAction(R.drawable.ic_notification, "Skip", skipPendingIntent(context, oldest.id))
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun contentPendingIntent(context: Context, occurrenceId: Long): PendingIntent {
        val intent = Intent(context, MainActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            .putExtra(EXTRA_BREAK_OCCURRENCE_ID, occurrenceId)
        return PendingIntent.getActivity(
            context, CONTENT_REQUEST_CODE, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun skipPendingIntent(context: Context, occurrenceId: Long): PendingIntent {
        val intent = Intent(context, SkipActionReceiver::class.java).putExtra(EXTRA_BREAK_OCCURRENCE_ID, occurrenceId)
        return PendingIntent.getBroadcast(
            context, SKIP_REQUEST_CODE, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    /**
     * A [NotificationChannel]'s sound/vibration is locked in at creation (API 26+), but
     * [AlertSettings] is per-break and mutable — so every chime/vibration combination gets its own
     * lazily-created channel instead of one shared channel nobody can adjust per break. Importance
     * stays HIGH on all of them regardless — that's what drives heads-up, not sound/vibration, so
     * the (required, always-on) notification itself still alerts even when both are off.
     */
    private fun channelIdFor(context: Context, alertSettings: AlertSettings): String {
        val id = "break_c${if (alertSettings.chimeEnabled) 1 else 0}_v${if (alertSettings.vibrationEnabled) 1 else 0}"
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (manager.getNotificationChannel(id) == null) {
            val channel = NotificationChannel(id, "Break alerts", NotificationManager.IMPORTANCE_HIGH).apply {
                setSound(if (alertSettings.chimeEnabled) RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION) else null, null)
                enableVibration(alertSettings.vibrationEnabled)
                if (alertSettings.vibrationEnabled) vibrationPattern = VIBRATION_PATTERN
            }
            manager.createNotificationChannel(channel)
        }
        return id
    }
}
