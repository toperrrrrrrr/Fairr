package com.example.fairr.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

data class ThemeState(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val isDarkMode: Boolean = false
)

class ThemeManager {
    var themeState by mutableStateOf(ThemeState())
        private set

    fun setThemeMode(mode: ThemeMode) {
        themeState = themeState.copy(themeMode = mode)
    }

    fun toggleDarkMode() {
        val newMode = when (themeState.themeMode) {
            ThemeMode.LIGHT -> ThemeMode.DARK
            ThemeMode.DARK -> ThemeMode.LIGHT
            ThemeMode.SYSTEM -> if (themeState.isDarkMode) ThemeMode.LIGHT else ThemeMode.DARK
        }
        setThemeMode(newMode)
    }

    @Composable
    fun isDarkTheme(): Boolean {
        val systemDarkTheme = isSystemInDarkTheme()
        return when (themeState.themeMode) {
            ThemeMode.SYSTEM -> systemDarkTheme
            ThemeMode.LIGHT -> false
            ThemeMode.DARK -> true
        }
    }
}

val LocalThemeManager = compositionLocalOf<ThemeManager> {
    error("ThemeManager not provided")
}

@Composable
fun ProvideThemeManager(
    themeManager: ThemeManager = remember { ThemeManager() },
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalThemeManager provides themeManager,
        content = content
    )
} 