package com.theseuntaylor.stackboxes

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class GameFeedbackTest {

    private sealed interface Event {
        data class Shown(val index: Int, val litAtCall: Boolean) : Event
        data class Correct(val index: Int) : Event
        data class Wrong(val index: Int) : Event
    }

    private class Recorder : GameFeedback {
        lateinit var game: GameState
        val events = mutableListOf<Event>()
        override fun boxShown(index: Int) {
            events += Event.Shown(index, game.flashing == index)
        }
        override fun correctTap(index: Int) {
            events += Event.Correct(index)
        }
        override fun wrongTap(index: Int) {
            events += Event.Wrong(index)
        }
    }

    private fun TestScope.started(difficulty: Difficulty, reverse: Boolean = false): Pair<GameState, Recorder> {
        val recorder = Recorder()
        val game = GameState(difficulty, reverse, recorder).also { recorder.game = it }
        launch { game.start() }
        advanceTimeBy(60_000)
        return game to recorder
    }

    private fun shown(sequence: List<Int>) = sequence.map { Event.Shown(it, litAtCall = true) }

    @Test
    fun `each box sounds once, in order, as it lights during playback`() = runTest {
        val (game, recorder) = started(Difficulty.Medium)
        assertEquals(shown(game.sequence), recorder.events)
    }

    @Test
    fun `correct taps sound their box`() = runTest {
        val (game, recorder) = started(Difficulty.Easy)
        recorder.events.clear()
        game.sequence.take(3).forEach { game.tap(it) }
        assertEquals(game.sequence.take(3).map { Event.Correct(it) }, recorder.events)
    }

    @Test
    fun `completing a round sounds each tap, then plays the next sequence`() = runTest {
        val (game, recorder) = started(Difficulty.Easy)
        val first = game.sequence
        recorder.events.clear()
        first.forEach { game.tap(it) }
        assertEquals(first.map { Event.Correct(it) } + shown(game.sequence), recorder.events)
    }

    @Test
    fun `reverse mode sounds correct backward taps`() = runTest {
        val (game, recorder) = started(Difficulty.Easy, reverse = true)
        recorder.events.clear()
        val backwards = game.sequence.reversed().take(2)
        backwards.forEach { game.tap(it) }
        assertEquals(backwards.map { Event.Correct(it) }, recorder.events)
    }

    @Test
    fun `a wrong tap buzzes once and the replay sounds the same sequence again`() = runTest {
        val (game, recorder) = started(Difficulty.Easy)
        val sequence = game.sequence
        game.tap(sequence[0])
        recorder.events.clear()

        game.tap(sequence.last())
        assertEquals(listOf(Event.Wrong(sequence.last())) + shown(sequence), recorder.events)
    }

    @Test
    fun `losing the last life buzzes`() = runTest {
        val (game, recorder) = started(Difficulty.Easy)
        repeat(2) { game.tap(game.sequence.last()) }
        recorder.events.clear()

        assertEquals(Result.GameOver, game.tap(game.sequence.last()))
        assertEquals(listOf(Event.Wrong(game.sequence.last())), recorder.events)
    }

    @Test
    fun `ignored taps make no sound or vibration`() = runTest {
        val recorder = Recorder()
        val game = GameState(Difficulty.Easy, feedback = recorder).also { recorder.game = it }

        game.tap(0)                                       // before Start
        assertTrue(recorder.events.isEmpty())

        launch { game.start() }
        runCurrent()
        advanceTimeBy(game.litMs / 2 + 1)
        recorder.events.clear()
        game.tap(game.sequence[0])                        // during playback
        game.tap(game.sequence[1])
        assertTrue(recorder.events.isEmpty())

        advanceTimeBy(60_000)
        game.tap(game.sequence[0])
        recorder.events.clear()
        game.tap(game.sequence[0])                        // already on the stack
        assertTrue(recorder.events.isEmpty())

        game.sequence.drop(1).dropLast(1).forEach { game.tap(it) }
        launch { game.tap(game.sequence.last()) }
        runCurrent()                                      // now unwinding
        recorder.events.clear()
        game.tap((0 until 9).first { it !in game.sequence })
        assertTrue(recorder.events.isEmpty())
    }
}
