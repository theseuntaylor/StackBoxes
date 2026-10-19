package com.theseuntaylor.stackboxes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import java.text.DateFormat
import java.util.Date

/**
 * The saved top scores for one difficulty, highest first, as a bottom sheet. The rank tiles fade
 * down the list like the trail on the grid. Back, swiping it down or Close dismisses it.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaderboardDialog(
    difficulty: Difficulty,
    reverse: Boolean,
    scores: List<ScoreEntry>,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    val dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM)

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier = Modifier.padding(start = 24.dp, end = 24.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "${difficulty.label}${if (reverse) " reverse" else ""} leaderboard",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.semantics { heading() }
            )
            if (scores.isEmpty()) {
                Text(
                    "No scores yet.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                scores.forEachIndexed { i, entry ->
                    val date = dateFormat.format(Date(entry.achievedAt))
                    Row(
                        // Read as one line: "1. 9 — 17 Oct 2026".
                        modifier = Modifier
                            .fillMaxWidth()
                            .clearAndSetSemantics { contentDescription = "${i + 1}. ${entry.score} — $date" },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        RankTile(rank = i + 1)
                        Text(
                            date,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            entry.score.toString(),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            OutlinedButton(
                onClick = { scope.launch { sheetState.hide() }.invokeOnCompletion { onDismiss() } },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Close")
            }
        }
    }
}

/** A rank in a grid tile: first lit brightest, then fading down the list. */
@Composable
private fun RankTile(rank: Int) {
    val alpha = listOf(1f, 0.7f, 0.46f, 0.3f, 0.3f)[(rank - 1).coerceIn(0, 4)]
    val tile = MaterialTheme.colorScheme.primary.copy(alpha = alpha)
        .compositeOver(MaterialTheme.colorScheme.surfaceContainerLow)
    Box(
        modifier = Modifier
            .size(40.dp)
            .background(tile, RoundedCornerShape(10.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            rank.toString(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            // Black or white, whichever has more contrast on this tile (they tie at 0.179).
            color = if (tile.luminance() > 0.179f) Color.Black else Color.White
        )
    }
}
