package com.rrameshbtech.micromoves

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.rrameshbtech.micromoves.ui.screens.BreakScreen
import com.rrameshbtech.micromoves.ui.screens.BreaksListScreen
import com.rrameshbtech.micromoves.ui.screens.CustomizeBreaksScreen
import com.rrameshbtech.micromoves.ui.theme.MicroMovesTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MicroMovesTheme {
                var showCustomize by remember { mutableStateOf(false) }
                var activeBreakId by remember { mutableStateOf<Long?>(null) }
                BackHandler(enabled = showCustomize) { showCustomize = false }
                BackHandler(enabled = activeBreakId != null) { activeBreakId = null }

                when {
                    activeBreakId != null -> BreakScreen(
                        breakId = activeBreakId!!,
                        onDone = { activeBreakId = null },
                        modifier = Modifier.fillMaxSize(),
                    )
                    showCustomize -> CustomizeBreaksScreen(
                        onBack = { showCustomize = false },
                        modifier = Modifier.fillMaxSize(),
                    )
                    else -> BreaksListScreen(
                        onManageBreaks = { showCustomize = true },
                        onOpenBreak = { breakItem -> activeBreakId = breakItem.id },
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