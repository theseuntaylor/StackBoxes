package com.theseuntaylor.stackboxes

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.consumeWindowInsets
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.theseuntaylor.stackboxes.ui.theme.StackBoxesTheme

class MainActivity : ComponentActivity() {
    private val viewModel: GameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        useSplashScreen(reduceMotion = viewModel.settings.reduceMotion)
        enableEdgeToEdge()
        setContent {
            StackBoxesTheme {
                Scaffold { innerPadding ->
                    GameScreen(viewModel, Modifier.padding(innerPadding).consumeWindowInsets(innerPadding))
                }
                viewModel.summary?.let { summary ->
                    EndOfGameDialog(
                        summary = summary,
                        onPlayAgain = viewModel::playAgain,
                        onClose = viewModel::closeSummary
                    )
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        // Leaving the app, not just rotating: now it is safe to switch the launcher icon.
        if (!isChangingConfigurations) viewModel.appIcon.apply()
    }
}

@Composable
fun GameScreen(viewModel: GameViewModel, modifier: Modifier = Modifier) {
    var settingsOpen by rememberSaveable { mutableStateOf(false) }
    if (settingsOpen) {
        SettingsScreen(viewModel.settings, viewModel.appIcon, onClose = { settingsOpen = false }, modifier = modifier)
    } else {
        GameBoard(viewModel, onOpenSettings = { settingsOpen = true }, modifier = modifier)
    }
}

/** The game itself: the settings gear, lives, controls and the grid. */
@Composable
private fun GameBoard(viewModel: GameViewModel, onOpenSettings: () -> Unit, modifier: Modifier = Modifier) {
    val game = viewModel.game
    val landscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE

    Box(
        modifier = modifier
            .fillMaxSize()
            .systemBarsPadding()
            .padding(16.dp)
    ) {
        SettingsButton(onClick = onOpenSettings, modifier = Modifier.align(Alignment.TopStart))
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
                    showNumbers = viewModel.settings.showNumbers,
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
                Boxes(
                    game = game,
                    onTap = viewModel::tap,
                    showNumbers = viewModel.settings.showNumbers,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun DifficultyPicker(selected: Difficulty, enabled: Boolean, onSelect: (Difficulty) -> Unit) {
    val difficulties = Difficulty.entries
    SingleChoiceSegmentedButtonRow {
        difficulties.forEachIndexed { i, difficulty ->
            SegmentedButton(
                selected = difficulty == selected,
                onClick = { onSelect(difficulty) },
                enabled = enabled,
                shape = SegmentedButtonDefaults.itemShape(index = i, count = difficulties.size)
            ) {
                Text(difficulty.label)
            }
        }
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
        Text("Reverse", style = MaterialTheme.typography.bodyLarge)
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
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite }
        )
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(text = "Score: ${game.score}", style = MaterialTheme.typography.bodyLarge)
            Text(text = "Best: ${scores.firstOrNull()?.score ?: 0}", style = MaterialTheme.typography.bodyLarge)
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
fun Boxes(game: GameState, onTap: (Int) -> Unit, showNumbers: Boolean, modifier: Modifier = Modifier) {
    val size = game.difficulty.gridSize

    LazyVerticalGrid(
        columns = GridCells.Fixed(size),
        modifier = modifier,
        userScrollEnabled = false
    ) {
        items(game.difficulty.boxCount) { index ->
            val wrong = game.wrong == index
            val lit = game.isLit(index)
            val color = if (wrong) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
            // State is carried by fill, border weight, bold text and the ✕ mark, not just color.
            Box(
                modifier = Modifier
                    .aspectRatio(1f)
                    .padding(5.dp)
                    .clip(BoxShape)
                    .clickable(role = Role.Button, onClickLabel = "Choose") { onTap(index) }
                    .semantics { contentDescription = boxDescription(index, game) }
                    .background(
                        if (wrong || lit) color.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surfaceContainerHigh
                    )
                    .border(
                        width = if (wrong || lit) 6.dp else 0.dp,
                        color = if (wrong || lit) color else Color.Transparent,
                        shape = BoxShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (showNumbers) (index + 1).toString() else "",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = if (wrong || lit) FontWeight.Bold else FontWeight.Normal,
                    textAlign = TextAlign.Center,
                    // The box's description already says the number; don't read it twice.
                    modifier = Modifier.clearAndSetSemantics {}
                )
                if (wrong) {
                    Text(
                        text = "✕",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(6.dp)
                            .clearAndSetSemantics {}
                    )
                }
            }
        }
    }
}

private val BoxShape = RoundedCornerShape(12.dp)

/** What TalkBack says for a box: its number, then its state if it has one. */
private fun boxDescription(index: Int, game: GameState): String = "Box ${index + 1}" + when {
    game.wrong == index -> ", wrong"
    index in game.stack -> ", stacked"
    game.flashing == index -> ", lit"
    else -> ""
}
