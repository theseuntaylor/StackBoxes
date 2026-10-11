package com.theseuntaylor.stackboxes

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class GameStateTest {

    /** Taps the current sequence back in the order the mode expects; returns the last tap's result. */
    private suspend fun GameState.playRound(): Result? {
        var result: Result? = null
        for (index in if (reverse) sequence.reversed() else sequence) result = tap(index)
        return result
    }

    /** A box that is wrong as the first tap: the last box normally, the first in reverse. */
    private fun GameState.wrongFirstTap() = if (reverse) sequence.first() else sequence.last()

    private fun TestScope.started(difficulty: Difficulty, reverse: Boolean = false): GameState {
        val game = GameState(difficulty, reverse)
        launch { game.start() }
        advanceTimeBy(60_000)
        return game
    }

    // Starting a game

    @Test
    fun `round 1 is startLength distinct boxes on the grid, then waits for input`() = runTest {
        for (difficulty in Difficulty.entries) {
            val game = started(difficulty)
            assertEquals(difficulty.startLength, game.sequence.size)
            assertEquals(game.sequence.size, game.sequence.toSet().size)
            assertTrue(game.sequence.all { it in 0 until difficulty.boxCount })
            assertEquals(Phase.Input, game.phase)
            assertEquals(1, game.round)
        }
    }

    @Test
    fun `start is ignored while a game is running`() = runTest {
        val game = started(Difficulty.Easy)
        val sequence = game.sequence
        game.start()
        assertEquals(sequence, game.sequence)
        assertEquals(Phase.Input, game.phase)
    }

    @Test
    fun `a new game resets score, lives, result and retry flag`() = runTest {
        val game = started(Difficulty.Easy)
        game.playRound()                                  // score 4
        repeat(3) { game.tap(game.wrongFirstTap()) }      // game over
        assertEquals(Result.GameOver, game.result)
        assertEquals(4, game.score)
        assertEquals(0, game.lives)

        game.start()
        assertEquals(0, game.score)
        assertEquals(MAX_LIVES, game.lives)
        assertNull(game.result)
        assertFalse(game.retrying)
        assertEquals(Difficulty.Easy.startLength, game.sequence.size)
    }

    // Playback

    @Test
    fun `boxes light one at a time during playback and taps are ignored`() = runTest {
        val game = GameState(Difficulty.Easy)
        launch { game.start() }
        runCurrent()
        assertEquals(Phase.Showing, game.phase)
        assertNull(game.flashing)                          // gap before the first box

        val lit = game.litMs
        advanceTimeBy(lit / 2 + 1)
        val first = game.sequence[0]
        assertEquals(first, game.flashing)
        assertTrue(game.isLit(first))

        assertNull(game.tap(first))
        assertTrue(game.stack.isEmpty())
        assertEquals(MAX_LIVES, game.lives)

        advanceTimeBy(lit)
        assertFalse(game.isLit(first))
    }

    @Test
    fun `taps are ignored before a game starts`() = runTest {
        val game = GameState(Difficulty.Easy)
        assertNull(game.tap(0))
        assertTrue(game.stack.isEmpty())
        assertEquals(Phase.Idle, game.phase)
    }

    // Input

    @Test
    fun `correct taps push onto the stack and light the box`() = runTest {
        val game = started(Difficulty.Medium)
        game.sequence.take(3).forEachIndexed { i, index ->
            assertNull(game.tap(index))
            assertEquals(game.sequence.take(i + 1), game.stack.toList())
            assertTrue(game.isLit(index))
        }
    }

    @Test
    fun `tapping a box already on the stack is ignored and costs nothing`() = runTest {
        val game = started(Difficulty.Easy)
        game.tap(game.sequence[0])
        assertNull(game.tap(game.sequence[0]))
        assertEquals(1, game.stack.size)
        assertEquals(MAX_LIVES, game.lives)
        assertEquals(Phase.Input, game.phase)
    }

    @Test
    fun `taps are ignored while the stack unwinds`() = runTest {
        val game = started(Difficulty.Easy)
        game.sequence.dropLast(1).forEach { game.tap(it) }
        launch { game.tap(game.sequence.last()) }
        runCurrent()
        assertEquals(Phase.Unwinding, game.phase)
        val stack = game.stack.toList()
        assertNull(game.tap((0 until 9).first { it !in game.sequence }))
        assertEquals(stack, game.stack.toList())
        assertEquals(MAX_LIVES, game.lives)
    }

    // Completing rounds

    @Test
    fun `completing a round scores it and adds one new box to the same sequence`() = runTest {
        val game = started(Difficulty.Medium)
        val previous = game.sequence
        assertNull(game.playRound())

        assertEquals(previous.size, game.score)
        assertEquals(previous, game.sequence.dropLast(1))
        assertTrue(game.sequence.last() !in previous)
        assertTrue(game.stack.isEmpty())
        assertEquals(Phase.Input, game.phase)
        assertEquals(2, game.round)
    }

    @Test
    fun `using every box clears the game`() = runTest {
        for (difficulty in Difficulty.entries) {
            val game = started(difficulty)
            var result: Result? = null
            while (result == null) result = game.playRound()

            assertEquals(Result.Cleared, result)
            assertEquals(Result.Cleared, game.result)
            assertEquals(difficulty.boxCount, game.score)
            assertEquals(difficulty.boxCount, game.sequence.toSet().size)
            assertEquals(Phase.Idle, game.phase)
            assertTrue(game.stack.isEmpty())
        }
    }

    // Mistakes and lives

    @Test
    fun `a wrong tap costs a life and replays the same round`() = runTest {
        val game = started(Difficulty.Medium)
        game.playRound()
        val sequence = game.sequence
        game.tap(sequence[0])

        assertNull(game.tap(game.wrongFirstTap()))
        assertEquals(MAX_LIVES - 1, game.lives)
        assertEquals(sequence, game.sequence)
        assertEquals(Difficulty.Medium.startLength, game.score)
        assertTrue(game.retrying)
        assertTrue(game.stack.isEmpty())
        assertEquals(Phase.Input, game.phase)
    }

    @Test
    fun `a box outside the sequence is a wrong tap`() = runTest {
        val game = started(Difficulty.Easy)
        game.tap((0 until 9).first { it !in game.sequence })
        assertEquals(MAX_LIVES - 1, game.lives)
    }

    @Test
    fun `the wrong box flashes, then clears`() = runTest {
        val game = started(Difficulty.Easy)
        val wrong = game.wrongFirstTap()
        launch { game.tap(wrong) }
        runCurrent()
        assertEquals(wrong, game.wrong)
        assertEquals(Phase.Unwinding, game.phase)
        advanceTimeBy(60_000)
        assertNull(game.wrong)
    }

    @Test
    fun `passing a replayed round clears the retry flag`() = runTest {
        val game = started(Difficulty.Easy)
        game.tap(game.wrongFirstTap())
        game.playRound()
        assertFalse(game.retrying)
        assertEquals(2, game.round)
    }

    @Test
    fun `losing the last life ends the game and keeps the score`() = runTest {
        val game = started(Difficulty.Hard)
        game.playRound()
        assertNull(game.tap(game.wrongFirstTap()))
        assertNull(game.tap(game.wrongFirstTap()))
        assertEquals(Result.GameOver, game.tap(game.wrongFirstTap()))

        assertEquals(0, game.lives)
        assertEquals(Result.GameOver, game.result)
        assertEquals(Difficulty.Hard.startLength, game.score)
        assertEquals(Phase.Idle, game.phase)
        assertTrue(game.stack.isEmpty())
    }

    @Test
    fun `lives never go above the max`() = runTest {
        val game = started(Difficulty.Easy)
        repeat(5) {
            game.playRound()
            assertEquals(MAX_LIVES, game.lives)
        }
    }

    @Test
    fun `a life comes back every third completed round, not per attempt`() = runTest {
        val game = started(Difficulty.Hard)
        game.tap(game.wrongFirstTap())
        game.tap(game.wrongFirstTap())                    // round 1 replayed twice
        assertEquals(1, game.lives)

        val livesAfterRound = (1..6).map { game.playRound(); game.lives }
        assertEquals(listOf(1, 1, 2, 2, 2, 3), livesAfterRound)
    }

    // Reverse mode

    @Test
    fun `reverse mode expects the sequence backwards`() = runTest {
        val game = started(Difficulty.Medium, reverse = true)
        val previous = game.sequence
        assertNull(game.playRound())
        assertEquals(previous.size, game.score)
        assertEquals(MAX_LIVES, game.lives)
        assertEquals(previous, game.sequence.dropLast(1))
    }

    @Test
    fun `reverse mode rejects forward order`() = runTest {
        val game = started(Difficulty.Medium, reverse = true)
        game.tap(game.sequence.first())
        assertEquals(MAX_LIVES - 1, game.lives)
        assertTrue(game.stack.isEmpty())
    }

    @Test
    fun `normal mode rejects backward order`() = runTest {
        val game = started(Difficulty.Medium)
        game.tap(game.sequence.last())
        assertEquals(MAX_LIVES - 1, game.lives)
    }

    // Speed

    @Test
    fun `each difficulty starts at its own speed and speeds up each round`() = runTest {
        val expected = mapOf(
            Difficulty.Easy to listOf(450L, 420L, 390L, 360L, 330L, 300L),
            Difficulty.Medium to listOf(550L, 530L, 510L, 490L, 470L, 450L),
            Difficulty.Hard to listOf(650L, 640L, 630L, 620L, 610L, 600L),
        )
        for ((difficulty, speeds) in expected) {
            val game = started(difficulty)
            val got = speeds.indices.map { round ->
                game.litMs.also { if (round < speeds.lastIndex) game.playRound() }
            }
            assertEquals(speeds, got)
        }
    }

    @Test
    fun `easy plays fastest and hard slowest`() {
        assertTrue(Difficulty.Easy.baseLitMs < Difficulty.Medium.baseLitMs)
        assertTrue(Difficulty.Medium.baseLitMs < Difficulty.Hard.baseLitMs)
    }

    @Test
    fun `speed never drops below the floor, even on the last round of a full grid`() = runTest {
        for (difficulty in Difficulty.entries) {
            val game = started(difficulty)
            while (game.sequence.size < difficulty.boxCount) {
                assertTrue(game.litMs >= 250)
                game.playRound()
            }
            assertTrue(game.litMs >= 250)
        }
    }

    @Test
    fun `a replayed round keeps its speed`() = runTest {
        val game = started(Difficulty.Medium)
        game.playRound()
        val lit = game.litMs
        game.tap(game.wrongFirstTap())
        assertEquals(lit, game.litMs)
    }

    @Test
    fun `playback takes one and a half lit times per box`() = runTest {
        val game = GameState(Difficulty.Easy)
        val before = currentTime
        game.start()
        val lit = Difficulty.Easy.baseLitMs
        assertEquals(Difficulty.Easy.startLength * (lit / 2 + lit), currentTime - before)
    }
}
