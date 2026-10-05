package com.theseuntaylor.stackboxes

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.theseuntaylor.stackboxes.ui.theme.heartColor
import com.theseuntaylor.stackboxes.ui.theme.heartGlintColor
import com.theseuntaylor.stackboxes.ui.theme.heartLostColor

// 8-bit heart: X is the body, o is the glint highlight.
private val HEART = listOf(
    ".XX.XX.",
    "XoXXXXX",
    "XXXXXXX",
    ".XXXXX.",
    "..XXX..",
    "...X...",
)

/** Retro pixel hearts, one per life; lost lives are drawn greyed out. */
@Composable
fun Lives(lives: Int, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.semantics { contentDescription = "Lives: $lives" },
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        repeat(MAX_LIVES) { i -> PixelHeart(full = i < lives) }
    }
}

@Composable
private fun PixelHeart(full: Boolean) {
    Canvas(Modifier.size(width = 28.dp, height = 24.dp)) {
        val pixel = size.width / HEART[0].length
        HEART.forEachIndexed { row, line ->
            line.forEachIndexed { col, c ->
                val color = when {
                    c == '.' -> return@forEachIndexed
                    !full -> heartLostColor
                    c == 'o' -> heartGlintColor
                    else -> heartColor
                }
                // Overlap by a hair so no seams show between pixels.
                drawRect(color, Offset(col * pixel, row * pixel), Size(pixel + 0.5f, pixel + 0.5f))
            }
        }
    }
}
