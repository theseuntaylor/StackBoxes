package com.theseuntaylor.stackboxes

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay

private const val TAG = "StackBoxes"

private const val SHOW_GAP_MS = 300L
private const val SHOW_LIT_MS = 600L
private const val WRONG_FLASH_MS = 600L
private const val POP_DELAY_MS = 300L
private const val NEXT_ROUND_DELAY_MS = 500L

enum class Difficulty(val label: String, val gridSize: Int, val startLength: Int) {
    Easy("Easy", 3, 4),
    Medium("Medium", 4, 6),
    Hard("Hard", 5, 8);

    val boxCount: Int get() = gridSize * gridSize
}

enum class Phase { Idle, Showing, Input, Unwinding }

/** How the last game ended: a wrong tap, or every box on the grid used in one sequence. */
enum class Result { GameOver, Cleared }

/**
 * A game is a run of rounds. [start] shows a random sequence of [Difficulty.startLength] boxes;
 * the player repeats it with [tap] (each correct tap pushes the box onto [stack]), then the stack
 * unwinds last-in-first-out and the same sequence is shown again with one new box added.
 * A wrong tap ends the game; so does completing a sequence that uses every box.
 * Taps are ignored unless the game is waiting for input.
 */
class GameState(val difficulty: Difficulty) {
    var phase by mutableStateOf(Phase.Idle)
        private set
    var flashing by mutableStateOf<Int?>(null)
        private set
    var wrong by mutableStateOf<Int?>(null)
        private set

    // Length of the longest sequence completed in the current (or last) game.
    var score by mutableIntStateOf(0)
        private set
    var result by mutableStateOf<Result?>(null)
        private set

    // Indices of the boxes the player has tapped correctly so far; top of the stack is last.
    val stack = mutableStateListOf<Int>()

    var sequence: List<Int> by mutableStateOf(emptyList())
        private set

    val round: Int get() = sequence.size - difficulty.startLength + 1

    fun isLit(index: Int) = index in stack || index == flashing

    suspend fun start() {
        if (phase != Phase.Idle) return
        score = 0
        result = null
        show((0 until difficulty.boxCount).shuffled().take(difficulty.startLength))
    }

    /** Returns how the game ended once it ends, or null while it is still going. */
    suspend fun tap(index: Int): Result? {
        if (phase != Phase.Input || index in stack) return null

        if (index != sequence[stack.size]) {
            phase = Phase.Unwinding
            wrong = index
            delay(WRONG_FLASH_MS)
            wrong = null
            return end(Result.GameOver)
        }

        stack.add(index)
        if (stack.size < sequence.size) return null

        score = sequence.size
        phase = Phase.Unwinding
        // Boxes never repeat within a sequence, so a full-grid sequence is the last one.
        val unused = (0 until difficulty.boxCount) - sequence.toSet()
        if (unused.isEmpty()) return end(Result.Cleared)

        unwind()
        delay(NEXT_ROUND_DELAY_MS)
        show(sequence + unused.random())
        return null
    }

    private suspend fun show(next: List<Int>) {
        phase = Phase.Showing
        sequence = next
        Log.d(TAG, "sequence=$sequence")

        for (index in sequence) {
            delay(SHOW_GAP_MS)
            flashing = index
            delay(SHOW_LIT_MS)
            flashing = null
        }
        phase = Phase.Input
    }

    private suspend fun end(outcome: Result): Result {
        unwind()
        result = outcome
        phase = Phase.Idle
        return outcome
    }

    private suspend fun unwind() {
        while (stack.isNotEmpty()) {
            delay(POP_DELAY_MS)
            stack.removeAt(stack.lastIndex)
        }
    }
}
