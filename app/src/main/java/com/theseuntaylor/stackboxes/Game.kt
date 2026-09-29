package com.theseuntaylor.stackboxes

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay

private const val TAG = "StackBoxes"

private const val SHOW_GAP_MS = 300L
private const val SHOW_LIT_MS = 600L
private const val WRONG_FLASH_MS = 600L
private const val POP_DELAY_MS = 300L

enum class Difficulty(val label: String, val gridSize: Int, val sequenceLength: Int) {
    Easy("Easy", 3, 4),
    Medium("Medium", 4, 6),
    Hard("Hard", 5, 8);

    val boxCount: Int get() = gridSize * gridSize
}

enum class Phase { Idle, Showing, Input, Unwinding }

enum class Outcome { Success, WrongOrder }

/**
 * One round is: [start] shows a random sequence of boxes, the player repeats it with [tap]
 * (each correct tap pushes the box onto [stack]), then the stack unwinds last-in-first-out.
 * Taps are ignored unless the game is waiting for input.
 */
class GameState(val difficulty: Difficulty) {
    var phase by mutableStateOf(Phase.Idle)
        private set
    var flashing by mutableStateOf<Int?>(null)
        private set
    var wrong by mutableStateOf<Int?>(null)
        private set

    // Indices of the boxes the player has tapped correctly so far; top of the stack is last.
    val stack = mutableStateListOf<Int>()

    private var sequence: List<Int> = emptyList()

    fun isLit(index: Int) = index in stack || index == flashing

    suspend fun start() {
        if (phase != Phase.Idle) return
        phase = Phase.Showing
        sequence = (0 until difficulty.boxCount).shuffled().take(difficulty.sequenceLength)
        Log.d(TAG, "sequence=$sequence")

        for (index in sequence) {
            delay(SHOW_GAP_MS)
            flashing = index
            delay(SHOW_LIT_MS)
            flashing = null
        }
        phase = Phase.Input
    }

    /** Returns the round's outcome once it ends, or null while it is still going. */
    suspend fun tap(index: Int): Outcome? {
        if (phase != Phase.Input || index in stack) return null

        if (index != sequence[stack.size]) {
            phase = Phase.Unwinding
            wrong = index
            delay(WRONG_FLASH_MS)
            wrong = null
            unwind()
            return Outcome.WrongOrder
        }

        stack.add(index)
        if (stack.size < sequence.size) return null

        phase = Phase.Unwinding
        unwind()
        return Outcome.Success
    }

    private suspend fun unwind() {
        while (stack.isNotEmpty()) {
            delay(POP_DELAY_MS)
            stack.removeAt(stack.lastIndex)
        }
        phase = Phase.Idle
    }
}
