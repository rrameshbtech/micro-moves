package com.rrameshbtech.micromoves.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rrameshbtech.micromoves.data.Break
import com.rrameshbtech.micromoves.data.BreakState
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

@Composable
fun BreaksListScreen(modifier: Modifier = Modifier) {
    val now = System.currentTimeMillis()
    val mockBreaks = listOf(
        Break(id = 1, name = "Palming Eye Exercise", state = BreakState.Active, nextTriggerTime = now + 15 * 60_000L),
        Break(id = 2, name = "Neck Stretches", state = BreakState.Active, nextTriggerTime = now + 45 * 60_000L),
        Break(id = 3, name = "Stand & Walk", state = BreakState.Active, nextTriggerTime = now + 120 * 60_000L),
        Break(id = 4, name = "Shoulder Rolls", state = BreakState.PausedForOccurrence(occurrences = 2)),
    )

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
                    text = "${mockBreaks.size} breaks scheduled",
                    fontSize = 15.sp,
                    color = ForegroundLight,
                    modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
                )
            }

            items(mockBreaks) { breakItem ->
                when (breakItem.state) {
                    is BreakState.Active -> ActiveBreakCard(breakItem = breakItem)
                    is BreakState.Paused,
                    is BreakState.PausedForOccurrence -> PausedBreakCard(breakItem)
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
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 80.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = CardLight,
            contentColor = CardForegroundLight
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        val minutesUntilNext = ((breakItem.nextTriggerTime - System.currentTimeMillis()) / 60_000L)
            .coerceAtLeast(0L).toInt()
        Row(
            modifier = Modifier
                .fillMaxSize()
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
                        text = "in $minutesUntilNext ${if (minutesUntilNext >= 60) "hrs" else "mins"}",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = PrimaryLight
                    )
                }
            }
            Button(
                onClick = { /* Pause break */ },
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
    }
}

@Composable
fun PausedBreakCard(
    breakItem: Break,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 80.dp)
            .alpha(0.6f),
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
                .fillMaxSize()
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
                            .background(color = BorderLight, shape = RoundedCornerShape(50.dp))
                    )
                    Text(
                        text = when (val s = breakItem.state) {
                            is BreakState.PausedForOccurrence -> "Paused for ${s.occurrences} cycles"
                            else -> "Paused"
                        },
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = MutedForegroundLight
                    )
                }
            }
            Button(
                onClick = { /* Resume break */ },
                modifier = Modifier
                    .height(40.dp)
                    .widthIn(min = 80.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CardLight,
                    contentColor = ForegroundLight
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
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF1F3F1)
@Composable
fun BreaksListScreenPreview() {
    MicroMovesTheme {
        BreaksListScreen()
    }
}