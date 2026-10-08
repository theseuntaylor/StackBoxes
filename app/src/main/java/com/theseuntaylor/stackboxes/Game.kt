package com.theseuntaylor.stackboxes

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay

private const val TAG = "StackBoxes"

private const val MIN_LIT_MS = 250L
private const val WRONG_FLASH_MS = 600L
private const val POP_DELAY_MS = 300L
private const val NEXT_ROUND_DELAY_MS = 500L

const val MAX_LIVES = 3
private const val ROUNDS_PER_LIFE = 3

/**
 * [baseLitMs] is how long each box stays lit in round 1; every round after that is
 * [speedUpMs] faster. Easy plays fastest; Hard plays slowest so its long sequences stay playable.
 */
enum class Difficulty(
    val label: String,
    val gridSize: Int,
    val startLength: Int,
    val baseLitMs: Long,
    val speedUpMs: Long,
) {
    Easy("Easy", 3, 4, baseLitMs = 450, speedUpMs = 30),
    Medium("Medium", 4, 6, baseLitMs = 550, speedUpMs = 20),
    Hard("Hard", 5, 8, baseLitMs = 650, speedUpMs = 10);

    val boxCount: Int get() = gridSize * gridSize
}

enum class Phase { Idle, Showing, Input, Unwinding }

/** How the last game ended: out of lives, or every box on the grid used in one sequence. */
enum class Result { GameOver, Cleared }

/**
 * A game is a run of rounds. [start] shows a random sequence of [Difficulty.startLength] boxes;
 * the player repeats it with [tap] (each correct tap pushes the box onto [stack]), then the stack
 * unwinds last-in-first-out and the same sequence is shown again with one new box added, a little
 * faster. In [reverse] mode the player repeats each sequence backwards, last box first.
 * A wrong tap costs a life and replays the same round; every [ROUNDS_PER_LIFE] completed
 * rounds win a life back, up to [MAX_LIVES]. The game ends when the lives run out, or when a
 * sequence that uses every box is completed.
 * Taps are ignored unless the game is waiting for input.
 */
class GameState(val difficulty: Difficulty, val reverse: Boolean = false) {
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
    var lives by mutableIntStateOf(MAX_LIVES)
        private set

    // True while replaying a round the player got wrong.
    var retrying by mutableStateOf(false)
        private set

    // Indices of the boxes the player has tapped correctly so far; top of the stack is last.
    val stack = mutableStateListOf<Int>()

    var sequence: List<Int> by mutableStateOf(emptyList())
        private set

    val round: Int get() = sequence.size - difficulty.startLength + 1

    /** How long each box stays lit this round; the gap between boxes is half of it. */
    val litMs: Long
        get() = (difficulty.baseLitMs - difficulty.speedUpMs * (round - 1)).coerceAtLeast(MIN_LIT_MS)

    fun isLit(index: Int) = index in stack || index == flashing

    suspend fun start() {
        if (phase != Phase.Idle) return
        score = 0
        result = null
        lives = MAX_LIVES
        retrying = false
        show((0 until difficulty.boxCount).shuffled().take(difficulty.startLength))
    }

    /** Returns how the game ended once it ends, or null while it is still going. */
    suspend fun tap(index: Int): Result? {
        if (phase != Phase.Input || index in stack) return null

        val expected = if (reverse) sequence[sequence.lastIndex - stack.size] else sequence[stack.size]
        if (index != expected) {
            phase = Phase.Unwinding
            wrong = index
            delay(WRONG_FLASH_MS)
            wrong = null
            lives--
            if (lives == 0) return end(Result.GameOver)

            unwind()
            retrying = true
            delay(NEXT_ROUND_DELAY_MS)
            show(sequence)
            return null
        }

        stack.add(index)
        if (stack.size < sequence.size) return null

        score = sequence.size
        retrying = false
        if (round % ROUNDS_PER_LIFE == 0 && lives < MAX_LIVES) lives++
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
        val lit = litMs
        Log.d(TAG, "sequence=$sequence litMs=$lit")

        for (index in sequence) {
            delay(lit / 2)
            flashing = index
            delay(lit)
            flashing = null
        }
        Log.d(TAG, "input")
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
