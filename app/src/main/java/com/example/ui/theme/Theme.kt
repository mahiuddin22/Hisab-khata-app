package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = IosDarkPrimary,
    onPrimary = IosDarkOnPrimary,
    primaryContainer = IosDarkSurfaceVariant,
    onPrimaryContainer = IosDarkTextPrimary,
    secondary = IosDarkSecondary,
    onSecondary = IosDarkBackground,
    secondaryContainer = IosDarkSurfaceVariant,
    onSecondaryContainer = IosDarkSecondary,
    tertiary = IosDarkTertiary,
    onTertiary = IosDarkBackground,
    background = IosDarkBackground,
    onBackground = IosDarkTextPrimary,
    surface = IosDarkSurface,
    onSurface = IosDarkTextPrimary,
    surfaceVariant = IosDarkSurfaceVariant,
    onSurfaceVariant = IosDarkTextSecondary,
    outline = IosDarkOutline
)

private val LightColorScheme = lightColorScheme(
    primary = IosLightPrimary,
    onPrimary = IosLightOnPrimary,
    primaryContainer = IosLightSurfaceVariant,
    onPrimaryContainer = IosLightTextPrimary,
    secondary = IosLightSecondary,
    onSecondary = IosLightOnPrimary,
    secondaryContainer = IosLightSurfaceVariant,
    onSecondaryContainer = IosLightSecondary,
    tertiary = IosLightTertiary,
    onTertiary = IosLightOnPrimary,
    background = IosLightBackground,
    onBackground = IosLightTextPrimary,
    surface = IosLightSurface,
    onSurface = IosLightTextPrimary,
    surfaceVariant = IosLightSurfaceVariant,
    onSurfaceVariant = IosLightTextSecondary,
    outline = IosLightOutline
)

@Composable
fun HisabKhataTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}
