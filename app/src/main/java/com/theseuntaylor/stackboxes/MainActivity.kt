package com.theseuntaylor.stackboxes

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Scaffold
import androidx.compose.material.IconToggleButton
import androidx.compose.material.Switch
import androidx.compose.material.SnackbarDuration
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.theseuntaylor.stackboxes.ui.theme.StackBoxesTheme
import com.theseuntaylor.stackboxes.ui.theme.activeColor
import com.theseuntaylor.stackboxes.ui.theme.inactiveColor

class MainActivity : ComponentActivity() {
    private val viewModel: GameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            StackBoxesTheme {
                val snackbarHostState = remember { SnackbarHostState() }
                LaunchedEffect(viewModel) {
                    viewModel.messages.collect {
                        snackbarHostState.showSnackbar(message = it, duration = SnackbarDuration.Short)
                    }
                }
                Scaffold(
                    snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                ) { innerPadding ->
                    GameScreen(viewModel, Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun GameScreen(viewModel: GameViewModel, modifier: Modifier = Modifier) {
    val game = viewModel.game
    val landscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE

    Box(
        modifier = modifier
            .fillMaxSize()
            .systemBarsPadding()
            .padding(16.dp)
    ) {
        FeedbackToggles(settings = viewModel.settings, modifier = Modifier.align(Alignment.TopStart))
        Lives(lives = game.lives, modifier = Modifier.align(Alignment.TopEnd))

        val controls = @Composable {
            DifficultyPicker(
                selected = viewModel.difficulty,
                enabled = game.phase == Phase.Idle,
                onSelect = viewModel::selectDifficulty
            )
            ReverseToggle(
                reverse = viewModel.reverse,
                enabled = game.phase == Phase.Idle,
                onChange = viewModel::selectReverse
            )
            Spacer(Modifier.height(8.dp))
            Controls(game = game, scores = viewModel.scores, onStart = viewModel::start)
        }

        if (landscape) {
            // Controls on the left, a square grid as tall as the screen on the right.
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 40.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) { controls() }
                Boxes(
                    game = game,
                    onTap = viewModel::tap,
                    modifier = Modifier
                        .fillMaxHeight()
                        .aspectRatio(1f)
                )
            }
        } else {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                controls()
                Spacer(Modifier.height(16.dp))
                Boxes(game = game, onTap = viewModel::tap, modifier = Modifier.fillMaxWidth())
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
fun FeedbackToggles(settings: Settings, modifier: Modifier = Modifier) {
    Row(modifier = modifier) {
        FeedbackToggle(
            on = settings.sound, onIcon = "🔊", offIcon = "🔇", name = "Sound",
            onChange = settings::updateSound
        )
        FeedbackToggle(
            on = settings.vibration, onIcon = "📳", offIcon = "📴", name = "Vibration",
            onChange = settings::updateVibration
        )
    }
}

@Composable
private fun FeedbackToggle(
    on: Boolean,
    onIcon: String,
    offIcon: String,
    name: String,
    onChange: (Boolean) -> Unit,
) {
    IconToggleButton(
        checked = on,
        onCheckedChange = onChange,
        modifier = Modifier.semantics { contentDescription = "$name ${if (on) "on" else "off"}" }
    ) {
        Text(if (on) onIcon else offIcon, fontSize = 22.sp)
    }
}

@Composable
fun ReverseToggle(reverse: Boolean, enabled: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .toggleable(value = reverse, enabled = enabled, role = Role.Switch, onValueChange = onChange)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("Reverse", fontSize = 16.sp)
        Switch(checked = reverse, onCheckedChange = null, enabled = enabled)
    }
}

@Composable
fun Controls(game: GameState, scores: List<ScoreEntry>, onStart: () -> Unit) {
    var showLeaderboard by rememberSaveable { mutableStateOf(false) }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = when (game.phase) {
                Phase.Idle -> when (game.result) {
                    null -> "Repeat the sequence${if (game.reverse) " backwards" else ""}, " +
                        "starting with ${game.difficulty.startLength} boxes"
                    Result.GameOver -> "Game over!"
                    Result.Cleared ->
                        "You cleared ${game.difficulty.label}${if (game.reverse) " in reverse" else ""}!"
                }
                Phase.Showing ->
                    "${if (game.retrying) "Try again! " else ""}Round ${game.round}: watch the sequence..."
                Phase.Input -> "Your turn${if (game.reverse) ", backwards" else ""}: " +
                    "${game.stack.size}/${game.sequence.size}"
                Phase.Unwinding -> "Unstacking..."
            },
            fontSize = 18.sp,
            textAlign = TextAlign.Center
        )
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(text = "Score: ${game.score}", fontSize = 16.sp)
            Text(text = "Best: ${scores.firstOrNull()?.score ?: 0}", fontSize = 16.sp)
        }
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = onStart,
                enabled = game.phase == Phase.Idle
            ) {
                Text("Start")
            }
            OutlinedButton(
                onClick = { showLeaderboard = true },
                enabled = game.phase == Phase.Idle
            ) {
                Text("Leaderboard")
            }
        }
    }

    if (showLeaderboard) {
        LeaderboardDialog(game.difficulty, game.reverse, scores, onDismiss = { showLeaderboard = false })
    }
}

@Composable
fun Boxes(game: GameState, onTap: (Int) -> Unit, modifier: Modifier = Modifier) {
    val size = game.difficulty.gridSize

    LazyVerticalGrid(
        columns = GridCells.Fixed(size),
        modifier = modifier,
        userScrollEnabled = false
    ) {
        items(game.difficulty.boxCount) { index ->
            Box(
                modifier = Modifier
                    .aspectRatio(1f)
                    .clickable { onTap(index) }
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
