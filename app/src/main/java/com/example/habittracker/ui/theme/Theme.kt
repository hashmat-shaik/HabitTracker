package com.example.habittracker.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = HabitBlue,
    secondary = HabitGreen,
    tertiary = HabitOrange,
    background = DarkBackground,
    surface = DarkSurface,
    onBackground = Color.White,
    onSurface = DarkOnSurface,
)

@Composable
fun HabitTrackerTheme(
    darkTheme: Boolean = true, // Force dark theme as per requirements
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
