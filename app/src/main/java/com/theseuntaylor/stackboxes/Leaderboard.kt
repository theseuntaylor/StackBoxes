package com.theseuntaylor.stackboxes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material.AlertDialog
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.DateFormat
import java.util.Date

/** The saved top scores for one difficulty, highest first. */
@Composable
fun LeaderboardDialog(difficulty: Difficulty, scores: List<ScoreEntry>, onDismiss: () -> Unit) {
    val dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM)
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("${difficulty.label} leaderboard") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (scores.isEmpty()) Text("No scores yet.", fontSize = 16.sp)
                scores.forEachIndexed { i, entry ->
                    Text(
                        "${i + 1}. ${entry.score} — ${dateFormat.format(Date(entry.achievedAt))}",
                        fontSize = 16.sp
                    )
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } }
    )
}
