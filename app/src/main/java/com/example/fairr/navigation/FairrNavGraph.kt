package com.example.fairr.navigation

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
import androidx.navigation.NavHostController

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Onboarding : Screen("onboarding")
    object Welcome : Screen("welcome")
    object Login : Screen("login")
    object SignUp : Screen("signup")
    object Main : Screen("main?tab={tab}") {
        fun createRoute(tab: Int = 0) = "main?tab=$tab"
    }
    object CreateGroup : Screen("create_group")
    object JoinGroup : Screen("join_group")
    object Search : Screen("search")
    object Notifications : Screen("notifications")
    object GroupDetail : Screen("group_detail/{groupId}") {
        fun createRoute(groupId: String) = "group_detail/$groupId"
    }
    object Settings : Screen("settings")
    object EditProfile : Screen("edit_profile")
    object HelpSupport : Screen("help_support")
    object UserProfile : Screen("user_profile")
    object CategoryManagement : Screen("category_management")
    object ExportData : Screen("export_data")
    object GroupSettings : Screen("group_settings/{groupId}") {
        fun createRoute(groupId: String) = "group_settings/$groupId"
    }
    object GroupActivity : Screen("group_activity/{groupId}") {
        fun createRoute(groupId: String) = "group_activity/$groupId"
    }
    object Settlement : Screen("settlement/{groupId}") {
        fun createRoute(groupId: String) = "settlement/$groupId"
    }
    object SettlementsOverview : Screen("settlements_overview")
    object ExpenseDetail : Screen("expense_detail/{expenseId}") {
        fun createRoute(expenseId: String) = "expense_detail/$expenseId"
    }
    object EditExpense : Screen("edit_expense/{expenseId}") {
        fun createRoute(expenseId: String) = "edit_expense/$expenseId"
    }
    object CurrencySelection : Screen("currency_selection")
    object AddExpense : Screen("add_expense/{groupId}") {
        fun createRoute(groupId: String) = "add_expense/$groupId"
    }
    object Friends : Screen("friends")
    object PrivacyPolicy : Screen("privacy_policy")
    object ContactSupport : Screen("contact_support")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FairrNavGraph(
    navController: NavHostController,
    startupViewModel: StartupViewModel,
    onAppReset: () -> Unit = {}
) {
    val hasCompletedOnboarding by startupViewModel.isOnboardingCompleted.collectAsState()

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(Screen.Splash.route) {
            // Splash screen is now handled in MainActivity
            // This route is kept for compatibility but not used
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
            val authViewModel: AuthViewModel = hiltViewModel()
            WelcomeScreen(
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route)
                },
                onNavigateToSignUp = {
                    navController.navigate(Screen.SignUp.route)
                },
                viewModel = authViewModel
            )
        }

        composable(Screen.Login.route) {
            val authViewModel: AuthViewModel = hiltViewModel()
            ModernLoginScreen(
                navController = navController,
                onLoginSuccess = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Welcome.route) { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(Screen.SignUp.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                viewModel = authViewModel
            )
        }

        composable(Screen.SignUp.route) {
            val authViewModel: AuthViewModel = hiltViewModel()
            ModernSignUpScreen(
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
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.SignUp.route) { inclusive = true }
                    }
                },
                viewModel = authViewModel
            )
        }

        composable(
            route = Screen.Main.route,
            arguments = listOf(
                navArgument("tab") {
                    type = NavType.IntType
                    defaultValue = 0
                }
            )
        ) { backStackEntry ->
            val tab = backStackEntry.arguments?.getInt("tab") ?: 0
            MainScreen(
                navController = navController,
                onNavigateToAddExpense = { groupId: String ->
                    navController.navigate(Screen.AddExpense.createRoute(groupId))
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
                onNavigateToGroupDetail = { groupId: String ->
                    navController.navigate(Screen.GroupDetail.createRoute(groupId))
                },
                onNavigateToSettlements = {
                    navController.navigate(Screen.SettlementsOverview.route)
                },
                onNavigateToFriends = {
                    navController.navigate(Screen.Friends.route)
                },
                onSignOut = {
                    onAppReset()
                }
            )
        }

        composable(Screen.CreateGroup.route) {
            CreateGroupScreen(
                navController = navController
            )
        }

        composable(Screen.JoinGroup.route) {
            JoinGroupScreen(
                navController = navController,
                onJoinSuccess = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Screen.GroupDetail.route,
            arguments = listOf(
                navArgument("groupId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val groupId = backStackEntry.arguments?.getString("groupId")
                ?: return@composable
            GroupDetailScreen(
                groupId = groupId,
                navController = navController,
                onNavigateToAddExpense = {
                    navController.navigate(Screen.AddExpense.createRoute(groupId))
                }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                navController = navController,
                onSignOut = {
                    onAppReset()
                }
            )
        }

        composable(Screen.CurrencySelection.route) {
            CurrencySelectionScreen(
                navController = navController
            )
        }

        composable(Screen.Friends.route) {
            FriendsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // AddExpense screen implementation
        composable(
            route = Screen.AddExpense.route,
            arguments = listOf(
                navArgument("groupId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val groupId = backStackEntry.arguments?.getString("groupId")
                ?: return@composable
            AddExpenseScreen(
                groupId = groupId,
                navController = navController,
                onExpenseAdded = {
                    navController.popBackStack()
                }
            )
        }

        // Edit Profile screen
        composable(Screen.EditProfile.route) {
            EditProfileScreen(
                navController = navController
            )
        }

        // Help & Support screen
        composable(Screen.HelpSupport.route) {
            HelpSupportScreen(
                navController = navController
            )
        }

        // User Profile screen (Advanced profile)
        composable(Screen.UserProfile.route) {
            UserProfileScreen(
                navController = navController
            )
        }

        // Category Management screen
        composable(Screen.CategoryManagement.route) {
            CategoryManagementScreen(
                navController = navController
            )
        }

        // Export Data screen
        composable(Screen.ExportData.route) {
            ExportDataScreen(
                navController = navController
            )
        }

        // Group Settings screen
        composable(
            route = Screen.GroupSettings.route,
            arguments = listOf(
                navArgument("groupId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val groupId = backStackEntry.arguments?.getString("groupId")
                ?: return@composable
            GroupSettingsScreen(
                groupId = groupId,
                navController = navController
            )
        }

        // Group Activity screen
        composable(
            route = Screen.GroupActivity.route,
            arguments = listOf(
                navArgument("groupId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val groupId = backStackEntry.arguments?.getString("groupId")
                ?: return@composable
            GroupActivityScreen(
                groupId = groupId,
                navController = navController
            )
        }

        // Settlement screen
        composable(
            route = Screen.Settlement.route,
            arguments = listOf(
                navArgument("groupId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val groupId = backStackEntry.arguments?.getString("groupId")
                ?: return@composable
            SettlementScreen(
                groupId = groupId,
                navController = navController
            )
        }

        // Expense Detail screen
        composable(
            route = Screen.ExpenseDetail.route,
            arguments = listOf(
                navArgument("expenseId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val expenseId = backStackEntry.arguments?.getString("expenseId")
                ?: return@composable
            ExpenseDetailScreen(
                expenseId = expenseId,
                navController = navController,
                onEditExpense = {
                    navController.navigate(Screen.EditExpense.createRoute(expenseId))
                }
            )
        }

        // Edit Expense screen
        composable(
            route = Screen.EditExpense.route,
            arguments = listOf(
                navArgument("expenseId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val expenseId = backStackEntry.arguments?.getString("expenseId")
                ?: return@composable
            EditExpenseScreen(
                expenseId = expenseId,
                navController = navController
            )
        }

        // Search screen
        composable(Screen.Search.route) {
            SearchScreen(navController = navController)
        }

        // Notifications screen
        composable(Screen.Notifications.route) {
            NotificationsScreen(navController = navController)
        }

        // Privacy Policy screen
        composable(Screen.PrivacyPolicy.route) {
            PrivacyPolicyScreen(navController = navController)
        }

        // Contact Support screen
        composable(Screen.ContactSupport.route) {
            ContactSupportScreen(navController = navController)
        }

        // Settlements Overview screen
        composable(Screen.SettlementsOverview.route) {
            SettlementsOverviewScreen(
                navController = navController
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
                popUpTo(Screen.Splash.route) { inclusive = true }
            }
        }
        isAuthenticated && startupState == StartupState.Main -> {
            // User is authenticated, redirect to main app
            navigate(Screen.Main.route) {
                popUpTo(Screen.Splash.route) { inclusive = true }
            }
        }
        startupState == StartupState.Onboarding -> {
            // User needs to complete onboarding
            navigate(Screen.Onboarding.route) {
                popUpTo(Screen.Splash.route) { inclusive = true }
            }
        }
    }
} 