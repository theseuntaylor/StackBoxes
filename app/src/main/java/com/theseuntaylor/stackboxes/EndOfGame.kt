package com.theseuntaylor.stackboxes

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

/**
 * Shown when a game ends, as a bottom sheet: how it ended, the final score and the best, beside
 * the last boxes of the sequence. Back, swiping it down or Close dismisses it.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EndOfGameDialog(summary: GameSummary, onPlayAgain: () -> Unit, onClose: () -> Unit) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    // Slide the sheet away, then act.
    fun hideThen(action: () -> Unit) {
        scope.launch { sheetState.hide() }.invokeOnCompletion { action() }
    }

    ModalBottomSheet(onDismissRequest = onClose, sheetState = sheetState) {
        Column(
            modifier = Modifier.padding(start = 24.dp, end = 24.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(20.dp), verticalAlignment = Alignment.CenterVertically) {
                Trail(summary.difficulty.gridSize, summary.trail)
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        when (summary.result) {
                            Result.GameOver -> "Game over"
                            Result.Cleared ->
                                "You cleared ${summary.difficulty.label}${if (summary.reverse) " in reverse" else ""}!"
                        },
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.semantics { heading() }
                    )
                    Text(
                        summary.difficulty.label + if (summary.reverse) " · reverse" else "",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    summary.score.toString(),
                    style = MaterialTheme.typography.displayLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clearAndSetSemantics { contentDescription = "Final score: ${summary.score}" }
                )
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        "final score",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.clearAndSetSemantics {}
                    )
                    Text(
                        "Your best: ${summary.best}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (summary.newBest) {
                        Text(
                            "New best!",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .border(1.5.dp, MaterialTheme.colorScheme.primary, CircleShape)
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(onClick = { hideThen(onPlayAgain) }, modifier = Modifier.fillMaxWidth()) {
                    Text("Play again")
                }
                OutlinedButton(onClick = { hideThen(onClose) }, modifier = Modifier.fillMaxWidth()) {
                    Text("Close")
                }
            }
        }
    }
}

/** The difficulty's grid in miniature, its last boxes lit as a fading trail, newest brightest. */
@Composable
private fun Trail(gridSize: Int, trail: List<Int>, width: Dp = 92.dp) {
    val gap = 4.dp
    val tile = (width - gap * (gridSize - 1)) / gridSize
    Column(verticalArrangement = Arrangement.spacedBy(gap), modifier = Modifier.clearAndSetSemantics {}) {
        repeat(gridSize) { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(gap)) {
                repeat(gridSize) { col ->
                    val age = trail.size - 1 - trail.indexOf(row * gridSize + col)
                    val lit = age < trail.size
                    Box(
                        Modifier
                            .size(tile)
                            .background(
                                if (lit) MaterialTheme.colorScheme.primary.copy(alpha = 1f - age * 0.22f)
                                else MaterialTheme.colorScheme.surfaceContainerHighest,
                                RoundedCornerShape(tile / 4)
                            )
                    )
                }
            }
        }
    }
}
