package com.example.fairr.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.tween
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.fairr.ui.screens.*
import com.example.fairr.ui.screens.auth.*
import com.example.fairr.ui.screens.groups.*
import com.example.fairr.ui.screens.settings.*
import com.example.fairr.ui.screens.onboarding.*
import com.example.fairr.ui.screens.expenses.*
import com.example.fairr.ui.screens.friends.FriendsScreen
import com.example.fairr.ui.screens.profile.EditProfileScreen
import com.example.fairr.ui.screens.profile.UserProfileScreen
import com.example.fairr.ui.screens.support.HelpSupportScreen
import com.example.fairr.ui.screens.support.PrivacyPolicyScreen
import com.example.fairr.ui.screens.support.ContactSupportScreen
import com.example.fairr.ui.screens.categories.CategoryManagementScreen
import com.example.fairr.ui.screens.export.ExportDataScreen
import com.example.fairr.ui.screens.groups.GroupSettingsScreen
import com.example.fairr.ui.screens.groups.GroupActivityScreen
import com.example.fairr.ui.screens.settlements.SettlementScreen
import com.example.fairr.ui.screens.settlements.SettlementsOverviewScreen
import com.example.fairr.ui.screens.expenses.ExpenseDetailScreen
import com.example.fairr.ui.screens.expenses.EditExpenseScreen
import com.example.fairr.ui.screens.search.SearchScreen
import com.example.fairr.ui.screens.notifications.NotificationsScreen
import com.example.fairr.ui.viewmodels.StartupViewModel
import com.example.fairr.ui.viewmodels.StartupState
import com.example.fairr.ui.screens.auth.AuthViewModel
import com.example.fairr.ui.screens.auth.WelcomeScreen
import com.example.fairr.ui.screens.auth.ForgotPasswordScreen
import com.example.fairr.ui.screens.auth.AccountVerificationScreen
import androidx.navigation.NavHostController
import com.example.fairr.ui.screens.SplashScreen
import com.example.fairr.ui.screens.home.HomeScreen
import com.example.fairr.ui.screens.groups.GroupDetailScreen
import com.example.fairr.ui.screens.auth.ModernLoginScreen as LoginScreen
import com.example.fairr.ui.screens.auth.ModernSignUpScreen as SignUpScreen
import com.example.fairr.ui.screens.onboarding.OnboardingScreen
import com.example.fairr.ui.screens.settings.SettingsScreen
import com.example.fairr.ui.screens.MainScreen
import com.example.fairr.ui.screens.groups.CreateGroupScreen
import com.example.fairr.ui.screens.groups.JoinGroupScreen
import com.example.fairr.ui.screens.settlements.SettlementsOverviewScreen as SettlementsScreen
import com.example.fairr.ui.screens.expenses.RecurringExpenseManagementScreen
import com.example.fairr.ui.screens.expenses.RecurringExpenseAnalyticsScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FairrNavGraph(
    navController: NavHostController,
    startupViewModel: StartupViewModel,
    onAppReset: () -> Unit
) {
    val startupState = startupViewModel.startupState.value

    NavHost(
        navController = navController,
        startDestination = when (startupState) {
            StartupState.Welcome -> Screen.Welcome.route
            StartupState.Login -> Screen.Login.route
            StartupState.Onboarding -> Screen.Onboarding.route
            StartupState.Authentication -> Screen.Welcome.route
            else -> Screen.Main.route
        }
    ) {
        composable(Screen.Welcome.route) {
            WelcomeScreen(
                onNavigateToHome = { navController.navigate(Screen.Login.route) },
                onNavigateToAuth = { navController.navigate(Screen.SignUp.route) }
            )
        }

        composable(Screen.Login.route) {
            LoginScreen(
                navController = navController,
                onLoginSuccess = { navController.navigate(Screen.Main.route) },
                onNavigateToRegister = { navController.navigate(Screen.SignUp.route) },
                onNavigateToForgotPassword = { navController.navigate(Screen.ForgotPassword.route) }
            )
        }

        composable(Screen.SignUp.route) {
            SignUpScreen(
                navController = navController,
                onNavigateBack = { navController.popBackStack() },
                onSignUpSuccess = { navController.navigate(Screen.Main.route) },
                onNavigateToLogin = { navController.navigate(Screen.Login.route) }
            )
        }

        composable(Screen.Main.route) {
            MainScreen(
                navController = navController,
                onNavigateToAddExpense = { groupId -> navController.navigate(Screen.AddExpense.createRoute(groupId)) },
                onNavigateToCreateGroup = { navController.navigate(Screen.CreateGroup.route) },
                onNavigateToJoinGroup = { navController.navigate(Screen.JoinGroup.route) },
                onNavigateToSearch = { navController.navigate(Screen.Search.route) },
                onNavigateToNotifications = { navController.navigate(Screen.Notifications.route) },
                onNavigateToGroupDetail = { groupId -> navController.navigate(Screen.GroupDetail.createRoute(groupId)) },
                onNavigateToSettlements = { navController.navigate(Screen.Settlements.route) },
                onNavigateToFriends = { navController.navigate(Screen.Friends.route) },
                onSignOut = onAppReset
            )
        }

        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onGetStarted = { navController.navigate(Screen.Welcome.route) }
            )
        }

        composable(Screen.CreateGroup.route) {
            CreateGroupScreen(
                navController = navController
            )
        }

        composable(Screen.JoinGroup.route) {
            JoinGroupScreen(
                navController = navController
            )
        }

        composable(
            route = Screen.ExpenseDetail.route,
            arguments = listOf(navArgument("expenseId") { type = NavType.StringType })
        ) { backStackEntry ->
            val expenseId = backStackEntry.arguments?.getString("expenseId") ?: return@composable
            ExpenseDetailScreen(
                navController = navController,
                expenseId = expenseId
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                navController = navController,
                onSignOut = onAppReset
            )
        }

        composable(Screen.Settlements.route) {
            SettlementsScreen(
                navController = navController
            )
        }

        composable(Screen.Search.route) {
            SearchScreen(
                navController = navController
            )
        }

        composable(Screen.Notifications.route) {
            NotificationsScreen(
                navController = navController
            )
        }

        composable(
            route = Screen.GroupDetail.route,
            arguments = listOf(navArgument("groupId") { type = NavType.StringType })
        ) { backStackEntry ->
            val groupId = backStackEntry.arguments?.getString("groupId") ?: return@composable
            GroupDetailScreen(
                groupId = groupId,
                navController = navController,
                onNavigateToAddExpense = { navController.navigate(Screen.AddExpense.createRoute(groupId)) }
            )
        }

        composable(
            route = Screen.GroupSettings.route,
            arguments = listOf(navArgument("groupId") { type = NavType.StringType })
        ) { backStackEntry ->
            val groupId = backStackEntry.arguments?.getString("groupId") ?: return@composable
            GroupSettingsScreen(
                navController = navController,
                groupId = groupId
            )
        }

        composable(
            route = Screen.Settlement.route,
            arguments = listOf(navArgument("settlementId") { type = NavType.StringType })
        ) { backStackEntry ->
            val settlementId = backStackEntry.arguments?.getString("settlementId") ?: return@composable
            SettlementScreen(
                navController = navController,
                groupId = settlementId, // Using settlementId as groupId for now
                onSettlementComplete = { navController.popBackStack() }
            )
        }

        composable(Screen.EditProfile.route) {
            EditProfileScreen(
                navController = navController
            )
        }

        composable(Screen.CurrencySelection.route) {
            CurrencySelectionScreen(
                navController = navController
            )
        }

        composable(Screen.CategoryManagement.route) {
            CategoryManagementScreen(
                navController = navController
            )
        }

        composable(Screen.ExportData.route) {
            ExportDataScreen(
                navController = navController
            )
        }

        composable(Screen.HelpSupport.route) {
            HelpSupportScreen(
                navController = navController
            )
        }

        composable(Screen.ContactSupport.route) {
            ContactSupportScreen(
                navController = navController
            )
        }

        composable(Screen.PrivacyPolicy.route) {
            PrivacyPolicyScreen(
                navController = navController
            )
        }

        composable(Screen.Friends.route) {
            FriendsScreen(
                navController = navController,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.AddExpense.route,
            arguments = listOf(navArgument("groupId") { type = NavType.StringType })
        ) { backStackEntry ->
            val groupId = backStackEntry.arguments?.getString("groupId") ?: return@composable
            AddExpenseScreen(
                groupId = groupId,
                navController = navController
            )
        }

        composable(
            route = Screen.RecurringExpenseManagement.route,
            arguments = listOf(navArgument("groupId") { type = NavType.StringType })
        ) { backStackEntry ->
            val groupId = backStackEntry.arguments?.getString("groupId") ?: return@composable
            RecurringExpenseManagementScreen(
                groupId = groupId,
                navController = navController
            )
        }

        composable(
            route = Screen.RecurringExpenseAnalytics.route,
            arguments = listOf(navArgument("groupId") { type = NavType.StringType })
        ) { backStackEntry ->
            val groupId = backStackEntry.arguments?.getString("groupId") ?: return@composable
            RecurringExpenseAnalyticsScreen(
                viewModel = hiltViewModel(),
                groupId = groupId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}

/**
 * Navigation helper to handle authentication redirects
 */
fun NavHostController.handleAuthRedirect(
    isAuthenticated: Boolean,
    startupState: StartupState
) {
    when {
        !isAuthenticated && startupState == StartupState.Authentication -> {
            // User is not authenticated, redirect to login
            navigate(Screen.Login.route) {
                popUpTo(Screen.Splash.route) { 
                    this.inclusive = true 
                }
            }
        }
        isAuthenticated && startupState == StartupState.Main -> {
            // User is authenticated, redirect to main app
            navigate(Screen.Main.route) {
                popUpTo(Screen.Splash.route) { 
                    this.inclusive = true 
                }
            }
        }
        startupState == StartupState.Onboarding -> {
            // User needs to complete onboarding
            navigate(Screen.Onboarding.route) {
                popUpTo(Screen.Splash.route) { 
                    this.inclusive = true 
                }
            }
        }
    }
}

/**
 * Standardized navigation helper for handling successful operations
 * Ensures consistent navigation patterns across the app
 */
fun NavHostController.navigateWithResult(
    destinationRoute: String,
    popUpToRoute: String? = null,
    inclusive: Boolean = false
) {
    navigate(destinationRoute) {
        popUpToRoute?.let { route ->
            popUpTo(route) { this.inclusive = inclusive }
        }
    }
}

/**
 * Helper for navigating back with proper stack management
 */
fun NavHostController.navigateBackSafely(): Boolean {
    return if (previousBackStackEntry != null) {
        popBackStack()
    } else {
        // If no previous entry, navigate to main screen
        navigate(Screen.Main.route) {
            popUpTo(0) { inclusive = true }
        }
        true
    }
}

/**
 * Helper for deep linking to group screens with proper back stack
 */
fun NavHostController.navigateToGroup(
    groupId: String,
    clearBackStack: Boolean = false
) {
    navigate(Screen.GroupDetail.createRoute(groupId)) {
        if (clearBackStack) {
            popUpTo(Screen.Main.route) { inclusive = false }
        }
    }
}

/**
 * Helper for navigating to expense screens with proper context
 */
fun NavHostController.navigateToExpense(
    expenseId: String,
    fromGroup: String? = null
) {
    navigate(Screen.ExpenseDetail.createRoute(expenseId))
}

/**
 * Helper for settings navigation patterns
 */
fun NavHostController.navigateToSettings(
    fromScreen: String? = null
) {
    navigate(Screen.Settings.route)
} 