package com.rrameshbtech.micromoves.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.rrameshbtech.micromoves.data.BreakSchedule
import com.rrameshbtech.micromoves.data.BreakState
import com.rrameshbtech.micromoves.data.resumeOccurrenceAfterSkipping
import com.rrameshbtech.micromoves.ui.theme.CardForegroundLight
import com.rrameshbtech.micromoves.ui.theme.MutedForegroundLight
import com.rrameshbtech.micromoves.ui.theme.PrimaryForegroundLight
import com.rrameshbtech.micromoves.ui.theme.PrimaryLight
import java.time.Instant
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

private const val DEFAULT_PAUSE_OCCURRENCES = 2
private const val DEFAULT_MAX_PAUSE_OCCURRENCES = 20
private const val DEFAULT_PAUSE_UNTIL_HOURS_FROM_NOW = 1L

private val DATE_LABEL_FORMAT = DateTimeFormatter.ofPattern("MMM d")
private val TIME_LABEL_FORMAT = DateTimeFormatter.ofPattern("h:mm a")
private val RESUME_PREVIEW_FORMAT = DateTimeFormatter.ofPattern("MMM d, h:mm a")

private enum class PauseMode { OCCURRENCES, UNTIL }

/**
 * The two ways a break can be paused, [OCCURRENCES] and [UNTIL], are mutually exclusive —
 * only the selected mode's fields feed [onSave].
 */
@Composable
fun PauseOptionsPanel(
    schedule: BreakSchedule,
    now: LocalDateTime,
    onCancel: () -> Unit,
    onSave: (BreakState) -> Unit,
    modifier: Modifier = Modifier,
    initialState: BreakState = BreakState.PausedForOccurrences(DEFAULT_PAUSE_OCCURRENCES),
    maxOccurrences: Int = DEFAULT_MAX_PAUSE_OCCURRENCES,
) {
    var mode by remember { mutableStateOf((initialState as? BreakState.PausedUntil)?.let { PauseMode.UNTIL } ?: PauseMode.OCCURRENCES) }
    var occurrenceCount by remember {
        mutableIntStateOf((initialState as? BreakState.PausedForOccurrences)?.occurrences ?: DEFAULT_PAUSE_OCCURRENCES)
    }
    var untilDateTime by remember {
        mutableStateOf(
            (initialState as? BreakState.PausedUntil)
                ?.let { Instant.ofEpochMilli(it.timestampMillis).atZone(ZoneId.systemDefault()).toLocalDateTime() }
                ?: now.plusHours(DEFAULT_PAUSE_UNTIL_HOURS_FROM_NOW)
        )
    }

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        PauseModeOption(
            label = "Skip next occurrences",
            selected = mode == PauseMode.OCCURRENCES,
            onSelect = { mode = PauseMode.OCCURRENCES },
        ) {
            OccurrencesSlider(
                count = occurrenceCount,
                maxOccurrences = maxOccurrences,
                resumeAt = schedule.resumeOccurrenceAfterSkipping(now, occurrenceCount),
                onCountChange = { occurrenceCount = it },
            )
        }

        PauseModeOption(
            label = "Pause until",
            selected = mode == PauseMode.UNTIL,
            onSelect = { mode = PauseMode.UNTIL },
        ) {
            PausedUntilPicker(
                dateTime = untilDateTime,
                now = now,
                onDateTimeChange = { untilDateTime = it },
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
            ) {
                Text("Cancel", fontSize = 15.sp, fontWeight = FontWeight.Medium)
            }
            Button(
                onClick = {
                    val state = when (mode) {
                        PauseMode.OCCURRENCES -> BreakState.PausedForOccurrences(occurrenceCount)
                        PauseMode.UNTIL -> BreakState.PausedUntil(
                            untilDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                        )
                    }
                    onSave(state)
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryLight,
                    contentColor = PrimaryForegroundLight,
                ),
                shape = RoundedCornerShape(12.dp),
            ) {
                Text("Pause", fontSize = 15.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
private fun PauseModeOption(
    label: String,
    selected: Boolean,
    onSelect: () -> Unit,
    content: @Composable () -> Unit,
) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(selected = selected, onClick = onSelect, colors = RadioButtonDefaults.colors(selectedColor = PrimaryLight))
            Text(text = label, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = CardForegroundLight)
        }
        if (selected) {
            Column(modifier = Modifier.padding(start = 44.dp, top = 4.dp)) {
                content()
            }
        }
    }
}

@Composable
private fun OccurrencesSlider(
    count: Int,
    maxOccurrences: Int,
    resumeAt: LocalDateTime,
    onCountChange: (Int) -> Unit,
) {
    Column {
        Text(
            text = "Skip next $count ${pluralize(count.toLong(), "occurrence")}",
            fontSize = 15.sp,
            color = CardForegroundLight,
        )
        Slider(
            value = count.toFloat(),
            onValueChange = { onCountChange(it.roundToInt()) },
            valueRange = 1f..maxOccurrences.toFloat(),
            steps = (maxOccurrences - 2).coerceAtLeast(0),
            colors = SliderDefaults.colors(thumbColor = PrimaryLight, activeTrackColor = PrimaryLight),
        )
        Text(
            text = "Resumes ${RESUME_PREVIEW_FORMAT.format(resumeAt)}",
            fontSize = 13.sp,
            color = MutedForegroundLight,
        )
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
private fun PausedUntilPicker(
    dateTime: LocalDateTime,
    now: LocalDateTime,
    onDateTimeChange: (LocalDateTime) -> Unit,
) {
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    // Never let a confirmed pick land before `now` — clamping here means the TimePicker's lack
    // of per-minute disabling (unlike DatePicker's selectableDates) can't smuggle a past pick through.
    fun onDateTimeChangeClamped(candidate: LocalDateTime) {
        onDateTimeChange(if (candidate.isBefore(now)) now else candidate)
    }

    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedButton(onClick = { showDatePicker = true }, shape = RoundedCornerShape(12.dp)) {
            Text(DATE_LABEL_FORMAT.format(dateTime), fontSize = 15.sp)
        }
        OutlinedButton(onClick = { showTimePicker = true }, shape = RoundedCornerShape(12.dp)) {
            Text(TIME_LABEL_FORMAT.format(dateTime), fontSize = 15.sp)
        }
    }

    if (showDatePicker) {
        val todayUtcMillis = remember(now) { now.toLocalDate().atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli() }
        val selectableDates = remember(todayUtcMillis) {
            object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean = utcTimeMillis >= todayUtcMillis
            }
        }
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = dateTime.toLocalDate().atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli(),
            selectableDates = selectableDates,
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val newDate = Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()
                        onDateTimeChangeClamped(LocalDateTime.of(newDate, dateTime.toLocalTime()))
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancel") } },
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(initialHour = dateTime.hour, initialMinute = dateTime.minute)
        Dialog(onDismissRequest = { showTimePicker = false }) {
            Surface(shape = RoundedCornerShape(16.dp)) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    TimePicker(state = timePickerState)
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        TextButton(onClick = { showTimePicker = false }) { Text("Cancel") }
                        TextButton(onClick = {
                            onDateTimeChangeClamped(
                                LocalDateTime.of(dateTime.toLocalDate(), LocalTime.of(timePickerState.hour, timePickerState.minute))
                            )
                            showTimePicker = false
                        }) { Text("OK") }
                    }
                }
            }
        }
    }
}

private fun pluralize(count: Long, unit: String): String = if (count == 1L) unit else "${unit}s"
