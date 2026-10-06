package com.theseuntaylor.stackboxes

import android.content.Context

const val LEADERBOARD_SIZE = 5

data class ScoreEntry(val score: Int, val achievedAt: Long)

/**
 * The top [LEADERBOARD_SIZE] scores per difficulty, highest first, kept on the device.
 * Ties keep the earlier score ahead.
 */
class ScoreStore(context: Context) {
    private val prefs = context.getSharedPreferences("scores", Context.MODE_PRIVATE)

    fun top(difficulty: Difficulty): List<ScoreEntry> =
        prefs.getString(key(difficulty), null).orEmpty().split(';').mapNotNull { entry ->
            val (score, achievedAt) = entry.split('@').takeIf { it.size == 2 } ?: return@mapNotNull null
            ScoreEntry(score.toIntOrNull() ?: return@mapNotNull null, achievedAt.toLongOrNull() ?: 0L)
        }

    /** Records a finished game's score; returns true if it beats the previous best. */
    fun record(difficulty: Difficulty, score: Int, achievedAt: Long = System.currentTimeMillis()): Boolean {
        if (score <= 0) return false
        val current = top(difficulty)
        val updated = (current + ScoreEntry(score, achievedAt))
            .sortedByDescending { it.score }
            .take(LEADERBOARD_SIZE)
        prefs.edit()
            .putString(key(difficulty), updated.joinToString(";") { "${it.score}@${it.achievedAt}" })
            .apply()
        return score > (current.firstOrNull()?.score ?: 0)
    }

    private fun key(difficulty: Difficulty) = "top_${difficulty.name}"
}
