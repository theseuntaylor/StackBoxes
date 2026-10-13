package com.theseuntaylor.stackboxes

import kotlin.math.pow

private const val C4_HZ = 261.6256
private const val SPAN_SEMITONES = 24

/**
 * The pitch of a box: box 0 is C4 and the last box is C6, two octaves up, with every box in
 * between on its own semitone, rising with the box number. Smaller grids take bigger steps.
 */
fun boxToneHz(index: Int, boxCount: Int): Double {
    val semitones = index * SPAN_SEMITONES / (boxCount - 1)
    return C4_HZ * 2.0.pow(semitones / 12.0)
}
