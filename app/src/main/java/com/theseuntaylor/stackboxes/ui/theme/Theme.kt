package com.theseuntaylor.stackboxes.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

/** Material 3 theme in the Arcade icon's colours, light or dark with the system. */
@Composable
fun StackBoxesTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (darkTheme) ArcadeDark else ArcadeLight,
        typography = Typography,
        content = content
    )
}
