package com.rrameshbtech.micromoves.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rrameshbtech.micromoves.data.BreakSchedule
import com.rrameshbtech.micromoves.data.DaysOfWeek
import com.rrameshbtech.micromoves.ui.theme.CardForegroundLight
import com.rrameshbtech.micromoves.ui.theme.PrimaryForegroundLight
import com.rrameshbtech.micromoves.ui.theme.PrimaryLight
import com.rrameshbtech.micromoves.ui.theme.SecondaryForegroundLight
import com.rrameshbtech.micromoves.ui.theme.SecondaryLight
import java.time.DayOfWeek

private const val FREQUENCY_STEP_MINUTES = 30
private const val MIN_FREQUENCY_MINUTES = 30
private const val MAX_FREQUENCY_MINUTES = 480

/**
 * Local edit state seeded from [schedule] and only committed via [onSave] — [onCancel] simply
 * drops this composable from composition, discarding whatever was edited.
 */
@Composable
fun BreakScheduleEditorPanel(
    schedule: BreakSchedule,
    onCancel: () -> Unit,
    onSave: (BreakSchedule) -> Unit,
    modifier: Modifier = Modifier,
) {
    var frequencyMinutes by remember { mutableIntStateOf(schedule.frequencyMinutes) }
    var startHour by remember { mutableIntStateOf(schedule.activeStartHour) }
    var endHour by remember { mutableIntStateOf(schedule.activeEndHour) }
    var days by remember { mutableStateOf(schedule.daysOfWeek) }

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(20.dp)) {
        Text(text = "Every", fontSize = 15.sp, fontWeight = FontWeight.Medium, color = CardForegroundLight)
        StepperControl(
            valueText = formatFrequencyStepperLabel(frequencyMinutes),
            onDecrement = { frequencyMinutes = (frequencyMinutes - FREQUENCY_STEP_MINUTES).coerceAtLeast(MIN_FREQUENCY_MINUTES) },
            onIncrement = { frequencyMinutes = (frequencyMinutes + FREQUENCY_STEP_MINUTES).coerceAtMost(MAX_FREQUENCY_MINUTES) },
            decrementEnabled = frequencyMinutes > MIN_FREQUENCY_MINUTES,
            incrementEnabled = frequencyMinutes < MAX_FREQUENCY_MINUTES,
        )

        Column {
            Text(text = "Active Hours", fontSize = 15.sp, fontWeight = FontWeight.Medium, color = CardForegroundLight)
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                StepperControl(
                    label = "Start",
                    valueText = formatHourLabel(startHour),
                    onDecrement = { startHour -= 1 },
                    onIncrement = { startHour += 1 },
                    decrementEnabled = startHour > 0,
                    incrementEnabled = startHour < endHour - 1,
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                StepperControl(
                    label = "End",
                    valueText = formatHourLabel(endHour),
                    onDecrement = { endHour -= 1 },
                    onIncrement = { endHour += 1 },
                    decrementEnabled = endHour > startHour + 1,
                    incrementEnabled = endHour < 23,
                )
            }
        }

        Column {
            Text(text = "Days", fontSize = 15.sp, fontWeight = FontWeight.Medium, color = CardForegroundLight)
            Spacer(modifier = Modifier.height(8.dp))
            DayOfWeekChipRow(selectedDays = days, onToggleDay = { day -> days = days.toggling(day) })
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
            ) {
                Text(text = "Cancel", fontSize = 15.sp, fontWeight = FontWeight.Medium)
            }
            Button(
                onClick = { onSave(BreakSchedule(frequencyMinutes, startHour, endHour, days)) },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryLight, contentColor = PrimaryForegroundLight),
                shape = RoundedCornerShape(12.dp),
            ) {
                Text(text = "Save", fontSize = 15.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
private fun StepperControl(
    label: String = "",
    valueText: String,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit,
    decrementEnabled: Boolean = true,
    incrementEnabled: Boolean = true,
) {
    Column {
        if(label.isNotBlank()) {
            Text(
                text = label,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = CardForegroundLight
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StepperButton(glyph = "−", enabled = decrementEnabled, onClick = onDecrement)
            Text(
                text = valueText,
                modifier = Modifier.widthIn(min = 64.dp),
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = CardForegroundLight,
                textAlign = TextAlign.Center,
            )
            StepperButton(glyph = "+", enabled = incrementEnabled, onClick = onIncrement)
        }
    }
}

@Composable
private fun StepperButton(glyph: String, enabled: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(60.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(SecondaryLight.copy(alpha = if (enabled) 1f else 0.4f))
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = glyph, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = SecondaryForegroundLight)
    }
}

@Composable
private fun DayOfWeekChipRow(selectedDays: DaysOfWeek, onToggleDay: (DayOfWeek) -> Unit) {
    val allDays = DayOfWeek.entries
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            allDays.take(4).forEach { day ->
                DayChip(day = day, selected = selectedDays.contains(day), onClick = { onToggleDay(day) })
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            allDays.drop(4).forEach { day ->
                DayChip(day = day, selected = selectedDays.contains(day), onClick = { onToggleDay(day) })
            }
        }
    }
}

@Composable
private fun DayChip(day: DayOfWeek, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(60.dp)
            .clip(CircleShape)
            .background(if (selected) PrimaryLight else SecondaryLight)
            .clickable(onClickLabel = day.fullName(), onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = day.letter(),
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (selected) PrimaryForegroundLight else SecondaryForegroundLight,
        )
    }
}

internal fun DaysOfWeek.toggling(day: DayOfWeek): DaysOfWeek =
    DaysOfWeek(if (contains(day)) days - day else days + day)

private fun DayOfWeek.letter(): String = when (this) {
    DayOfWeek.MONDAY -> "M"
    DayOfWeek.TUESDAY -> "T"
    DayOfWeek.WEDNESDAY -> "W"
    DayOfWeek.THURSDAY -> "T"
    DayOfWeek.FRIDAY -> "F"
    DayOfWeek.SATURDAY -> "S"
    DayOfWeek.SUNDAY -> "S"
}

private fun DayOfWeek.fullName(): String = name.lowercase().replaceFirstChar { it.uppercase() }

private fun formatHourLabel(hour: Int): String {
    val period = if (hour < 12) "AM" else "PM"
    val displayHour = if (hour % 12 == 0) 12 else hour % 12
    return "$displayHour $period"
}

private fun formatFrequencyStepperLabel(minutes: Int): String = when {
    minutes < 60 -> "$minutes min"
    minutes % 60 == 0 -> "${minutes / 60} hr"
    else -> "$minutes min"
}
