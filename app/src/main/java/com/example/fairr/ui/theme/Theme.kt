package com.example.fairr.ui.theme

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
    primary = AccentGreen,
    onPrimary = PureWhite,
    primaryContainer = SuccessGreenDark,
    onPrimaryContainer = PureWhite,
    
    secondary = AccentBlue,
    onSecondary = PureWhite,
    secondaryContainer = InfoBlueDark,
    onSecondaryContainer = PureWhite,
    
    tertiary = AccentOrange,
    onTertiary = PureWhite,
    tertiaryContainer = WarningOrangeDark,
    onTertiaryContainer = PureWhite,
    
    background = SoftBlack,
    onBackground = TextOnDark,
    surface = CharcoalGray,
    onSurface = TextOnDark,
    surfaceVariant = CharcoalGray,
    onSurfaceVariant = TextOnDark,
    
    error = AccentRed,
    onError = PureWhite,
    errorContainer = ErrorRedDark,
    onErrorContainer = PureWhite,
    
    outline = MediumGray,
    outlineVariant = MediumGray.copy(alpha = 0.5f)
)

private val LightColorScheme = lightColorScheme(
    primary = Primary,
    onPrimary = TextOnDark,
    primaryContainer = SuccessGreenLight,
    onPrimaryContainer = SuccessGreenDark,
    
    secondary = Secondary,
    onSecondary = TextOnDark,
    secondaryContainer = InfoBlueLight,
    onSecondaryContainer = InfoBlueDark,
    
    tertiary = AccentBlue,
    onTertiary = PureWhite,
    tertiaryContainer = InfoBlueLight,
    onTertiaryContainer = InfoBlueDark,
    
    background = BackgroundPrimary,
    onBackground = TextPrimary,
    surface = Surface,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = TextPrimary,
    
    error = ErrorRed,
    onError = PureWhite,
    errorContainer = ErrorRedLight,
    onErrorContainer = ErrorRedDark,
    
    outline = TextSecondary,
    outlineVariant = TextSecondary.copy(alpha = 0.5f)
)

@Composable
fun FairrTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false, // Disabled to use our custom colors
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
            window.statusBarColor = colorScheme.surface.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
