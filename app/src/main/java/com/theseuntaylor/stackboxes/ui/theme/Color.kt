package com.theseuntaylor.stackboxes.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// Pixel hearts: the same red as the heart on the Arcade icon.
val heartColor = Color(0xFFE53935)
val heartLostColor = Color(0xFFCFCFCF)
val heartGlintColor = Color.White

/** Arcade icon: mint, yellow and coral blocks on a plum-black ground. */
val ArcadeDark: ColorScheme = darkColorScheme(
    primary = Color(0xFF23E6A8), onPrimary = Color(0xFF00382A),
    primaryContainer = Color(0xFF00513D), onPrimaryContainer = Color(0xFF7DF5CF),
    secondary = Color(0xFFFFC93C), onSecondary = Color(0xFF3F2E00),
    secondaryContainer = Color(0xFF5B4300), onSecondaryContainer = Color(0xFFFFE08A),
    tertiary = Color(0xFFFF8A78), onTertiary = Color(0xFF5C1106),
    tertiaryContainer = Color(0xFF7E2617), onTertiaryContainer = Color(0xFFFFDAD3),
    background = Color(0xFF121018), onBackground = Color(0xFFE9E5F0),
    surface = Color(0xFF121018), onSurface = Color(0xFFE9E5F0),
    surfaceVariant = Color(0xFF2C2836), onSurfaceVariant = Color(0xFFC0B9CC),
    outline = Color(0xFF8A8396), outlineVariant = Color(0xFF3A3445),
    surfaceContainerLowest = Color(0xFF0C0A11), surfaceContainerLow = Color(0xFF1A1721),
    surfaceContainer = Color(0xFF1E1B26), surfaceContainerHigh = Color(0xFF292531),
    surfaceContainerHighest = Color(0xFF34303C),
)

val ArcadeLight: ColorScheme = lightColorScheme(
    primary = Color(0xFF006C4E), onPrimary = Color.White,
    primaryContainer = Color(0xFF23E6A8), onPrimaryContainer = Color(0xFF002116),
    secondary = Color(0xFF795900), onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFDF9A), onSecondaryContainer = Color(0xFF261A00),
    tertiary = Color(0xFFA93A2A), onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFDAD3), onTertiaryContainer = Color(0xFF3E0500),
    background = Color(0xFFFDF8FD), onBackground = Color(0xFF1C1A21),
    surface = Color(0xFFFDF8FD), onSurface = Color(0xFF1C1A21),
    surfaceVariant = Color(0xFFE7E0EC), onSurfaceVariant = Color(0xFF49454F),
    outline = Color(0xFF7A7581), outlineVariant = Color(0xFFCBC4D0),
    surfaceContainerLowest = Color.White, surfaceContainerLow = Color(0xFFF7F2F8),
    surfaceContainer = Color(0xFFF1ECF2), surfaceContainerHigh = Color(0xFFEBE6EC),
    surfaceContainerHighest = Color(0xFFE6E1E6),
)

/** Sequence icon: a mint trail on a navy grid. */
val SequenceDark: ColorScheme = darkColorScheme(
    primary = Color(0xFF23E6A8), onPrimary = Color(0xFF00382A),
    primaryContainer = Color(0xFF00513D), onPrimaryContainer = Color(0xFF7DF5CF),
    secondary = Color(0xFFB7C4FF), onSecondary = Color(0xFF1F2C61),
    secondaryContainer = Color(0xFF364379), onSecondaryContainer = Color(0xFFDCE1FF),
    tertiary = Color(0xFF9ED0FF), onTertiary = Color(0xFF003352),
    tertiaryContainer = Color(0xFF1F4A6B), onTertiaryContainer = Color(0xFFCFE5FF),
    background = Color(0xFF0F1222), onBackground = Color(0xFFE3E5F3),
    surface = Color(0xFF0F1222), onSurface = Color(0xFFE3E5F3),
    surfaceVariant = Color(0xFF252B47), onSurfaceVariant = Color(0xFFA3A9C4),
    outline = Color(0xFF6F7591), outlineVariant = Color(0xFF2E3658),
    surfaceContainerLowest = Color(0xFF0A0C18), surfaceContainerLow = Color(0xFF151930),
    surfaceContainer = Color(0xFF181C33), surfaceContainerHigh = Color(0xFF22273F),
    surfaceContainerHighest = Color(0xFF2D324B),
)

val SequenceLight: ColorScheme = lightColorScheme(
    primary = Color(0xFF006C4E), onPrimary = Color.White,
    primaryContainer = Color(0xFF23E6A8), onPrimaryContainer = Color(0xFF002116),
    secondary = Color(0xFF4C5C92), onSecondary = Color.White,
    secondaryContainer = Color(0xFFDCE1FF), onSecondaryContainer = Color(0xFF041451),
    tertiary = Color(0xFF33618A), onTertiary = Color.White,
    tertiaryContainer = Color(0xFFCFE5FF), onTertiaryContainer = Color(0xFF001D33),
    background = Color(0xFFF8F8FF), onBackground = Color(0xFF191B24),
    surface = Color(0xFFF8F8FF), onSurface = Color(0xFF191B24),
    surfaceVariant = Color(0xFFE1E2EF), onSurfaceVariant = Color(0xFF454859),
    outline = Color(0xFF767889), outlineVariant = Color(0xFFC6C7D6),
    surfaceContainerLowest = Color.White, surfaceContainerLow = Color(0xFFF2F3FB),
    surfaceContainer = Color(0xFFECEDF6), surfaceContainerHigh = Color(0xFFE6E8F1),
    surfaceContainerHighest = Color(0xFFE1E2EB),
)
