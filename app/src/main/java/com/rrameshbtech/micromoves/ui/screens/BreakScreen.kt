package com.rrameshbtech.micromoves.ui.screens

import android.app.Application
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.rrameshbtech.micromoves.data.BreakPlaybackItem
import com.rrameshbtech.micromoves.data.Slide
import com.rrameshbtech.micromoves.ui.theme.BackgroundLight
import com.rrameshbtech.micromoves.ui.theme.CardForegroundLight
import com.rrameshbtech.micromoves.ui.theme.ForegroundLight
import com.rrameshbtech.micromoves.ui.theme.MicroMovesTheme
import com.rrameshbtech.micromoves.ui.theme.MutedForegroundLight
import com.rrameshbtech.micromoves.ui.theme.PrimaryForegroundLight
import com.rrameshbtech.micromoves.ui.theme.PrimaryLight
import com.rrameshbtech.micromoves.ui.theme.SecondaryLight
import com.rrameshbtech.micromoves.viewmodel.BreakUiState
import com.rrameshbtech.micromoves.viewmodel.BreakViewModel

@Composable
fun BreakScreen(
    breakId: Long,
    viewModel: BreakViewModel = viewModel(
        factory = BreakViewModel.factory(LocalContext.current.applicationContext as Application, breakId)
    ),
    onDone: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()
    BreakContent(
        uiState = uiState,
        onSkipExercise = viewModel::skipCurrentExercise,
        onDone = onDone,
        modifier = modifier,
    )
}

@Composable
private fun BreakContent(
    uiState: BreakUiState,
    onSkipExercise: () -> Unit = {},
    onDone: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize().background(BackgroundLight)) {
        when (uiState) {
            is BreakUiState.Loading -> Unit
            is BreakUiState.NotFound -> LaunchedEffect(Unit) { onDone() }
            is BreakUiState.Playing -> AnimatedContent(
                targetState = uiState.item,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "break-playback-item",
                modifier = Modifier.fillMaxSize(),
            ) { item ->
                when (item) {
                    is BreakPlaybackItem.ExerciseIntro -> ExerciseIntroContent(exerciseName = item.exerciseName)
                    is BreakPlaybackItem.SlideItem -> SlideContent(
                        slide = item.slide,
                        progress = uiState.elapsedMs.toFloat() / uiState.durationMs.toFloat(),
                        remainingMs = (uiState.durationMs - uiState.elapsedMs).coerceAtLeast(0L),
                        onLongPressSkip = onSkipExercise,
                    )
                    BreakPlaybackItem.Congrats -> CongratsContent(onDone = onDone)
                }
            }
        }
    }
}

@Composable
private fun SlideContent(
    slide: Slide,
    progress: Float,
    remainingMs: Long,
    onLongPressSkip: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val hasImage = slide.imageUri != null
    Column(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) { detectTapGestures(onLongPress = { onLongPressSkip() }) }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .background(SecondaryLight)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress.coerceIn(0f, 1f))
                    .height(4.dp)
                    .background(PrimaryLight)
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            if (hasImage) {
                AsyncImage(
                    model = slide.imageUri,
                    contentDescription = slide.description,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .widthIn(max = 320.dp)
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(24.dp))
                        .background(SecondaryLight),
                )
                Spacer(modifier = Modifier.height(32.dp))
            }

            Text(
                text = slide.description,
                fontSize = if (hasImage) 28.sp else 36.sp,
                fontWeight = FontWeight.SemiBold,
                color = CardForegroundLight,
                textAlign = TextAlign.Center,
            )
            if (slide.subText.isNotBlank()) {
                Text(
                    text = slide.subText,
                    fontSize = if (hasImage) 17.sp else 22.sp,
                    color = ForegroundLight,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 12.dp),
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = formatCountdown(remainingMs),
                fontSize = 60.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryLight,
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(Color.Transparent, BackgroundLight)))
                .padding(vertical = 24.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "Hold to skip to next exercise",
                fontSize = 15.sp,
                color = MutedForegroundLight,
            )
        }
    }
}

@Composable
private fun ExerciseIntroContent(exerciseName: String, modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "Next up", fontSize = 18.sp, color = ForegroundLight)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = exerciseName,
                fontSize = 32.sp,
                fontWeight = FontWeight.SemiBold,
                color = CardForegroundLight,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun CongratsContent(onDone: () -> Unit = {}, modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Filled.ThumbUp,
                contentDescription = null,
                tint = PrimaryLight,
                modifier = Modifier.size(64.dp),
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "Nice work!", fontSize = 28.sp, fontWeight = FontWeight.SemiBold, color = CardForegroundLight)
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = onDone,
                modifier = Modifier.fillMaxWidth().height(60.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryLight, contentColor = PrimaryForegroundLight),
                shape = RoundedCornerShape(16.dp),
            ) {
                Text(text = "Done", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

internal fun formatCountdown(remainingMs: Long): String {
    val totalSeconds = (remainingMs / 1000).coerceAtLeast(0)
    return "${totalSeconds / 60}:${(totalSeconds % 60).toString().padStart(2, '0')}"
}

@Preview(showBackground = true, backgroundColor = 0xFFF1F3F1)
@Composable
private fun SlideContentWithImagePreview() {
    MicroMovesTheme {
        SlideContent(
            slide = Slide(
                imageUri = "file:///android_asset/images/exercises/chest-opener/step-1.png",
                description = "Preparation Posture",
                subText = "Sit up straight on the front edge of your chair with your feet flat on the floor.",
                durationMs = 5000,
            ),
            progress = 0.4f,
            remainingMs = 3000,
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF1F3F1)
@Composable
private fun SlideContentNoImagePreview() {
    MicroMovesTheme {
        SlideContent(
            slide = Slide(
                description = "Breathe deeply",
                subText = "In through the nose, out through the mouth.",
                durationMs = 10000,
            ),
            progress = 0.7f,
            remainingMs = 3000,
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF1F3F1)
@Composable
private fun ExerciseIntroContentPreview() {
    MicroMovesTheme { ExerciseIntroContent(exerciseName = "Neck Stretches") }
}

@Preview(showBackground = true, backgroundColor = 0xFFF1F3F1)
@Composable
private fun CongratsContentPreview() {
    MicroMovesTheme { CongratsContent() }
}
