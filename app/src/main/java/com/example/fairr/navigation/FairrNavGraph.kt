package com.example.fairr.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fairr.ui.screens.MainScreen
import com.example.fairr.ui.screens.SplashScreen
import com.example.fairr.ui.screens.auth.MobileLoginScreen
import com.example.fairr.ui.screens.auth.MobileSignUpScreen
import com.example.fairr.ui.screens.auth.WelcomeScreen
import com.example.fairr.ui.screens.home.HomeScreen
import com.example.fairr.ui.screens.onboarding.OnboardingScreen
import com.example.fairr.ui.viewmodels.StartupViewModel

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Onboarding : Screen("onboarding")
    object Welcome : Screen("welcome")
    object MobileLogin : Screen("mobile_login")
    object MobileSignUp : Screen("mobile_signup")
    object Main : Screen("main")
    object CreateGroup : Screen("create_group")
    object JoinGroup : Screen("join_group")
    object Search : Screen("search")
    object Notifications : Screen("notifications")
    object GroupDetail : Screen("group_detail/{groupId}") {
        fun createRoute(groupId: String) = "group_detail/$groupId"
    }
    object Budgets : Screen("budgets")
    object Settings : Screen("settings")
    object AddExpense : Screen("add_expense/{groupId}") {
        fun createRoute(groupId: String) = "add_expense/$groupId"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FairrNavGraph() {
    val navController = rememberNavController()
    val startupViewModel: StartupViewModel = viewModel()
    val hasCompletedOnboarding by startupViewModel.onboardingCompleted.collectAsState()

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route,
        modifier = Modifier
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onTimeout = {
                    if (hasCompletedOnboarding) {
                        navController.navigate(Screen.Welcome.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    } else {
                        navController.navigate(Screen.Onboarding.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    }
                }
            )
        }

        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onGetStarted = {
                    startupViewModel.setOnboardingCompleted()
                    navController.navigate(Screen.Welcome.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Welcome.route) {
            WelcomeScreen(
                onNavigateToLogin = {
                    navController.navigate(Screen.MobileLogin.route)
                },
                onNavigateToSignUp = {
                    navController.navigate(Screen.MobileSignUp.route)
                }
            )
        }

        composable(Screen.MobileLogin.route) {
            MobileLoginScreen(
                navController = navController,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onLoginSuccess = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Welcome.route) { inclusive = true }
                    }
                },
                onNavigateToSignUp = {
                    navController.navigate(Screen.MobileSignUp.route) {
                        popUpTo(Screen.MobileLogin.route) { inclusive = true }
                    }
                },
                onNavigateToForgotPassword = {
                    // TODO: Implement forgot password navigation
                }
            )
        }

        composable(Screen.MobileSignUp.route) {
            MobileSignUpScreen(
                navController = navController,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onSignUpSuccess = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Welcome.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.navigate(Screen.MobileLogin.route) {
                        popUpTo(Screen.MobileSignUp.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Main.route) {
            MainScreen(
                onNavigateToAddExpense = {
                    navController.navigate(Screen.AddExpense.createRoute("default_group"))
                },
                onNavigateToCreateGroup = {
                    navController.navigate(Screen.CreateGroup.route)
                },
                onNavigateToJoinGroup = {
                    navController.navigate(Screen.JoinGroup.route)
                },
                onNavigateToSearch = {
                    navController.navigate(Screen.Search.route)
                },
                onNavigateToNotifications = {
                    navController.navigate(Screen.Notifications.route)
                },
                onNavigateToGroupDetail = { groupId ->
                    navController.navigate(Screen.GroupDetail.createRoute(groupId))
                },
                onNavigateToBudgets = {
                    navController.navigate(Screen.Budgets.route)
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                },
                onSignOut = {
                    navController.navigate(Screen.Welcome.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
} 