package com.theseuntaylor.stackboxes

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

/** What the end-of-game screen shows. */
data class GameSummary(
    val result: Result,
    val difficulty: Difficulty,
    val reverse: Boolean,
    val score: Int,
    val best: Int,
    val newBest: Boolean,
)

/**
 * Owns the game and everything it plays through, so a rotation (or any other configuration
 * change) only rebuilds the screen: playback, input and unwinding carry on in [viewModelScope].
 */
class GameViewModel(application: Application) : AndroidViewModel(application) {
    val settings = Settings(application)
    private val scoreStore = ScoreStore(application)
    private val sounds = SoundPlayer()
    private val haptics = Haptics(application)

    var difficulty by mutableStateOf(Difficulty.Easy)
        private set
    var reverse by mutableStateOf(false)
        private set
    var game by mutableStateOf(newGame())
        private set
    var scores by mutableStateOf(scoreStore.top(difficulty, reverse))
        private set

    /** Set when a game ends, until the player plays again or closes the end screen. */
    var summary by mutableStateOf<GameSummary?>(null)
        private set

    fun selectDifficulty(value: Difficulty) {
        if (game.phase != Phase.Idle) return
        difficulty = value
        reset()
    }

    fun selectReverse(value: Boolean) {
        if (game.phase != Phase.Idle) return
        reverse = value
        reset()
    }

    fun start() {
        summary = null
        viewModelScope.launch { game.start() }
    }

    fun playAgain() = start()

    fun closeSummary() {
        summary = null
    }

    fun tap(index: Int) {
        val game = game
        viewModelScope.launch {
            val result = game.tap(index) ?: return@launch
            val newBest = scoreStore.record(game.difficulty, game.reverse, game.score)
            scores = scoreStore.top(game.difficulty, game.reverse)
            summary = GameSummary(
                result = result,
                difficulty = game.difficulty,
                reverse = game.reverse,
                score = game.score,
                best = scores.firstOrNull()?.score ?: 0,
                newBest = newBest,
            )
        }
    }

    override fun onCleared() {
        sounds.close()
    }

    private fun reset() {
        game = newGame()
        scores = scoreStore.top(difficulty, reverse)
    }

    private fun newGame(): GameState {
        val boxCount = difficulty.boxCount
        return GameState(difficulty, reverse, object : GameFeedback {
            override fun boxShown(index: Int) {
                if (settings.sound) sounds.box(index, boxCount)
            }

            override fun correctTap(index: Int) {
                if (settings.sound) sounds.box(index, boxCount)
                if (settings.vibration) haptics.tick()
            }

            override fun wrongTap(index: Int) {
                if (settings.sound) sounds.buzz()
                if (settings.vibration) haptics.buzz()
            }
        })
    }
}
