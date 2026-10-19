package com.theseuntaylor.stackboxes.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import com.theseuntaylor.stackboxes.AppIcon
import com.theseuntaylor.stackboxes.Appearance

/** Material 3 theme in the colours of the chosen app icon, light or dark as the player chose. */
@Composable
fun StackBoxesTheme(
    icon: AppIcon = AppIcon.Arcade,
    appearance: Appearance = Appearance.System,
    content: @Composable () -> Unit,
) {
    val darkTheme = when (appearance) {
        Appearance.System -> isSystemInDarkTheme()
        Appearance.Light -> false
        Appearance.Dark -> true
    }
    val colorScheme = when (icon) {
        AppIcon.Arcade -> if (darkTheme) ArcadeDark else ArcadeLight
        AppIcon.Sequence -> if (darkTheme) SequenceDark else SequenceLight
    }
    MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
