package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = EcoGreenTertiary,
    onPrimary = Color.White,
    primaryContainer = EcoGreenPrimary,
    onPrimaryContainer = EcoGreenLight,
    secondary = EcoGreenSecondary,
    onSecondary = Color.White,
    background = DarkSurface,
    surface = DarkCard,
    onBackground = Color(0xFFE8F5E9),
    onSurface = Color(0xFFE8F5E9),
    error = HazardAlertRed,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = EcoGreenPrimary,
    onPrimary = Color.White,
    primaryContainer = EcoGreenLight,
    onPrimaryContainer = EcoGreenPrimary,
    secondary = EcoGreenSecondary,
    onSecondary = Color.White,
    background = Color(0xFFF6FAF6),
    surface = Color.White,
    onBackground = Color(0xFF131D14),
    onSurface = Color(0xFF131D14),
    error = HazardAlertRed,
    onError = Color.White
)

@Composable
fun SortAiTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep consistent brand identity
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

// Backward compatibility alias
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) = SortAiTheme(darkTheme = darkTheme, dynamicColor = dynamicColor, content = content)
