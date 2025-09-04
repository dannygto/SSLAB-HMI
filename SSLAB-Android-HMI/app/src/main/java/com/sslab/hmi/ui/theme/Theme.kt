package com.sslab.hmi.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = DarkBlue500,
    onPrimary = Grey50,
    primaryContainer = DarkBlue700,
    onPrimaryContainer = LightBlue100,
    
    secondary = LightBlue300,
    onSecondary = DarkBlue900,
    secondaryContainer = DarkBlue800,
    onSecondaryContainer = LightBlue100,
    
    tertiary = Orange500,
    onTertiary = Grey50,
    tertiaryContainer = Orange600,
    onTertiaryContainer = Grey50,
    
    error = ErrorDark,
    onError = Grey900,
    errorContainer = Red500,
    onErrorContainer = Grey50,
    
    background = Grey900,
    onBackground = Grey100,
    surface = SurfaceDark,
    onSurface = Grey100,
    
    surfaceVariant = Grey800,
    onSurfaceVariant = Grey300,
    outline = Grey500,
    outlineVariant = Grey700,
    
    scrim = Grey900,
    inverseSurface = Grey100,
    inverseOnSurface = Grey800,
    inversePrimary = DarkBlue700
)

private val LightColorScheme = lightColorScheme(
    primary = DarkBlue700,
    onPrimary = Grey50,
    primaryContainer = LightBlue100,
    onPrimaryContainer = DarkBlue900,
    
    secondary = DarkBlue600,
    onSecondary = Grey50,
    secondaryContainer = LightBlue50,
    onSecondaryContainer = DarkBlue800,
    
    tertiary = Orange500,
    onTertiary = Grey50,
    tertiaryContainer = Color(0xFFFFE0B2),
    onTertiaryContainer = Color(0xFFE65100),
    
    error = ErrorLight,
    onError = Grey50,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    
    background = Grey50,
    onBackground = Grey900,
    surface = SurfaceLight,
    onSurface = Grey900,
    
    surfaceVariant = Grey200,
    onSurfaceVariant = Grey700,
    outline = Grey500,
    outlineVariant = Grey300,
    
    scrim = Grey900,
    inverseSurface = Grey800,
    inverseOnSurface = Grey100,
    inversePrimary = DarkBlue500
)

@Composable
fun SSLabHMITheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
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
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
