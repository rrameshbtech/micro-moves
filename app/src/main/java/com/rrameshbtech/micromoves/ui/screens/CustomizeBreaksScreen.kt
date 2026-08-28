package com.rrameshbtech.micromoves.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rrameshbtech.micromoves.data.Break
import com.rrameshbtech.micromoves.data.BreakSchedule
import com.rrameshbtech.micromoves.data.DaysOfWeek
import com.rrameshbtech.micromoves.ui.components.BreakScheduleEditorPanel
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
import com.rrameshbtech.micromoves.viewmodel.CustomizeBreaksViewModel
import java.time.DayOfWeek

@Composable
fun CustomizeBreaksScreen(
    viewModel: CustomizeBreaksViewModel = viewModel(),
    onBack: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val breaks by viewModel.breaks.collectAsState()
    CustomizeBreaksContent(
        breaks = breaks,
        onBack = onBack,
        onToggleEnabled = viewModel::setEnabled,
        onSaveSchedule = viewModel::updateSchedule,
        modifier = modifier,
    )
}

@Composable
private fun CustomizeBreaksContent(
    breaks: List<Break>,
    onBack: () -> Unit = {},
    onToggleEnabled: (Break, Boolean) -> Unit = { _, _ -> },
    onSaveSchedule: (Break, BreakSchedule) -> Unit = { _, _ -> },
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
                    text = "Customize Breaks",
                    modifier = Modifier.align(Alignment.Center),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = CardForegroundLight,
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
                    onSaveSchedule = { schedule -> onSaveSchedule(breakItem, schedule) },
                    modifier = Modifier.animateItem(),
                )
            }

            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }
}

@Composable
fun CustomizeBreakCard(
    breakItem: Break,
    onToggleEnabled: (Boolean) -> Unit,
    onSaveSchedule: (BreakSchedule) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    val textColor = if (breakItem.enabled) CardForegroundLight else MutedForegroundLight
    val subtextColor = if (breakItem.enabled) ForegroundLight else MutedForegroundLight

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
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
                onCancel = { expanded = false },
                onSave = { schedule -> onSaveSchedule(schedule); expanded = false },
                modifier = Modifier.padding(20.dp),
            )
        }
    }
}

@Composable
private fun EnabledToggle(enabled: Boolean, onToggle: (Boolean) -> Unit) {
    Box(
        modifier = Modifier
            .size(60.dp)
            .clickable(role = Role.Switch, onClick = { onToggle(!enabled) }),
        contentAlignment = Alignment.Center,
    ) {
        Switch(
            checked = enabled,
            onCheckedChange = null,
            colors = SwitchDefaults.colors(
                checkedTrackColor = PrimaryLight,
                checkedThumbColor = PrimaryForegroundLight,
                uncheckedTrackColor = BorderLight,
                uncheckedThumbColor = MutedForegroundLight,
            ),
        )
    }
}

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

private fun formatFrequencyClause(minutes: Int): String = when {
    minutes < 60 -> "Every $minutes min"
    minutes % 60 == 0 -> "Every ${minutes / 60} hr"
    else -> "Every $minutes min"
}

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
