package com.theseuntaylor.stackboxes

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

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

    private val endMessages = Channel<String>(Channel.BUFFERED)

    /** One message per finished game, for the snackbar. */
    val messages: Flow<String> = endMessages.receiveAsFlow()

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
        viewModelScope.launch { game.start() }
    }

    fun tap(index: Int) {
        val game = game
        viewModelScope.launch {
            val result = game.tap(index) ?: return@launch
            val newBest = scoreStore.record(game.difficulty, game.reverse, game.score)
            scores = scoreStore.top(game.difficulty, game.reverse)
            endMessages.send(
                when (result) {
                    Result.GameOver -> "Out of lives! Game over."
                    Result.Cleared -> "Every box used. Well done!"
                } + if (newBest) " New best!" else ""
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
