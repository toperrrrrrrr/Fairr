package com.example.fairr

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import com.example.fairr.navigation.FairrNavGraph
import com.example.fairr.navigation.Screen
import com.example.fairr.ui.screens.SplashScreen
import com.example.fairr.ui.theme.FairrTheme
import com.example.fairr.ui.viewmodels.StartupViewModel
import com.example.fairr.ui.viewmodels.StartupState
import com.example.fairr.data.notifications.RecurringExpenseNotificationService
import com.example.fairr.data.repository.RecurringExpenseScheduler
import com.example.fairr.data.analytics.RecurringExpenseAnalytics
import com.example.fairr.data.auth.AuthService
import com.example.fairr.data.settings.SettingsDataStore
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    @Inject
    lateinit var recurringExpenseNotificationService: RecurringExpenseNotificationService
    
    @Inject
    lateinit var recurringExpenseScheduler: RecurringExpenseScheduler
    
    @Inject
    lateinit var recurringExpenseAnalytics: RecurringExpenseAnalytics
    
    @Inject
    lateinit var authService: AuthService
    
    @Inject
    lateinit var settingsDataStore: SettingsDataStore
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Set navigation bar background to white and icons to dark before Compose content
        WindowCompat.setDecorFitsSystemWindows(window, true)
        window.navigationBarColor = android.graphics.Color.WHITE
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightNavigationBars = true
        setContent {
            // Collect dark mode setting
            val isDarkMode by settingsDataStore.darkModeEnabled.collectAsState(initial = false)
            
            FairrTheme(darkTheme = isDarkMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val startupViewModel: StartupViewModel = hiltViewModel()
                    
                    // Collect authentication state
                    val startupState by startupViewModel.startupState.collectAsState()
                    val isAuthenticated by startupViewModel.isAuthenticated.collectAsState()
                    val authLoading by startupViewModel.authLoading.collectAsState()
                    val authError by startupViewModel.authError.collectAsState()

                    // State to control splash screen visibility
                    var showSplash by remember { mutableStateOf(true) }

                    // Show splash screen for minimum time and until startup state is determined
                    LaunchedEffect(startupState) {
                        delay(2000) // 2 second minimum splash screen time
                        
                        // Hide splash when we have a determined startup state (not Loading)
                        if (startupState != StartupState.Loading) {
                            showSplash = false
                        }
                    }

                    // Trigger notification check when user is authenticated
                    LaunchedEffect(isAuthenticated) {
                        if (isAuthenticated) {
                            // Small delay to ensure app is fully loaded
                            delay(1000)
                            recurringExpenseNotificationService.triggerNotificationCheck()
                        }
                    }

                    // Update navigation bar based on theme
                    LaunchedEffect(isDarkMode) {
                        window.navigationBarColor = if (isDarkMode) {
                            android.graphics.Color.BLACK
                        } else {
                            android.graphics.Color.WHITE
                        }
                        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightNavigationBars = !isDarkMode
                    }

                    // Function to handle complete app reset
                    fun handleAppReset() {
                        startupViewModel.resetToInitialState()
                        showSplash = true // Show splash during reset
                    }

                    // Show splash screen during loading or initial startup determination
                    if (showSplash || startupState == StartupState.Loading || authLoading) {
                        SplashScreen(
                            onNavigateToMain = { 
                                // Handle navigation to main app when ready
                                if (!authLoading && startupState != StartupState.Loading) {
                                    showSplash = false 
                                }
                            },
                            onNavigateToAuth = { startupViewModel.retryAuthValidation() },
                            authLoading = authLoading,
                            isUserAuthenticated = isAuthenticated,
                            authError = authError,
                            onClearError = { startupViewModel.clearAuthError() }
                        )
                    } else {
                        // Show main navigation when ready
                        FairrNavGraph(
                            navController = navController,
                            startupViewModel = startupViewModel,
                            onAppReset = { handleAppReset() }
                        )
                    }
                }
            }
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        // Clean up all services to prevent memory leaks
        try {
            recurringExpenseNotificationService.cleanup()
            recurringExpenseScheduler.cleanup()
            recurringExpenseAnalytics.cleanup()
            authService.cleanup()
        } catch (e: Exception) {
            // Log error but don't crash during cleanup
            android.util.Log.e("MainActivity", "Error during cleanup", e)
        }
    }
}
