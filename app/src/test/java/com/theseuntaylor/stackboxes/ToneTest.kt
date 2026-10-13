package com.theseuntaylor.stackboxes

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ToneTest {

    @Test
    fun `every box has its own pitch, rising with the box number`() {
        for (difficulty in Difficulty.entries) {
            val pitches = (0 until difficulty.boxCount).map { boxToneHz(it, difficulty.boxCount) }
            assertTrue("${difficulty.label}: $pitches", pitches.zipWithNext().all { (a, b) -> b > a })
        }
    }

    @Test
    fun `every grid spans two octaves, C4 to C6`() {
        for (difficulty in Difficulty.entries) {
            assertEquals(261.63, boxToneHz(0, difficulty.boxCount), 0.01)
            assertEquals(1046.50, boxToneHz(difficulty.boxCount - 1, difficulty.boxCount), 0.01)
        }
    }

    @Test
    fun `pitches land on notes of the scale`() {
        for (difficulty in Difficulty.entries) {
            for (index in 0 until difficulty.boxCount) {
                val semitones = 12 * kotlin.math.log2(boxToneHz(index, difficulty.boxCount) / 261.63)
                assertEquals(Math.round(semitones).toDouble(), semitones, 0.001)
            }
        }
    }
}
