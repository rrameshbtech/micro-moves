package com.rrameshbtech.micromoves.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rrameshbtech.micromoves.data.AlertSettings
import com.rrameshbtech.micromoves.data.BreakSchedule
import com.rrameshbtech.micromoves.data.Exercise
import com.rrameshbtech.micromoves.ui.components.BreakScheduleEditorPanel
import com.rrameshbtech.micromoves.ui.theme.BackgroundLight
import com.rrameshbtech.micromoves.ui.theme.BorderLight
import com.rrameshbtech.micromoves.ui.theme.CardForegroundLight
import com.rrameshbtech.micromoves.ui.theme.CardLight
import com.rrameshbtech.micromoves.ui.theme.MutedForegroundLight
import com.rrameshbtech.micromoves.ui.theme.PrimaryForegroundLight
import com.rrameshbtech.micromoves.ui.theme.PrimaryLight
import com.rrameshbtech.micromoves.ui.theme.SecondaryForegroundLight
import com.rrameshbtech.micromoves.ui.theme.SecondaryLight
import com.rrameshbtech.micromoves.viewmodel.CreateBreakViewModel
import kotlinx.coroutines.launch

private const val MAX_EXERCISES_PER_BREAK = 3
private const val UNTITLED_BREAK_TITLE = "[untitled break]"

@Composable
fun CreateBreakScreen(
    viewModel: CreateBreakViewModel = viewModel(),
    onBack: () -> Unit = {},
    onSaved: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val catalog by viewModel.catalog.collectAsState()
    val scope = rememberCoroutineScope()
    CreateBreakContent(
        catalog = catalog,
        onBack = onBack,
        onCreate = { name, description, schedule, alertSettings, exerciseIds ->
            scope.launch {
                viewModel.createBreak(name, description, schedule, alertSettings, exerciseIds)
                onSaved()
            }
        },
        modifier = modifier,
    )
}

@Composable
private fun CreateBreakContent(
    catalog: List<Exercise>,
    onBack: () -> Unit = {},
    onCreate: (String, String, BreakSchedule, AlertSettings, List<Long>) -> Unit = { _, _, _, _, _ -> },
    modifier: Modifier = Modifier,
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selected by remember { mutableStateOf(listOf<Exercise>()) }
    var schedule by remember { mutableStateOf(BreakSchedule()) }
    var alertSettings by remember { mutableStateOf(AlertSettings()) }
    var showDiscardDialog by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    val availableExercises = catalog.filterNot { exercise -> selected.any { it.id == exercise.id } }
    val hasUnsavedChanges = name.isNotBlank() || description.isNotBlank() || selected.isNotEmpty() ||
        schedule != BreakSchedule() || alertSettings != AlertSettings()
    val requestBack = { if (hasUnsavedChanges) showDiscardDialog = true else onBack() }

    BackHandler(onBack = requestBack)

    if (showDiscardDialog) {
        AlertDialog(
            onDismissRequest = { showDiscardDialog = false },
            title = { Text("Discard this break?") },
            text = { Text("You have unsaved changes. Leaving now will lose them.") },
            confirmButton = {
                TextButton(onClick = { showDiscardDialog = false; onBack() }) { Text("Discard") }
            },
            dismissButton = {
                TextButton(onClick = { showDiscardDialog = false }) { Text("Keep Editing") }
            },
        )
    }

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
                    .statusBarsPadding()
                    .padding(horizontal = 12.dp, vertical = 12.dp)
            ) {
                IconButton(onClick = requestBack, modifier = Modifier.align(Alignment.CenterStart).size(48.dp)) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = CardForegroundLight)
                }
                Text(
                    text = name.ifBlank { UNTITLED_BREAK_TITLE },
                    modifier = Modifier
                        .align(Alignment.Center)
                        .fillMaxWidth()
                        .padding(horizontal = 56.dp),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = CardForegroundLight,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = BackgroundLight, shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                    .padding(24.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(
                        modifier = Modifier
                            .clickable {
                                scope.launch { listState.animateScrollToItem(listState.layoutInfo.totalItemsCount - 1) }
                            }
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = formatScheduleSubtext(schedule),
                            modifier = Modifier.padding(end = 6.dp),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = MutedForegroundLight,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit schedule",
                            tint = MutedForegroundLight,
                            modifier = Modifier.size(16.dp),
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { onCreate(name.trim(), description.trim(), schedule, alertSettings, selected.map { it.id }) },
                        enabled = name.isNotBlank() && selected.isNotEmpty(),
                        modifier = Modifier.fillMaxWidth().height(60.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryLight, contentColor = PrimaryForegroundLight),
                        shape = RoundedCornerShape(16.dp),
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                        Text(text = "Create Break", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        },
    ) { innerPadding ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
                .background(BackgroundLight),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "Name", fontSize = 15.sp, fontWeight = FontWeight.Medium, color = CardForegroundLight)
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("e.g., Eye Relief Exercise") },
                        singleLine = true,
                    )
                }
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "Description", fontSize = 15.sp, fontWeight = FontWeight.Medium, color = CardForegroundLight)
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("What does this break help with?") },
                        minLines = 3,
                    )
                }
            }

            item {
                Text(
                    text = "Exercises (up to $MAX_EXERCISES_PER_BREAK)",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = CardForegroundLight,
                )
            }

            itemsIndexed(selected, key = { _, exercise -> exercise.id }) { index, exercise ->
                SelectedExerciseRow(
                    position = index + 1,
                    exercise = exercise,
                    canMoveUp = index > 0,
                    canMoveDown = index < selected.lastIndex,
                    onMoveUp = { selected = selected.moved(index, index - 1) },
                    onMoveDown = { selected = selected.moved(index, index + 1) },
                    onRemove = { selected = selected - exercise },
                )
            }

            if (availableExercises.isNotEmpty() && selected.size < MAX_EXERCISES_PER_BREAK) {
                item {
                    Text(text = "Add from catalog", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = MutedForegroundLight)
                }
                items(availableExercises, key = { it.id }) { exercise ->
                    CatalogExerciseRow(exercise = exercise, onAdd = { selected = selected + exercise })
                }
            }

            item {
                ScheduleSection(
                    schedule = schedule,
                    alertSettings = alertSettings,
                    onChange = { newSchedule, newAlertSettings ->
                        schedule = newSchedule
                        alertSettings = newAlertSettings
                    },
                )
            }

            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }
}

@Composable
private fun SelectedExerciseRow(
    position: Int,
    exercise: Exercise,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onRemove: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardLight, contentColor = CardForegroundLight),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = "$position.", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MutedForegroundLight)
            Text(
                text = exercise.name,
                modifier = Modifier.weight(1f).padding(start = 8.dp),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = CardForegroundLight,
            )
            IconButton(onClick = onMoveUp, enabled = canMoveUp, modifier = Modifier.size(48.dp)) {
                Icon(imageVector = Icons.Default.KeyboardArrowUp, contentDescription = "Move ${exercise.name} up")
            }
            IconButton(onClick = onMoveDown, enabled = canMoveDown, modifier = Modifier.size(48.dp)) {
                Icon(imageVector = Icons.Default.KeyboardArrowDown, contentDescription = "Move ${exercise.name} down")
            }
            IconButton(onClick = onRemove, modifier = Modifier.size(48.dp)) {
                Icon(imageVector = Icons.Default.Close, contentDescription = "Remove ${exercise.name}", tint = MutedForegroundLight)
            }
        }
    }
}

@Composable
private fun CatalogExerciseRow(exercise: Exercise, onAdd: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardLight, contentColor = CardForegroundLight),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = exercise.name, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = CardForegroundLight)
                if (exercise.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = exercise.description, fontSize = 14.sp, color = MutedForegroundLight)
                }
            }
            IconButton(
                onClick = onAdd,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(SecondaryLight),
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add ${exercise.name}", tint = SecondaryForegroundLight)
            }
        }
    }
}

@Composable
private fun ScheduleSection(
    schedule: BreakSchedule,
    alertSettings: AlertSettings,
    onChange: (BreakSchedule, AlertSettings) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardLight, contentColor = CardForegroundLight),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(text = "Schedule", fontSize = 15.sp, fontWeight = FontWeight.Medium, color = CardForegroundLight)
        }
        HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp), color = BorderLight)
        BreakScheduleEditorPanel(
            schedule = schedule,
            alertSettings = alertSettings,
            onCancel = {},
            onSave = { _, _ -> },
            showActions = false,
            onChange = onChange,
            modifier = Modifier.padding(20.dp),
        )
    }
}

/** Reorders [from] to [to], a no-op if [to] would fall outside the list's bounds. */
internal fun <T> List<T>.moved(from: Int, to: Int): List<T> =
    if (to !in indices) this else toMutableList().apply { add(to, removeAt(from)) }
