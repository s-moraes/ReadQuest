package com.saulo.readquest.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK,
    CUSTOM
}

object EyeComfortPresets {
    const val SEPIA_ID = "SEPIA"
    const val SEPIA_BG = "#F4ECD8"
    const val SEPIA_TEXT = "#2C2416"

    const val NIGHT_OWL_ID = "NIGHT_OWL"
    const val NIGHT_OWL_BG = "#1E1D1B"
    const val NIGHT_OWL_TEXT = "#E8DFD1"
}

fun parseHexColor(hex: String, fallback: Color): Color {
    return try {
        val clean = hex.removePrefix("#").trim()
        val colorInt = when (clean.length) {
            6 -> (0xFF000000L or clean.toLong(16)).toInt()
            8 -> clean.toLong(16).toInt()
            else -> return fallback
        }
        Color(colorInt)
    } catch (_: Exception) {
        fallback
    }
}

private fun Color.blendWith(other: Color, ratio: Float): Color {
    val r = this.red + (other.red - this.red) * ratio
    val g = this.green + (other.green - this.green) * ratio
    val b = this.blue + (other.blue - this.blue) * ratio
    return Color(r, g, b, this.alpha)
}

fun createCustomColorScheme(
    backgroundColor: Color,
    textColor: Color
): ColorScheme {
    val isBgDark = backgroundColor.luminance() < 0.5f

    val surfaceColor = if (isBgDark) {
        backgroundColor.blendWith(Color.White, 0.08f)
    } else {
        backgroundColor.blendWith(Color.Black, 0.04f)
    }

    val surfaceVariantColor = if (isBgDark) {
        backgroundColor.blendWith(Color.White, 0.14f)
    } else {
        backgroundColor.blendWith(Color.Black, 0.08f)
    }

    val outlineColor = if (isBgDark) {
        textColor.copy(alpha = 0.25f)
    } else {
        textColor.copy(alpha = 0.20f)
    }

    val outlineVariantColor = if (isBgDark) {
        textColor.copy(alpha = 0.15f)
    } else {
        textColor.copy(alpha = 0.10f)
    }

    val primaryColor = if (isBgDark) {
        Color(0xFFD4A373)
    } else {
        Color(0xFF8D5B28)
    }

    val primaryContainer = if (isBgDark) {
        primaryColor.copy(alpha = 0.25f)
    } else {
        primaryColor.copy(alpha = 0.18f)
    }

    return if (isBgDark) {
        darkColorScheme(
            primary = primaryColor,
            onPrimary = Color.Black,
            primaryContainer = primaryContainer,
            onPrimaryContainer = textColor,
            background = backgroundColor,
            onBackground = textColor,
            surface = surfaceColor,
            onSurface = textColor,
            surfaceVariant = surfaceVariantColor,
            onSurfaceVariant = textColor.copy(alpha = 0.75f),
            outline = outlineColor,
            outlineVariant = outlineVariantColor
        )
    } else {
        lightColorScheme(
            primary = primaryColor,
            onPrimary = Color.White,
            primaryContainer = primaryContainer,
            onPrimaryContainer = textColor,
            background = backgroundColor,
            onBackground = textColor,
            surface = surfaceColor,
            onSurface = textColor,
            surfaceVariant = surfaceVariantColor,
            onSurfaceVariant = textColor.copy(alpha = 0.75f),
            outline = outlineColor,
            outlineVariant = outlineVariantColor
        )
    }
}

private val LightColorScheme = lightColorScheme(
    primary = LightPrimary,
    onPrimary = LightOnPrimary,
    primaryContainer = LightPrimaryContainer,
    onPrimaryContainer = LightOnPrimaryContainer,
    secondary = LightSecondary,
    onSecondary = LightOnSecondary,
    secondaryContainer = LightSecondaryContainer,
    onSecondaryContainer = LightOnSecondaryContainer,
    tertiary = LightTertiary,
    onTertiary = LightOnTertiary,
    tertiaryContainer = LightTertiaryContainer,
    onTertiaryContainer = LightOnTertiaryContainer,
    background = LightBackground,
    onBackground = LightOnBackground,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant,
    outline = LightOutline,
    outlineVariant = LightOutlineVariant
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = DarkOnPrimary,
    primaryContainer = DarkPrimaryContainer,
    onPrimaryContainer = DarkOnPrimaryContainer,
    secondary = DarkSecondary,
    onSecondary = DarkOnSecondary,
    secondaryContainer = DarkSecondaryContainer,
    onSecondaryContainer = DarkOnSecondaryContainer,
    tertiary = DarkTertiary,
    onTertiary = DarkOnTertiary,
    tertiaryContainer = DarkTertiaryContainer,
    onTertiaryContainer = DarkOnTertiaryContainer,
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    outline = DarkOutline,
    outlineVariant = DarkOutlineVariant
)

@Composable
fun StudyHubTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    fontScale: Float = 1.0f,
    customBackgroundHex: String = EyeComfortPresets.SEPIA_BG,
    customTextHex: String = EyeComfortPresets.SEPIA_TEXT,
    content: @Composable () -> Unit
) {
    val isSystemDark = isSystemInDarkTheme()
    val colorScheme = when (themeMode) {
        ThemeMode.SYSTEM -> if (isSystemDark) DarkColorScheme else LightColorScheme
        ThemeMode.LIGHT -> LightColorScheme
        ThemeMode.DARK -> DarkColorScheme
        ThemeMode.CUSTOM -> {
            val bg = remember(customBackgroundHex) {
                parseHexColor(customBackgroundHex, Color(0xFFF4ECD8))
            }
            val text = remember(customTextHex) {
                parseHexColor(customTextHex, Color(0xFF2C2416))
            }
            remember(bg, text) {
                createCustomColorScheme(bg, text)
            }
        }
    }

    val scaledTypography = remember(fontScale) { scaledTypography(Typography, fontScale) }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = scaledTypography,
        content = content
    )
}
