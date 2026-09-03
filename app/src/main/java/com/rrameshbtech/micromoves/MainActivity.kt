package com.rrameshbtech.micromoves

import android.Manifest
import android.app.AlarmManager
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rrameshbtech.micromoves.scheduling.BreakAlarmScheduler
import com.rrameshbtech.micromoves.scheduling.BreakNotifier
import com.rrameshbtech.micromoves.ui.screens.BreakScreen
import com.rrameshbtech.micromoves.ui.screens.BreaksListScreen
import com.rrameshbtech.micromoves.ui.screens.CustomizeBreaksScreen
import com.rrameshbtech.micromoves.ui.theme.MicroMovesTheme
import com.rrameshbtech.micromoves.viewmodel.MainViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private var notificationOccurrenceId by mutableStateOf<Long?>(null)

    private val requestNotificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {}

    // SCHEDULE_EXACT_ALARM is a "special app access" toggle on API 31+, granted via Settings
    // rather than a runtime dialog — rearm() so an alarm gets armed the moment the user returns
    // from granting it, instead of waiting for the next unrelated trigger.
    private val requestExactAlarmSetting = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        lifecycleScope.launch { BreakAlarmScheduler.rearm(applicationContext) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // Lets the break-alert notification's full-screen intent wake the device and show this
        // Activity directly over the lock screen — no-ops when launched normally/unlocked.
        setShowWhenLocked(true)
        setTurnScreenOn(true)

        notificationOccurrenceId = intent.breakOccurrenceIdExtra()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(AlarmManager::class.java)
            if (!alarmManager.canScheduleExactAlarms()) {
                requestExactAlarmSetting.launch(
                    Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, Uri.parse("package:$packageName"))
                )
            }
        }
        lifecycleScope.launch { BreakAlarmScheduler.rearm(applicationContext) }

        setContent {
            MicroMovesTheme {
                val mainViewModel: MainViewModel = viewModel()
                // RESUMED-gated: a break that becomes pending while this app is backgrounded (but
                // still alive in memory) must not auto-launch BreakScreen — only the notification
                // should alert then. Collection simply stops updating below RESUMED and catches up
                // to whatever's latest the moment the app is truly in the foreground again.
                val pendingOccurrenceId by mainViewModel.pendingOccurrenceId.collectAsStateWithLifecycle(
                    minActiveState = Lifecycle.State.RESUMED,
                )
                val scope = rememberCoroutineScope()

                var showCustomize by remember { mutableStateOf(false) }
                var activeBreakOccurrenceId by remember { mutableStateOf<Long?>(null) }

                // Auto-show: only when nothing is currently playing and the user isn't mid-edit
                // on CustomizeBreaksScreen — it reappears the moment they back out to the list.
                LaunchedEffect(pendingOccurrenceId, showCustomize, activeBreakOccurrenceId) {
                    if (activeBreakOccurrenceId == null && !showCustomize && pendingOccurrenceId != null) {
                        activeBreakOccurrenceId = pendingOccurrenceId
                    }
                }

                // A notification tap (cold start via onCreate, or already-running via onNewIntent)
                // jumps straight to that occurrence; nulled out after consuming so a repeat tap
                // on the same notification re-triggers instead of being ignored as an unchanged key.
                LaunchedEffect(notificationOccurrenceId) {
                    notificationOccurrenceId?.let {
                        activeBreakOccurrenceId = it
                        notificationOccurrenceId = null
                    }
                }

                BackHandler(enabled = showCustomize) { showCustomize = false }

                when {
                    activeBreakOccurrenceId != null -> BreakScreen(
                        breakOccurrenceId = activeBreakOccurrenceId!!,
                        onDone = { activeBreakOccurrenceId = null },
                        modifier = Modifier.fillMaxSize(),
                    )
                    showCustomize -> CustomizeBreaksScreen(
                        onBack = { showCustomize = false },
                        modifier = Modifier.fillMaxSize(),
                    )
                    else -> BreaksListScreen(
                        onManageBreaks = { showCustomize = true },
                        onOpenBreak = { breakItem ->
                            scope.launch {
                                mainViewModel.launchBreakManually(breakItem.id)?.let { activeBreakOccurrenceId = it }
                            }
                        },
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        notificationOccurrenceId = intent.breakOccurrenceIdExtra()
    }
}

private fun Intent.breakOccurrenceIdExtra(): Long? =
    getLongExtra(BreakNotifier.EXTRA_BREAK_OCCURRENCE_ID, -1L).takeIf { it != -1L }

@Preview(showBackground = true)
@Composable
fun MainActivityPreview() {
    MicroMovesTheme {
        BreaksListScreen(modifier = Modifier.fillMaxSize())
    }
}
