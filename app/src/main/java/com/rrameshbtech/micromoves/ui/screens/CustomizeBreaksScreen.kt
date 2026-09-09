package com.rrameshbtech.micromoves.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlin.math.roundToInt
import kotlinx.coroutines.launch
import com.rrameshbtech.micromoves.data.AlertSettings
import com.rrameshbtech.micromoves.data.Break
import com.rrameshbtech.micromoves.data.BreakSchedule
import com.rrameshbtech.micromoves.data.DaysOfWeek
import com.rrameshbtech.micromoves.ui.components.BreakScheduleEditorPanel
import com.rrameshbtech.micromoves.ui.components.formatFrequencyStepperLabel
import com.rrameshbtech.micromoves.ui.components.ToggleSwitch
import com.rrameshbtech.micromoves.ui.theme.BackgroundLight
import com.rrameshbtech.micromoves.ui.theme.BorderLight
import com.rrameshbtech.micromoves.ui.theme.CardForegroundLight
import com.rrameshbtech.micromoves.ui.theme.CardLight
import com.rrameshbtech.micromoves.ui.theme.ForegroundLight
import com.rrameshbtech.micromoves.ui.theme.MicroMovesTheme
import com.rrameshbtech.micromoves.ui.theme.MutedForegroundLight
import com.rrameshbtech.micromoves.ui.theme.MutedLight
import com.rrameshbtech.micromoves.ui.theme.PrimaryForegroundLight
import com.rrameshbtech.micromoves.ui.theme.PrimaryLight
import com.rrameshbtech.micromoves.ui.theme.SecondaryForegroundLight
import com.rrameshbtech.micromoves.ui.theme.SecondaryLight
import com.rrameshbtech.micromoves.viewmodel.CustomizeBreaksViewModel
import java.time.DayOfWeek

@Composable
fun CustomizeBreaksScreen(
    viewModel: CustomizeBreaksViewModel = viewModel(),
    onBack: () -> Unit = {},
    onNewBreak: () -> Unit = {},
    onEditBreak: (Break) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val breaks by viewModel.breaks.collectAsState()
    CustomizeBreaksContent(
        breaks = breaks,
        onBack = onBack,
        onNewBreak = onNewBreak,
        onEditBreak = onEditBreak,
        onToggleEnabled = viewModel::setEnabled,
        onSaveSettings = viewModel::updateSettings,
        onDelete = viewModel::deleteBreak,
        modifier = modifier,
    )
}

@Composable
private fun CustomizeBreaksContent(
    breaks: List<Break>,
    onBack: () -> Unit = {},
    onNewBreak: () -> Unit = {},
    onEditBreak: (Break) -> Unit = {},
    onToggleEnabled: (Break, Boolean) -> Unit = { _, _ -> },
    onSaveSettings: (Break, BreakSchedule, AlertSettings) -> Unit = { _, _, _ -> },
    onDelete: (Break) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundLight),
        containerColor = BackgroundLight,
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = BackgroundLight, shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                    .padding(24.dp)
            ) {
                Button(
                    onClick = onNewBreak,
                    modifier = Modifier.fillMaxWidth().height(60.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryLight, contentColor = PrimaryForegroundLight),
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                    Text(text = "New Break", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        },
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BackgroundLight)
                    .statusBarsPadding()
                    .padding(horizontal = 12.dp, vertical = 12.dp)
            ) {
                IconButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterStart).size(48.dp)) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = CardForegroundLight,
                    )
                }
                Text(
                    text = activeBreaksStatusMessage(breaks),
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 8.dp),
                    fontSize = 16.sp,
                    color = MutedForegroundLight,
                )
            }
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
                .background(BackgroundLight),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }

            items(breaks.sortedBy { !it.enabled }, key = { it.id }) { breakItem ->
                CustomizeBreakCard(
                    breakItem = breakItem,
                    onToggleEnabled = { enabled -> onToggleEnabled(breakItem, enabled) },
                    onSaveSettings = { schedule, alertSettings -> onSaveSettings(breakItem, schedule, alertSettings) },
                    onDelete = { onDelete(breakItem) },
                    onEdit = { onEditBreak(breakItem) },
                    modifier = Modifier.animateItem(),
                )
            }

            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }
}

private fun activeBreaksStatusMessage(breaks: List<Break>): String =
    if (breaks.isNotEmpty()) "${breaks.count { it.enabled }} of ${breaks.size} active ${if (breaks.size > 1) "breaks" else "break"}" else "No breaks found"

@Composable
fun CustomizeBreakCard(
    breakItem: Break,
    onToggleEnabled: (Boolean) -> Unit,
    onSaveSettings: (BreakSchedule, AlertSettings) -> Unit,
    onDelete: () -> Unit = {},
    onEdit: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    // A disabled break's schedule/alert settings can't be opened for editing — and if it's
    // disabled while already open, close it so any unsaved edits are discarded (see
    // BreakScheduleEditorPanel's doc comment: dropping it from composition is what cancels).
    LaunchedEffect(breakItem.enabled) {
        if (!breakItem.enabled) expanded = false
    }
    val textColor = if (breakItem.enabled) CardForegroundLight else MutedForegroundLight
    val subtextColor = if (breakItem.enabled) ForegroundLight else MutedForegroundLight

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete this break?") },
            text = { Text("This permanently removes \"${breakItem.name}\" and its schedule. This can't be undone.") },
            confirmButton = {
                TextButton(onClick = { showDeleteDialog = false; onDelete() }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
            },
        )
    }

    Box(modifier = modifier.fillMaxWidth()) {
        Row(modifier = Modifier.matchParentSize().clip(RoundedCornerShape(16.dp))) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(MaterialTheme.colorScheme.errorContainer)
                    .padding(horizontal = 24.dp),
                contentAlignment = Alignment.CenterStart,
            ) {
                Icon(imageVector = Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(SecondaryLight)
                    .padding(horizontal = 24.dp),
                contentAlignment = Alignment.CenterEnd,
            ) {
                Icon(imageVector = Icons.Default.Edit, contentDescription = null, tint = SecondaryForegroundLight)
            }
        }
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .swipeableBreakCard(enabled = !expanded, onSwipeRight = { showDeleteDialog = true }, onSwipeLeft = onEdit)
                .clickable(enabled = breakItem.enabled) { expanded = !expanded },
            shape = RoundedCornerShape(16.dp),
            colors = if (breakItem.enabled) {
                CardDefaults.cardColors(containerColor = CardLight, contentColor = CardForegroundLight)
            } else {
                CardDefaults.cardColors(containerColor = MutedLight, contentColor = MutedForegroundLight)
            },
            elevation = CardDefaults.cardElevation(defaultElevation = if (breakItem.enabled) 4.dp else 0.dp),
            border = if (breakItem.enabled) null else CardDefaults.outlinedCardBorder(enabled = true),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .alpha(if (breakItem.enabled) 1f else 0.6f)
                ) {
                    Text(text = breakItem.name, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = textColor)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = formatScheduleSubtext(breakItem.schedule), fontSize = 15.sp, color = subtextColor)
                }
                EnabledToggle(enabled = breakItem.enabled, onToggle = onToggleEnabled)
            }
            if (expanded) {
                HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp), color = BorderLight)
                BreakScheduleEditorPanel(
                    schedule = breakItem.schedule,
                    alertSettings = breakItem.alertSettings,
                    onCancel = { expanded = false },
                    onSave = { schedule, alertSettings -> onSaveSettings(schedule, alertSettings); expanded = false },
                    modifier = Modifier.padding(20.dp),
                )
            }
        }
    }
}

private const val SWIPE_ACTION_COMPLETE_FRACTION = 0.35f
private const val SWIPE_ACTION_SETTLE_DURATION_MILLIS = 200

/**
 * Dragging right past [SWIPE_ACTION_COMPLETE_FRACTION] of the card's width fires [onSwipeRight]
 * (delete); dragging left past the same fraction fires [onSwipeLeft] (edit). The card always
 * springs back to its resting position afterward — [onSwipeRight] only requests confirmation,
 * it doesn't remove the card itself.
 */
@Composable
private fun Modifier.swipeableBreakCard(enabled: Boolean, onSwipeRight: () -> Unit, onSwipeLeft: () -> Unit): Modifier {
    val offsetX = remember { Animatable(0f) }
    var widthPx by remember { mutableFloatStateOf(0f) }
    val scope = rememberCoroutineScope()

    return this
        .onSizeChanged { widthPx = it.width.toFloat() }
        .offset { IntOffset(offsetX.value.roundToInt(), 0) }
        .draggable(
            enabled = enabled,
            orientation = Orientation.Horizontal,
            state = rememberDraggableState { delta ->
                scope.launch { offsetX.snapTo(offsetX.value + delta) }
            },
            onDragStopped = {
                val thresholdPx = widthPx * SWIPE_ACTION_COMPLETE_FRACTION
                when {
                    offsetX.value >= thresholdPx -> onSwipeRight()
                    offsetX.value <= -thresholdPx -> onSwipeLeft()
                }
                offsetX.animateTo(0f, tween(SWIPE_ACTION_SETTLE_DURATION_MILLIS))
            },
        )
}

@Composable
private fun EnabledToggle(enabled: Boolean, onToggle: (Boolean) -> Unit) = ToggleSwitch(enabled, onToggle)

/** A crisp, static description of when a break fires — not a live countdown (see [formatScheduleSubtext] callers). */
internal fun formatScheduleSubtext(schedule: BreakSchedule): String {
    val clauses = mutableListOf(formatDaysClause(schedule.daysOfWeek))
    formatHoursClause(schedule.activeStartHour, schedule.activeEndHour)?.let(clauses::add)
    clauses += formatFrequencyClause(schedule.frequencyMinutes)

    return joinScheduleClauses(clauses)
}

private fun joinScheduleClauses(clauses: List<String>): String {
    val daysClause = clauses.first().replaceFirstChar { it.uppercase() }
    val rest = clauses.drop(1).toMutableList()
    rest[rest.lastIndex] = rest[rest.lastIndex].replaceFirstChar { it.lowercase() }
    return daysClause + " • " + rest.joinToString(", ")
}

private fun formatFrequencyClause(minutes: Int): String = "Every ${formatFrequencyStepperLabel(minutes)}"

private fun formatHoursClause(startHour: Int, endHour: Int): String? {
    if (startHour == 0 && endHour == 23) return null
    val startPeriod = periodOf(startHour)
    val endPeriod = periodOf(endHour)
    return if (startPeriod == endPeriod) {
        "${displayHour(startHour)}–${displayHour(endHour)} $endPeriod"
    } else {
        "${displayHour(startHour)} $startPeriod–${displayHour(endHour)} $endPeriod"
    }
}

private fun formatDaysClause(daysOfWeek: DaysOfWeek): String {
    val days = daysOfWeek.days
    return when {
        daysOfWeek == DaysOfWeek.EVERY_DAY -> "every day"
        days == WEEKDAYS -> "weekdays"
        days.size == 1 -> "${days.first().displayName()}s only"
        else -> days.sortedBy { it.value }.joinToString(", ") { it.abbreviation() }
    }
}

private val WEEKDAYS = setOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY)

private fun displayHour(hour: Int): Int = if (hour % 12 == 0) 12 else hour % 12

private fun periodOf(hour: Int): String = if (hour < 12) "AM" else "PM"

private fun DayOfWeek.displayName(): String = name.lowercase().replaceFirstChar { it.uppercase() }

private fun DayOfWeek.abbreviation(): String = name.take(3).lowercase().replaceFirstChar { it.uppercase() }

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, backgroundColor = 0xFFF1F3F1)
@Composable
private fun CustomizeBreaksScreenPreview() {
    val allDay = BreakSchedule(activeStartHour = 0, activeEndHour = 23)
    MicroMovesTheme {
        CustomizeBreaksContent(
            breaks = listOf(
                Break(id = 1, name = "Palming Eye Exercise", schedule = allDay.copy(frequencyMinutes = 15)),
                Break(
                    id = 2,
                    name = "Neck Stretches",
                    schedule = BreakSchedule(
                        frequencyMinutes = 45,
                        activeStartHour = 9,
                        activeEndHour = 19,
                        daysOfWeek = DaysOfWeek(WEEKDAYS),
                    ),
                ),
                Break(
                    id = 3,
                    name = "Thursday Reset",
                    schedule = BreakSchedule(
                        frequencyMinutes = 30,
                        activeStartHour = 6,
                        activeEndHour = 9,
                        daysOfWeek = DaysOfWeek(setOf(DayOfWeek.THURSDAY)),
                    ),
                ),
                Break(id = 4, name = "Stand & Walk", schedule = allDay.copy(frequencyMinutes = 60)),
                Break(id = 5, name = "Wrist Stretches", enabled = false, schedule = allDay.copy(frequencyMinutes = 120)),
            )
        )
    }
}
