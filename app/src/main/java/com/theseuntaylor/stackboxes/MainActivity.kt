package com.theseuntaylor.stackboxes

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Scaffold
import androidx.compose.material.SnackbarDuration
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.theseuntaylor.stackboxes.ui.theme.StackBoxesTheme
import com.theseuntaylor.stackboxes.ui.theme.activeColor
import com.theseuntaylor.stackboxes.ui.theme.inactiveColor
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            StackBoxesTheme {
                val snackbarHostState = remember { SnackbarHostState() }
                var difficulty by rememberSaveable { mutableStateOf(Difficulty.Easy) }
                // A new round state per difficulty; the picker is locked while a round runs.
                val game = remember(difficulty) { GameState(difficulty) }

                Scaffold(
                    snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                ) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .systemBarsPadding()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        DifficultyPicker(
                            selected = difficulty,
                            enabled = game.phase == Phase.Idle,
                            onSelect = { difficulty = it }
                        )
                        Spacer(Modifier.height(16.dp))
                        Controls(game = game)
                        Spacer(Modifier.height(16.dp))
                        Boxes(game = game, snackbarHostState = snackbarHostState)
                    }
                }
            }
        }
    }
}

@Composable
fun DifficultyPicker(selected: Difficulty, enabled: Boolean, onSelect: (Difficulty) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Difficulty.values().forEach { difficulty ->
            val isSelected = difficulty == selected
            OutlinedButton(
                onClick = { onSelect(difficulty) },
                enabled = enabled,
                colors = ButtonDefaults.outlinedButtonColors(
                    backgroundColor = if (isSelected) activeColor else inactiveColor
                )
            ) {
                Text(difficulty.label)
            }
        }
    }
}

@Composable
fun Controls(game: GameState) {
    val coroutineScope = rememberCoroutineScope()

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = when (game.phase) {
                Phase.Idle -> when (game.result) {
                    null -> "Repeat the sequence, starting with ${game.difficulty.startLength} boxes"
                    Result.GameOver -> "Game over!"
                    Result.Cleared -> "You cleared ${game.difficulty.label}!"
                }
                Phase.Showing ->
                    "${if (game.retrying) "Try again! " else ""}Round ${game.round}: watch the sequence..."
                Phase.Input -> "Your turn: ${game.stack.size}/${game.sequence.size}"
                Phase.Unwinding -> "Unstacking..."
            },
            fontSize = 18.sp,
            textAlign = TextAlign.Center
        )
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(text = "Score: ${game.score}", fontSize = 16.sp)
            Text(text = "Lives: ${game.lives}", fontSize = 16.sp)
        }
        Spacer(Modifier.height(8.dp))
        Button(
            onClick = { coroutineScope.launch { game.start() } },
            enabled = game.phase == Phase.Idle
        ) {
            Text("Start")
        }
    }
}

@Composable
fun Boxes(game: GameState, snackbarHostState: SnackbarHostState) {
    val coroutineScope = rememberCoroutineScope()
    val size = game.difficulty.gridSize

    LazyVerticalGrid(
        columns = GridCells.Fixed(size),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(game.difficulty.boxCount) { index ->
            Box(
                modifier = Modifier
                    .aspectRatio(1f)
                    .clickable {
                        coroutineScope.launch {
                            val message = when (game.tap(index)) {
                                Result.GameOver -> "Out of lives! Game over."
                                Result.Cleared -> "Every box used. Well done!"
                                null -> return@launch
                            }
                            snackbarHostState.showSnackbar(
                                message = message, duration = SnackbarDuration.Short
                            )
                        }
                    }
                    .padding(5.dp)
                    .border(
                        width = 3.dp,
                        color = when {
                            game.wrong == index -> MaterialTheme.colors.error
                            game.isLit(index) -> activeColor
                            else -> inactiveColor
                        },
                        shape = RectangleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = (index + 1).toString(),
                    fontSize = 24.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
