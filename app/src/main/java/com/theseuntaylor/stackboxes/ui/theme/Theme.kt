package com.theseuntaylor.stackboxes.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import com.theseuntaylor.stackboxes.AppIcon

/** Material 3 theme in the colours of the chosen app icon, light or dark with the system. */
@Composable
fun StackBoxesTheme(
    icon: AppIcon = AppIcon.Arcade,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = when (icon) {
        AppIcon.Arcade -> if (darkTheme) ArcadeDark else ArcadeLight
        AppIcon.Sequence -> if (darkTheme) SequenceDark else SequenceLight
    }
    MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
