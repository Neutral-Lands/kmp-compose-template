package com.nouri.presentation.shared

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color

// ── Palette ──────────────────────────────────────────────────────────────────

private val ColorNouriGreen = Color(0xFF4A7C59)
private val ColorNouriGreenLight = Color(0xFF9DC9AC)
private val ColorNouriGreenContainer = Color(0xFFB8E0C8)
private val ColorNouriGreenDark = Color(0xFF002111)
private val ColorNouriGreenDim = Color(0xFF336244)
private val ColorNouriSecondary = Color(0xFF52634F)
private val ColorNouriSecondaryContainer = Color(0xFFD5E8CE)
private val ColorNouriSecondaryDark = Color(0xFF101F0F)
private val ColorNouriSecondaryDim = Color(0xFF253423)
private val ColorNouriSecondaryDimContainer = Color(0xFF3B4B39)
private val ColorNouriSecondaryLight = Color(0xFFB9CCB3)
private val ColorNouriBackground = Color(0xFFF8FBF5)
private val ColorNouriOnBackground = Color(0xFF191C19)
private val ColorNouriBackgroundDark = Color(0xFF191C19)
private val ColorNouriOnBackgroundDark = Color(0xFFE1E4DE)
private val ColorNouriError = Color(0xFFBA1A1A)
private val ColorNouriErrorContainer = Color(0xFFFFDAD6)
private val ColorNouriOnError = Color(0xFF410002)
private val ColorNouriErrorDark = Color(0xFFFFB4AB)
private val ColorNouriErrorDarkOn = Color(0xFF690005)
private val ColorNouriErrorDarkContainer = Color(0xFF93000A)

// ── Schemes ───────────────────────────────────────────────────────────────────

private val LightColorScheme = lightColorScheme(
    primary = ColorNouriGreen,
    onPrimary = Color.White,
    primaryContainer = ColorNouriGreenContainer,
    onPrimaryContainer = ColorNouriGreenDark,
    secondary = ColorNouriSecondary,
    onSecondary = Color.White,
    secondaryContainer = ColorNouriSecondaryContainer,
    onSecondaryContainer = ColorNouriSecondaryDark,
    background = ColorNouriBackground,
    onBackground = ColorNouriOnBackground,
    surface = ColorNouriBackground,
    onSurface = ColorNouriOnBackground,
    error = ColorNouriError,
    onError = Color.White,
    errorContainer = ColorNouriErrorContainer,
    onErrorContainer = ColorNouriOnError,
)

private val DarkColorScheme = darkColorScheme(
    primary = ColorNouriGreenLight,
    onPrimary = ColorNouriGreenDark,
    primaryContainer = ColorNouriGreenDim,
    onPrimaryContainer = ColorNouriGreenContainer,
    secondary = ColorNouriSecondaryLight,
    onSecondary = ColorNouriSecondaryDim,
    secondaryContainer = ColorNouriSecondaryDimContainer,
    onSecondaryContainer = ColorNouriSecondaryContainer,
    background = ColorNouriBackgroundDark,
    onBackground = ColorNouriOnBackgroundDark,
    surface = ColorNouriBackgroundDark,
    onSurface = ColorNouriOnBackgroundDark,
    error = ColorNouriErrorDark,
    onError = ColorNouriErrorDarkOn,
    errorContainer = ColorNouriErrorDarkContainer,
    onErrorContainer = ColorNouriErrorContainer,
)

// ── Theme entry point ─────────────────────────────────────────────────────────

@Composable
fun NouriTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = MaterialTheme.typography,
        content = content,
    )
}

// ── Token accessors ───────────────────────────────────────────────────────────

object NouriTheme {
    val colors: ColorScheme
        @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme

    val typography: Typography
        @Composable @ReadOnlyComposable get() = MaterialTheme.typography
}
