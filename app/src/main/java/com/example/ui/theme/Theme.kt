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
    primary = BazaarOrange,
    onPrimary = White,
    primaryContainer = BazaarOrangeDark,
    onPrimaryContainer = BazaarOrangeLight,
    secondary = BazaarTeal,
    onSecondary = White,
    tertiary = BazaarGold,
    background = Slate900,
    surface = Slate800,
    onBackground = Slate50,
    onSurface = Slate50,
    surfaceVariant = Slate700,
    onSurfaceVariant = Slate300,
    outline = Slate600
)

private val LightColorScheme = lightColorScheme(
    primary = BazaarOrange,
    onPrimary = White,
    primaryContainer = BazaarOrangeLight,
    onPrimaryContainer = BazaarOrangeDark,
    secondary = BazaarTeal,
    onSecondary = White,
    secondaryContainer = BazaarTealLight,
    onSecondaryContainer = BazaarTeal,
    tertiary = BazaarGold,
    onTertiary = White,
    tertiaryContainer = BazaarGoldLight,
    onTertiaryContainer = BazaarGold,
    background = Slate50,
    surface = White,
    onBackground = Slate900,
    onSurface = Slate900,
    surfaceVariant = Slate100,
    onSurfaceVariant = Slate600,
    outline = Slate300
)

@Composable
fun MeriLocalBazaarTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep signature brand orange & saffron aesthetic
    content: @Composable () -> Unit
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
