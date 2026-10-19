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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.theseuntaylor.stackboxes.ui.theme.heartColor
import com.theseuntaylor.stackboxes.ui.theme.heartGlintColor

// 8-bit heart: X is the body, o is the glint highlight.
private val HEART = listOf(
    ".XX.XX.",
    "XoXXXXX",
    "XXXXXXX",
    ".XXXXX.",
    "..XXX..",
    "...X...",
)

// A smooth heart in a 24-unit box, for the Sequence style.
private val SMOOTH_HEART = PathParser().parsePathString(
    "M12,21.35l-1.45,-1.32C5.4,15.36 2,12.28 2,8.5 2,5.42 4.42,3 7.5,3c1.74,0 3.41,0.81 4.5,2.09" +
        "C13.09,3.81 14.76,3 16.5,3 19.58,3 22,5.42 22,8.5c0,3.78 -3.4,6.86 -8.55,11.54L12,21.35z"
).toPath()

/**
 * One heart per life, in the chosen icon's style: pixel hearts for Arcade (lost lives greyed
 * out), smooth hearts for Sequence (lost lives drawn as an empty outline).
 */
@Composable
fun Lives(lives: Int, style: AppIcon, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.semantics {
            contentDescription = "Lives: $lives"
            liveRegion = LiveRegionMode.Polite
        },
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        repeat(MAX_LIVES) { i ->
            when (style) {
                AppIcon.Arcade -> PixelHeart(full = i < lives)
                AppIcon.Sequence -> SmoothHeart(full = i < lives)
            }
        }
    }
}

@Composable
private fun PixelHeart(full: Boolean) {
    val lost = MaterialTheme.colorScheme.outlineVariant
    Canvas(Modifier.size(width = 28.dp, height = 24.dp)) {
        val pixel = size.width / HEART[0].length
        HEART.forEachIndexed { row, line ->
            line.forEachIndexed { col, c ->
                val color = when {
                    c == '.' -> return@forEachIndexed
                    !full -> lost
                    c == 'o' -> heartGlintColor
                    else -> heartColor
                }
                // Overlap by a hair so no seams show between pixels.
                drawRect(color, Offset(col * pixel, row * pixel), Size(pixel + 0.5f, pixel + 0.5f))
            }
        }
    }
}

@Composable
private fun SmoothHeart(full: Boolean) {
    val color = MaterialTheme.colorScheme.primary
    val outline = MaterialTheme.colorScheme.outline
    Canvas(Modifier.size(width = 28.dp, height = 24.dp)) {
        // Fit the 24-unit heart to the box's height, centred across its width.
        val unit = size.height / 24f
        translate(left = (size.width - 24f * unit) / 2f) {
            scale(unit, pivot = Offset.Zero) {
                if (full) drawPath(SMOOTH_HEART, color)
                else drawPath(SMOOTH_HEART, outline, style = Stroke(width = 2f))
            }
        }
    }
}
