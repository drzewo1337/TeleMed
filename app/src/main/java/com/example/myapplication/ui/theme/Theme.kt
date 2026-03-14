package com.example.myapplication.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
    primary = SeniorPrimary,
    onPrimary = SeniorOnPrimary,
    primaryContainer = SeniorPrimaryContainer,
    onPrimaryContainer = SeniorOnPrimaryContainer,
    secondary = SeniorSecondary,
    onSecondary = SeniorOnSecondary,
    secondaryContainer = SeniorSecondaryContainer,
    onSecondaryContainer = SeniorOnSecondaryContainer,
    background = SeniorBackground,
    onBackground = SeniorOnBackground,
    surface = SeniorSurface,
    onSurface = SeniorOnSurface,
    surfaceVariant = SeniorSurfaceVariant,
    onSurfaceVariant = SeniorOnSurfaceVariant,
    error = SeniorError,
    onError = SeniorOnError,
    errorContainer = SeniorErrorContainer,
    outline = SeniorOutline
)

private val DarkColorScheme = darkColorScheme(
    primary = SeniorPrimaryContainer,
    onPrimary = SeniorOnPrimaryContainer,
    primaryContainer = SeniorPrimary,
    onPrimaryContainer = SeniorOnPrimary,
    secondary = SeniorSecondaryContainer,
    onSecondary = SeniorOnSecondaryContainer,
    secondaryContainer = SeniorSecondary,
    onSecondaryContainer = SeniorOnSecondary,
    background = SeniorOnBackground,
    onBackground = SeniorBackground,
    surface = SeniorOnSurface,
    onSurface = SeniorSurface,
    surfaceVariant = SeniorOnSurfaceVariant,
    onSurfaceVariant = SeniorSurfaceVariant,
    error = SeniorErrorContainer,
    onError = SeniorOnError,
    errorContainer = SeniorError,
    outline = SeniorOutline
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
