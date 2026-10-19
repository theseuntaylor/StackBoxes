package com.theseuntaylor.stackboxes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Shown when a game ends: how it ended, the final score and the best. Back or Close dismisses it. */
@Composable
fun EndOfGameDialog(summary: GameSummary, onPlayAgain: () -> Unit, onClose: () -> Unit) {
    AlertDialog(
        onDismissRequest = onClose,
        title = {
            Text(
                when (summary.result) {
                    Result.GameOver -> "Game over"
                    Result.Cleared ->
                        "You cleared ${summary.difficulty.label}${if (summary.reverse) " in reverse" else ""}!"
                }
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Final score: ${summary.score}", fontSize = 18.sp)
                Text("Your best: ${summary.best}", fontSize = 16.sp)
                if (summary.newBest) {
                    Text(
                        "New best!",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        },
        confirmButton = { Button(onClick = onPlayAgain) { Text("Play again") } },
        dismissButton = { TextButton(onClick = onClose) { Text("Close") } }
    )
}
