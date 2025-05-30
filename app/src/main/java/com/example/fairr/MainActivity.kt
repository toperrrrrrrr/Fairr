package com.example.fairr

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.example.fairr.navigation.FairrNavGraph
import com.example.fairr.ui.theme.FairrTheme
import com.example.fairr.ui.theme.LocalThemeManager
import com.example.fairr.ui.theme.ProvideThemeManager
import com.example.fairr.ui.theme.ThemeManager

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FairrApp()
        }
    }
}

@Composable
fun FairrApp() {
    ProvideThemeManager {
        val themeManager = LocalThemeManager.current
        val isDarkTheme = themeManager.isDarkTheme()
        
        FairrTheme(darkTheme = isDarkTheme) {
            // A surface container using the 'background' color from the theme
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                FairrNavGraph()
            }
        }
    }
}
