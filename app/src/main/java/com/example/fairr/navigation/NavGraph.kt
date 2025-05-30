package com.example.fairr.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.fairr.ui.screens.SplashScreen
import com.example.fairr.ui.screens.auth.LoginScreen
import com.example.fairr.ui.screens.auth.RegisterScreen
import com.example.fairr.ui.screens.expenses.AddExpenseScreen
import com.example.fairr.ui.screens.expenses.ExpenseDetailScreen
import com.example.fairr.ui.screens.groups.CreateGroupScreen
import com.example.fairr.ui.screens.groups.GroupDetailScreen
import com.example.fairr.ui.screens.groups.GroupSettingsScreen
import com.example.fairr.ui.screens.groups.JoinGroupScreen
import com.example.fairr.ui.screens.home.HomeScreen
import com.example.fairr.ui.screens.notifications.NotificationsScreen
import com.example.fairr.ui.screens.onboarding.OnboardingScreen
import com.example.fairr.ui.screens.settings.SettingsScreen

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Onboarding : Screen("onboarding")
    object Login : Screen("login")
    object Register : Screen("register")
    object Home : Screen("home")
    object Settings : Screen("settings")
    object CreateGroup : Screen("create_group")
    object JoinGroup : Screen("join_group")
    object GroupDetail : Screen("group_detail/{groupId}") {
        fun createRoute(groupId: String) = "group_detail/$groupId"
    }
    object GroupSettings : Screen("group_settings/{groupId}") {
        fun createRoute(groupId: String) = "group_settings/$groupId"
    }
    object AddExpense : Screen("add_expense/{groupId}") {
        fun createRoute(groupId: String) = "add_expense/$groupId"
    }
    object ExpenseDetail : Screen("expense_detail/{expenseId}") {
        fun createRoute(expenseId: String) = "expense_detail/$expenseId"
    }
    object Notifications : Screen("notifications")
}

@Composable
fun FairrNavGraph(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Screen.Splash.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onTimeout = {
                    // Check if user is first time user
                    // For demo purposes, always show onboarding
                    navController.navigate(Screen.Onboarding.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onGetStarted = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.Login.route) {
            LoginScreen(
                navController = navController,
                onLoginSuccess = {
                    // Navigate to home on successful login
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                }
            )
        }
        
        composable(Screen.Register.route) {
            RegisterScreen(
                navController = navController,
                onRegisterSuccess = {
                    // Navigate to home on successful registration
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToCreateGroup = {
                    navController.navigate(Screen.CreateGroup.route)
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                },
                onNavigateToGroupDetail = { groupId ->
                    navController.navigate(Screen.GroupDetail.createRoute(groupId))
                }
            )
        }
        
        composable(Screen.Settings.route) {
            SettingsScreen(
                navController = navController,
                onSignOut = {
                    // Handle sign out
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.CreateGroup.route) {
            CreateGroupScreen(
                navController = navController,
                onGroupCreated = {
                    // Navigate back to home after group creation
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.JoinGroup.route) {
            JoinGroupScreen(
                navController = navController,
                onGroupJoined = {
                    // Navigate back to home after joining group
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.JoinGroup.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.GroupDetail.route) { backStackEntry ->
            val groupId = backStackEntry.arguments?.getString("groupId") ?: ""
            GroupDetailScreen(
                navController = navController,
                groupId = groupId,
                onNavigateToAddExpense = {
                    navController.navigate(Screen.AddExpense.createRoute(groupId))
                }
            )
        }
        
        composable(Screen.GroupSettings.route) { backStackEntry ->
            val groupId = backStackEntry.arguments?.getString("groupId") ?: ""
            GroupSettingsScreen(
                navController = navController,
                groupId = groupId,
                onLeaveGroup = {
                    // Navigate back to home after leaving group
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.GroupSettings.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.AddExpense.route) { backStackEntry ->
            val groupId = backStackEntry.arguments?.getString("groupId") ?: ""
            AddExpenseScreen(
                navController = navController,
                groupId = groupId,
                onExpenseAdded = {
                    // Navigate back to group detail after expense is added
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.ExpenseDetail.route) { backStackEntry ->
            val expenseId = backStackEntry.arguments?.getString("expenseId") ?: ""
            ExpenseDetailScreen(
                navController = navController,
                expenseId = expenseId,
                onEditExpense = {
                    // TODO: Navigate to edit expense screen
                },
                onDeleteExpense = {
                    // Navigate back after deleting expense
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.Notifications.route) {
            NotificationsScreen(
                navController = navController,
                onNotificationClick = { notificationId ->
                    // TODO: Handle notification click based on type
                    // For now, just navigate to specific screens based on notification
                }
            )
        }
    }
}
