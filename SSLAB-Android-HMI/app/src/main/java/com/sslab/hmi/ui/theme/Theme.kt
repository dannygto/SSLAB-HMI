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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryBlue,
    onPrimary = TextOnBlue,
    primaryContainer = SecondaryBlue,
    onPrimaryContainer = TextOnBlue,
    
    secondary = LightBlue,
    onSecondary = PrimaryBlue,
    secondaryContainer = SecondaryBlue,
    onSecondaryContainer = TextOnBlue,
    
    tertiary = VeryLightBlue,
    onTertiary = PrimaryBlue,
    tertiaryContainer = LightBlue,
    onTertiaryContainer = PrimaryBlue,
    
    error = ErrorRed,
    onError = TextOnBlue,
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    
    background = BackgroundDark,
    onBackground = TextOnBlue,
    surface = SurfaceDark,
    onSurface = TextOnBlue,
    
    surfaceVariant = Color(0xFF49454E),
    onSurfaceVariant = Color(0xFFCAC4D0),
    outline = Color(0xFF938F99),
    outlineVariant = Color(0xFF49454E),
    
    scrim = Color(0xFF000000),
    inverseSurface = Color(0xFFE6E1E5),
    inverseOnSurface = Color(0xFF313033),
    inversePrimary = PrimaryBlue
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryBlue,
    onPrimary = TextOnBlue,
    primaryContainer = VeryLightBlue,
    onPrimaryContainer = PrimaryBlue,
    
    secondary = SecondaryBlue,
    onSecondary = TextOnBlue,
    secondaryContainer = LightBlue50,
    onSecondaryContainer = PrimaryBlue,
    
    tertiary = LightBlue,
    onTertiary = PrimaryBlue,
    tertiaryContainer = Color(0xFFE3F2FD),
    onTertiaryContainer = PrimaryBlue,
    
    error = ErrorRed,
    onError = TextOnBlue,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    
    background = BackgroundLight,
    onBackground = TextPrimary,
    surface = SurfaceLight,
    onSurface = TextPrimary,
    
    surfaceVariant = Color(0xFFE7E0EC),
    onSurfaceVariant = Color(0xFF49454E),
    outline = Color(0xFF79747E),
    outlineVariant = Color(0xFFCAC4D0),
    
    scrim = Color(0xFF000000),
    inverseSurface = Color(0xFF313033),
    inverseOnSurface = Color(0xFFF4EFF4),
    inversePrimary = LightBlue
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
