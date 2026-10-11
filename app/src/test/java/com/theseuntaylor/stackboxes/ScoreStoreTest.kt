package com.theseuntaylor.stackboxes

import android.content.SharedPreferences
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ScoreStoreTest {

    private val prefs = FakeSharedPreferences()
    private val store = ScoreStore(prefs)

    private fun scores(difficulty: Difficulty = Difficulty.Easy, reverse: Boolean = false) =
        store.top(difficulty, reverse).map { it.score }

    @Test
    fun `an empty store has no scores`() {
        for (difficulty in Difficulty.entries) {
            assertEquals(emptyList<Int>(), scores(difficulty, reverse = false))
            assertEquals(emptyList<Int>(), scores(difficulty, reverse = true))
        }
    }

    @Test
    fun `a score of 0 or less is not saved and is not a new best`() {
        assertFalse(store.record(Difficulty.Easy, false, 0))
        assertFalse(store.record(Difficulty.Easy, false, -1))
        assertEquals(emptyList<Int>(), scores())
    }

    @Test
    fun `scores are kept highest first`() {
        listOf(5, 9, 4, 7).forEach { store.record(Difficulty.Easy, false, it) }
        assertEquals(listOf(9, 7, 5, 4), scores())
    }

    @Test
    fun `ties keep the earlier score ahead`() {
        store.record(Difficulty.Easy, false, 6, achievedAt = 1_000)
        store.record(Difficulty.Easy, false, 6, achievedAt = 2_000)
        store.record(Difficulty.Easy, false, 6, achievedAt = 3_000)
        assertEquals(listOf(1_000L, 2_000L, 3_000L), store.top(Difficulty.Easy, false).map { it.achievedAt })
    }

    @Test
    fun `only the top five are kept and the lowest drops off`() {
        listOf(4, 5, 6, 7, 8).forEach { store.record(Difficulty.Easy, false, it) }
        store.record(Difficulty.Easy, false, 9)
        assertEquals(listOf(9, 8, 7, 6, 5), scores())

        store.record(Difficulty.Easy, false, 4)              // lower than everything on a full board
        assertEquals(listOf(9, 8, 7, 6, 5), scores())
        assertEquals(LEADERBOARD_SIZE, scores().size)
    }

    @Test
    fun `new best only when strictly higher than the best`() {
        assertTrue(store.record(Difficulty.Easy, false, 5))  // first score
        assertFalse(store.record(Difficulty.Easy, false, 5)) // ties the best
        assertFalse(store.record(Difficulty.Easy, false, 3))
        assertTrue(store.record(Difficulty.Easy, false, 6))
    }

    @Test
    fun `each difficulty and mode keeps its own scores`() {
        store.record(Difficulty.Easy, false, 9)
        store.record(Difficulty.Easy, true, 5)
        store.record(Difficulty.Medium, false, 8)
        store.record(Difficulty.Hard, true, 10)

        assertEquals(listOf(9), scores(Difficulty.Easy, false))
        assertEquals(listOf(5), scores(Difficulty.Easy, true))
        assertEquals(listOf(8), scores(Difficulty.Medium, false))
        assertEquals(emptyList<Int>(), scores(Difficulty.Medium, true))
        assertEquals(emptyList<Int>(), scores(Difficulty.Hard, false))
        assertEquals(listOf(10), scores(Difficulty.Hard, true))
        // A reverse best does not count against the normal best.
        assertTrue(store.record(Difficulty.Easy, true, 6))
    }

    @Test
    fun `scores and dates survive reloading`() {
        store.record(Difficulty.Medium, false, 8, achievedAt = 1_791_000_000_000)
        store.record(Difficulty.Medium, false, 6, achievedAt = 1_791_000_500_000)

        val reloaded = ScoreStore(prefs)
        assertEquals(
            listOf(ScoreEntry(8, 1_791_000_000_000), ScoreEntry(6, 1_791_000_500_000)),
            reloaded.top(Difficulty.Medium, false)
        )
    }

    @Test
    fun `damaged saved data is skipped instead of crashing`() {
        prefs.edit().putString("top_Easy", "7@100;garbage;x@1;;5@notadate;3@300").apply()
        assertEquals(
            listOf(ScoreEntry(7, 100), ScoreEntry(5, 0), ScoreEntry(3, 300)),
            store.top(Difficulty.Easy, false)
        )

        store.record(Difficulty.Easy, false, 6, achievedAt = 400)
        assertEquals(listOf(7, 6, 5, 3), scores())
    }
}

/** In-memory SharedPreferences, enough for ScoreStore. */
private class FakeSharedPreferences : SharedPreferences {
    private val values = mutableMapOf<String, Any?>()

    override fun getAll(): Map<String, *> = values.toMap()
    override fun getString(key: String, defValue: String?) = values[key] as String? ?: defValue
    @Suppress("UNCHECKED_CAST")
    override fun getStringSet(key: String, defValues: Set<String>?) = values[key] as Set<String>? ?: defValues
    override fun getInt(key: String, defValue: Int) = values[key] as Int? ?: defValue
    override fun getLong(key: String, defValue: Long) = values[key] as Long? ?: defValue
    override fun getFloat(key: String, defValue: Float) = values[key] as Float? ?: defValue
    override fun getBoolean(key: String, defValue: Boolean) = values[key] as Boolean? ?: defValue
    override fun contains(key: String) = key in values
    override fun registerOnSharedPreferenceChangeListener(l: SharedPreferences.OnSharedPreferenceChangeListener) = Unit
    override fun unregisterOnSharedPreferenceChangeListener(l: SharedPreferences.OnSharedPreferenceChangeListener) = Unit

    override fun edit(): SharedPreferences.Editor = object : SharedPreferences.Editor {
        private val pending = mutableMapOf<String, Any?>()
        private var clear = false

        override fun putString(key: String, value: String?) = apply { pending[key] = value }
        override fun putStringSet(key: String, values: Set<String>?) = apply { pending[key] = values }
        override fun putInt(key: String, value: Int) = apply { pending[key] = value }
        override fun putLong(key: String, value: Long) = apply { pending[key] = value }
        override fun putFloat(key: String, value: Float) = apply { pending[key] = value }
        override fun putBoolean(key: String, value: Boolean) = apply { pending[key] = value }
        override fun remove(key: String) = apply { pending[key] = null }
        override fun clear() = apply { clear = true }
        override fun commit(): Boolean {
            if (clear) values.clear()
            pending.forEach { (k, v) -> if (v == null) values.remove(k) else values[k] = v }
            return true
        }
        override fun apply() {
            commit()
        }
    }
}
