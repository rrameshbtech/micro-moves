package com.rrameshbtech.micromoves

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rrameshbtech.micromoves.ui.screens.BreakScreen
import com.rrameshbtech.micromoves.ui.screens.BreaksListScreen
import com.rrameshbtech.micromoves.ui.screens.CustomizeBreaksScreen
import com.rrameshbtech.micromoves.ui.theme.MicroMovesTheme
import com.rrameshbtech.micromoves.viewmodel.MainViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MicroMovesTheme {
                val mainViewModel: MainViewModel = viewModel()
                val pendingOccurrenceId by mainViewModel.pendingOccurrenceId.collectAsState()
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
}

@Preview(showBackground = true)
@Composable
fun MainActivityPreview() {
    MicroMovesTheme {
        BreaksListScreen(modifier = Modifier.fillMaxSize())
    }
}