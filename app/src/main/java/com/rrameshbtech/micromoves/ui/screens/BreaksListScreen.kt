package com.rrameshbtech.micromoves.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rrameshbtech.micromoves.data.Break
import com.rrameshbtech.micromoves.data.BreakSchedule
import com.rrameshbtech.micromoves.data.BreakState
import com.rrameshbtech.micromoves.data.DaysOfWeek
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import com.rrameshbtech.micromoves.ui.theme.BackgroundLight
import com.rrameshbtech.micromoves.ui.theme.BorderLight
import com.rrameshbtech.micromoves.ui.theme.CardForegroundLight
import com.rrameshbtech.micromoves.ui.theme.CardLight
import com.rrameshbtech.micromoves.ui.theme.ForegroundLight
import com.rrameshbtech.micromoves.ui.theme.MicroMovesTheme
import com.rrameshbtech.micromoves.ui.theme.MutedForegroundLight
import com.rrameshbtech.micromoves.ui.theme.MutedLight
import com.rrameshbtech.micromoves.ui.theme.PrimaryLight
import com.rrameshbtech.micromoves.ui.theme.PrimaryForegroundLight
import com.rrameshbtech.micromoves.ui.theme.SecondaryForegroundLight
import com.rrameshbtech.micromoves.ui.theme.SecondaryLight
import com.rrameshbtech.micromoves.ui.components.MicroMovesToastHost
import com.rrameshbtech.micromoves.ui.components.PauseOptionsPanel
import com.rrameshbtech.micromoves.ui.components.rememberToastQueueState
import com.rrameshbtech.micromoves.viewmodel.BreaksListViewModel

private const val ETA_TICK_INTERVAL_MILLIS = 30_000L
private const val TOAST_DURATION_MILLIS = 5_000L
private const val MAX_VISIBLE_TOASTS = 3
private const val MAX_PAUSE_OCCURRENCES = 20
private val TOAST_ALIGNMENT = Alignment.BottomCenter
private val SWIPE_RIGHT_NUDGE_OFFSET = 24.dp
private const val SWIPE_RIGHT_COMPLETE_FRACTION = 0.25f
private const val SWIPE_LEFT_COMPLETE_FRACTION = 0.5f
private const val SWIPE_SETTLE_DURATION_MILLIS = 200

/**
 * Drives a break card's swipe gestures. Anything short of a completed swipe springs back to
 * the card's original position — no lingering offset, so collapsed and expanded cards stay
 * aligned with the rest of the list. A completed right swipe (past [SWIPE_RIGHT_COMPLETE_FRACTION]
 * of the card's width) or a long press nudges the card out and back while calling [onExpand] to
 * reveal the pause customization panel. A completed left swipe (past [SWIPE_LEFT_COMPLETE_FRACTION])
 * slides the card fully off-screen and fires [onSwipeLeftAction] (the card's primary CTA — pause
 * or resume) before resetting; the item then reappears wherever its now-changed break state sorts it.
 */
@Composable
private fun Modifier.swipeableBreakCard(
    expanded: Boolean,
    onExpand: () -> Unit,
    onSwipeLeftAction: () -> Unit,
): Modifier {
    val nudgePx = with(LocalDensity.current) { SWIPE_RIGHT_NUDGE_OFFSET.toPx() }
    val offsetX = remember { Animatable(0f) }
    var widthPx by remember { mutableFloatStateOf(0f) }
    val scope = rememberCoroutineScope()

    suspend fun nudgeRightThenExpand() {
        offsetX.animateTo(nudgePx, tween(SWIPE_SETTLE_DURATION_MILLIS))
        onExpand()
        offsetX.animateTo(0f, tween(SWIPE_SETTLE_DURATION_MILLIS))
    }

    return this
        .onSizeChanged { widthPx = it.width.toFloat() }
        .offset { IntOffset(offsetX.value.roundToInt(), 0) }
        .pointerInput(expanded) {
            if (!expanded) {
                detectTapGestures(onLongPress = { scope.launch { nudgeRightThenExpand() } })
            }
        }
        .draggable(
            enabled = !expanded,
            orientation = Orientation.Horizontal,
            state = rememberDraggableState { delta ->
                scope.launch { offsetX.snapTo(offsetX.value + delta) }
            },
            onDragStopped = {
                val rightThresholdPx = widthPx * SWIPE_RIGHT_COMPLETE_FRACTION
                val leftThresholdPx = widthPx * SWIPE_LEFT_COMPLETE_FRACTION
                when {
                    offsetX.value <= -leftThresholdPx -> {
                        offsetX.animateTo(-widthPx, tween(SWIPE_SETTLE_DURATION_MILLIS))
                        onSwipeLeftAction()
                        offsetX.snapTo(0f)
                    }
                    offsetX.value >= rightThresholdPx -> nudgeRightThenExpand()
                    else -> offsetX.animateTo(0f, tween(SWIPE_SETTLE_DURATION_MILLIS))
                }
            },
        )
}

@Composable
private fun tickingNow(intervalMillis: Long = ETA_TICK_INTERVAL_MILLIS): LocalDateTime {
    var now by remember { mutableStateOf(LocalDateTime.now()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(intervalMillis)
            now = LocalDateTime.now()
        }
    }
    return now
}

@Composable
fun BreaksListScreen(
    viewModel: BreaksListViewModel = viewModel(),
    modifier: Modifier = Modifier,
) {
    val breaks by viewModel.breaks.collectAsState()
    val toastQueue = rememberToastQueueState(maxVisible = MAX_VISIBLE_TOASTS)
    val now = tickingNow()

    Box(modifier = modifier.fillMaxSize()) {
        BreaksListContent(
            breaks = breaks,
            now = now,
            onPause = { breakItem, state ->
                viewModel.pauseBreak(breakItem, state)
                toastQueue.show(pauseToastMessage(breakItem, state, now))
            },
            onResume = { breakItem ->
                viewModel.resumeBreak(breakItem)
                toastQueue.show(resumeToastMessage(breakItem))
            },
        )
        MicroMovesToastHost(
            state = toastQueue,
            durationMillis = TOAST_DURATION_MILLIS,
            alignment = TOAST_ALIGNMENT,
        )
    }
}

private fun pauseToastMessage(breakItem: Break, state: BreakState, now: LocalDateTime): String = when (state) {
    is BreakState.PausedForOccurrences ->
        "${breakItem.name} paused for ${state.occurrences} ${pluralize(state.occurrences.toLong(), "time")}"
    is BreakState.PausedUntil -> "${breakItem.name} paused until ${formatPausedUntil(state.timestampMillis, now)}"
    is BreakState.Active -> resumeToastMessage(breakItem)
}

private fun resumeToastMessage(breakItem: Break): String = "${breakItem.name} is resumed"

@Composable
private fun BreaksListContent(
    breaks: List<Break>,
    now: LocalDateTime = LocalDateTime.now(),
    onPause: (Break, BreakState) -> Unit = { _, _ -> },
    onResume: (Break) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundLight),
        containerColor = BackgroundLight,
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BackgroundLight)
                    .padding(horizontal = 24.dp, vertical = 20.dp)
            ) {
                Text(
                    text = "Micro Moves",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Medium,
                    color = CardForegroundLight,
                    letterSpacing = (-1.5).sp
                )
            }
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = BackgroundLight,
                        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
                    )
                    .padding(24.dp)
            ) {
                Button(
                    onClick = { /* Navigate to manage breaks */ },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryLight,
                        contentColor = PrimaryForegroundLight
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = "Manage Breaks",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
                .background(BackgroundLight),
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(20.dp)
        ) {
            item {
                Text(
                    text = "${breaks.size} breaks scheduled",
                    fontSize = 15.sp,
                    color = ForegroundLight,
                    modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
                )
            }

            items(breaks.sortedWith(breakDisplayOrder(now)), key = { it.id }) { breakItem ->
                if (breakItem.enabled && breakItem.state is BreakState.Active) {
                    ActiveBreakCard(
                        breakItem = breakItem,
                        now = now,
                        onPause = { onPause(breakItem, BreakState.PausedForOccurrences(BreaksListViewModel.PAUSE_OCCURRENCES)) },
                        onCustomPause = { state -> onPause(breakItem, state) },
                        modifier = Modifier.animateItem(),
                    )
                } else {
                    PausedBreakCard(
                        breakItem = breakItem,
                        now = now,
                        onResume = { onResume(breakItem) },
                        onCustomPause = { state -> onPause(breakItem, state) },
                        modifier = Modifier.animateItem(),
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun ActiveBreakCard(
    breakItem: Break,
    now: LocalDateTime = LocalDateTime.now(),
    onPause: () -> Unit = {},
    onCustomPause: (BreakState) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 80.dp)
            .swipeableBreakCard(
                expanded = expanded,
                onExpand = { expanded = true },
                onSwipeLeftAction = { expanded = false; onPause() },
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = CardLight,
            contentColor = CardForegroundLight
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        val minutesUntilNext = breakItem.nextTriggerTimeInMins(now)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
            ) {
                Text(
                    text = breakItem.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = CardForegroundLight
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .width(8.dp)
                            .height(8.dp)
                            .background(color = PrimaryLight, shape = RoundedCornerShape(50.dp))
                    )
                    Text(
                        text = formatBreakEta(now, minutesUntilNext),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = PrimaryLight
                    )
                }
            }
            Button(
                onClick = onPause,
                modifier = Modifier
                    .height(40.dp)
                    .widthIn(min = 80.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SecondaryLight,
                    contentColor = SecondaryForegroundLight
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Pause",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        if (expanded) {
            HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp), color = BorderLight)
            PauseOptionsPanel(
                schedule = breakItem.schedule,
                now = now,
                maxOccurrences = MAX_PAUSE_OCCURRENCES,
                onCancel = { expanded = false },
                onSave = { state -> onCustomPause(state); expanded = false },
                modifier = Modifier.padding(20.dp),
            )
        }
    }
}

@Composable
fun PausedBreakCard(
    breakItem: Break,
    now: LocalDateTime = LocalDateTime.now(),
    onResume: () -> Unit = {},
    onCustomPause: (BreakState) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 80.dp)
            .swipeableBreakCard(
                expanded = expanded,
                onExpand = { expanded = true },
                onSwipeLeftAction = { expanded = false; onResume() },
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MutedLight,
            contentColor = MutedForegroundLight
        ),
        border = androidx.compose.material3.CardDefaults.outlinedCardBorder(
            enabled = true
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .alpha(0.6f),
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
            ) {
                Text(
                    text = breakItem.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MutedForegroundLight
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .width(8.dp)
                            .height(8.dp)
                            .background(color = MutedForegroundLight, shape = RoundedCornerShape(50.dp))
                    )
                    Text(
                        text = when (val s = breakItem.state) {
                            is BreakState.PausedForOccurrences -> "Paused for ${s.occurrences} cycles"
                            is BreakState.PausedUntil -> "Paused until ${formatPausedUntil(s.timestampMillis, now)}"
                            is BreakState.Active -> if (!breakItem.enabled) "Disabled" else "Paused"
                        },
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = MutedForegroundLight
                    )
                }
            }
            Button(
                onClick = onResume,
                modifier = Modifier
                    .height(40.dp)
                    .widthIn(min = 80.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MutedForegroundLight,
                    contentColor = BackgroundLight
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Resume",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        if (expanded) {
            HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp), color = BorderLight)
            PauseOptionsPanel(
                schedule = breakItem.schedule,
                now = now,
                maxOccurrences = MAX_PAUSE_OCCURRENCES,
                initialState = breakItem.state,
                onCancel = { expanded = false },
                onSave = { state -> onCustomPause(state); expanded = false },
                modifier = Modifier.padding(20.dp),
            )
        }
    }
}

private fun breakDisplayOrder(now: LocalDateTime): Comparator<Break> =
    compareBy(
        { breakItem -> !(breakItem.enabled && breakItem.state is BreakState.Active) },
        { breakItem -> breakItem.nextTriggerTimeInMins(now) },
    )

private val PAUSED_UNTIL_TIME_ONLY_FORMAT = java.time.format.DateTimeFormatter.ofPattern("h:mm a")
private val PAUSED_UNTIL_WITH_DATE_FORMAT = java.time.format.DateTimeFormatter.ofPattern("MMM d, h:mm a")

private fun formatPausedUntil(timestampMillis: Long, now: LocalDateTime): String {
    val zoned = java.time.Instant.ofEpochMilli(timestampMillis).atZone(java.time.ZoneId.systemDefault())
    val format = if (zoned.toLocalDate() == now.toLocalDate()) PAUSED_UNTIL_TIME_ONLY_FORMAT else PAUSED_UNTIL_WITH_DATE_FORMAT
    return zoned.format(format)
}

private fun pluralize(count: Long, unit: String): String = if (count == 1L) unit else "${unit}s"

private fun formatBreakEta(now: LocalDateTime, minutesUntil: Long): String {
    if (minutesUntil < 1) return "within a minute"
    if (minutesUntil < 60) return "in $minutesUntil ${pluralize(minutesUntil, "min")}"

    val targetDate = now.plusMinutes(minutesUntil).toLocalDate()
    val daysUntil = ChronoUnit.DAYS.between(now.toLocalDate(), targetDate)

    return when {
        daysUntil <= 0 -> {
            val hours = minutesUntil / 60
            "in $hours ${pluralize(hours, "hour")}"
        }
        daysUntil == 1L -> "occurs tomorrow"
        daysUntil in 2..6 -> "occurs in $daysUntil days"
        else -> "occurs next week"
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF1F3F1)
@Composable
private fun BreaksListScreenPreview() {
    // Fixed reference instant (a Monday) so every ETA wording bucket renders deterministically.
    val now = LocalDateTime.of(2026, 1, 5, 10, 5)
    val allDay = BreakSchedule(activeStartHour = 0, activeEndHour = 23)
    MicroMovesTheme {
        BreaksListContent(
            now = now,
            breaks = listOf(
                Break(id = 1, name = "Palming Eye Exercise", schedule = allDay.copy(frequencyMinutes = 5)),
                Break(id = 2, name = "Neck Stretches", schedule = allDay.copy(frequencyMinutes = 15)),
                Break(id = 3, name = "Stand & Walk", schedule = allDay.copy(frequencyMinutes = 240)),
                Break(
                    id = 4,
                    name = "Morning Stretch",
                    schedule = BreakSchedule(frequencyMinutes = 30, activeStartHour = 6, activeEndHour = 9),
                ),
                Break(
                    id = 5,
                    name = "Thursday Reset",
                    schedule = BreakSchedule(
                        frequencyMinutes = 30,
                        activeStartHour = 6,
                        activeEndHour = 9,
                        daysOfWeek = DaysOfWeek(setOf(DayOfWeek.THURSDAY)),
                    ),
                ),
                Break(
                    id = 6,
                    name = "Weekly Deep Stretch",
                    schedule = BreakSchedule(
                        frequencyMinutes = 30,
                        activeStartHour = 6,
                        activeEndHour = 9,
                        daysOfWeek = DaysOfWeek(setOf(DayOfWeek.MONDAY)),
                    ),
                ),
                Break(id = 7, name = "Shoulder Rolls", state = BreakState.PausedForOccurrences(occurrences = 2)),
                Break(id = 8, name = "Wrist Stretches", enabled = false),
                Break(id = 9, name = "Desk Yoga", state = BreakState.PausedUntil(now.plusHours(2).atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli())),
            )
        )
    }
}
