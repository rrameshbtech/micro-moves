package com.rrameshbtech.micromoves.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rrameshbtech.micromoves.ui.theme.BackgroundLight
import com.rrameshbtech.micromoves.ui.theme.CardForegroundLight
import kotlinx.coroutines.delay

private const val DEFAULT_MAX_VISIBLE_TOASTS = 3

data class ToastMessage(val id: Long, val text: String)

/**
 * Queues toasts oldest-first, capped at [maxVisible] — showing one past the cap
 * silently drops the oldest so the newest is never blocked from appearing.
 */
class ToastQueueState(private val maxVisible: Int = DEFAULT_MAX_VISIBLE_TOASTS) {
    var messages by mutableStateOf<List<ToastMessage>>(emptyList())
        private set
    private var nextId = 0L

    fun show(text: String) {
        messages = (messages + ToastMessage(id = nextId++, text = text)).takeLast(maxVisible)
    }

    fun dismiss(id: Long) {
        messages = messages.filterNot { it.id == id }
    }
}

@Composable
fun rememberToastQueueState(maxVisible: Int = DEFAULT_MAX_VISIBLE_TOASTS): ToastQueueState =
    remember { ToastQueueState(maxVisible) }

/**
 * Renders [state]'s queued toasts oldest-at-top, each dismissing itself independently
 * after [durationMillis] regardless of when sibling toasts were shown or dismissed.
 */
@Composable
fun MicroMovesToastHost(
    state: ToastQueueState,
    modifier: Modifier = Modifier,
    durationMillis: Long = 5_000L,
    alignment: Alignment = Alignment.BottomCenter,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = alignment
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            state.messages.forEach { toast ->
                key(toast.id) {
                    LaunchedEffect(toast.id) {
                        delay(durationMillis)
                        state.dismiss(toast.id)
                    }
                    val swipeState = rememberSwipeToDismissBoxState()
                    SwipeToDismissBox(
                        state = swipeState,
                        backgroundContent = {},
                        onDismiss = { state.dismiss(toast.id) },
                    ) {
                        ToastBubble(text = toast.text)
                    }
                }
            }
        }
    }
}

@Composable
private fun ToastBubble(text: String) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = CardForegroundLight,
        contentColor = BackgroundLight,
        shadowElevation = 4.dp
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp),
            fontSize = 15.sp
        )
    }
}
