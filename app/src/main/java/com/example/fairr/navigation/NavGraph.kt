package com.example.fairr.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.fairr.ui.screens.SplashScreen
import com.example.fairr.ui.screens.analytics.AnalyticsScreen
import com.example.fairr.ui.screens.auth.RegisterScreen
import com.example.fairr.ui.screens.auth.WelcomeScreen
import com.example.fairr.ui.screens.auth.MobileLoginScreen
import com.example.fairr.ui.screens.auth.MobileSignUpScreen
import com.example.fairr.ui.screens.auth.ForgotPasswordScreen
import com.example.fairr.ui.screens.budget.BudgetManagementScreen
import com.example.fairr.ui.screens.categories.CategoryManagementScreen
import com.example.fairr.ui.screens.expenses.AddExpenseScreen
import com.example.fairr.ui.screens.expenses.EditExpenseScreen
import com.example.fairr.ui.screens.expenses.ExpenseDetailScreen
import com.example.fairr.ui.screens.export.ExportDataScreen
import com.example.fairr.ui.screens.groups.CreateGroupScreen
import com.example.fairr.ui.screens.groups.GroupDetailScreen
import com.example.fairr.ui.screens.groups.GroupSettingsScreen
import com.example.fairr.ui.screens.groups.JoinGroupScreen
import com.example.fairr.ui.screens.home.HomeScreen
import com.example.fairr.ui.screens.notifications.NotificationsScreen
import com.example.fairr.ui.screens.onboarding.OnboardingScreen
import com.example.fairr.ui.screens.profile.EditProfileScreen
import com.example.fairr.ui.screens.search.SearchScreen
import com.example.fairr.ui.screens.settings.SettingsScreen
import com.example.fairr.ui.screens.settlements.SettlementScreen
import com.example.fairr.ui.screens.support.HelpSupportScreen
import com.example.fairr.ui.screens.camera.PhotoCaptureScreen
import com.example.fairr.ui.screens.MainScreen
import com.example.fairr.ui.screens.SettingsScreenWrapper
import com.example.fairr.ui.screens.AnalyticsScreenWrapper

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Onboarding : Screen("onboarding")
    object Welcome : Screen("welcome")
    object MobileLogin : Screen("mobile_login")
    object ForgotPassword : Screen("forgot_password")
    object Register : Screen("register")
    object MobileSignUp : Screen("mobile_signup")
    object Home : Screen("home")
    object Settings : Screen("settings")
    object EditProfile : Screen("edit_profile")
    object Categories : Screen("categories")
    object Budgets : Screen("budgets")
    object Search : Screen("search")
    object Analytics : Screen("analytics")
    object PhotoCapture : Screen("photo_capture")
    object Settlement : Screen("settlement/{groupId}") {
        fun createRoute(groupId: String) = "settlement/$groupId"
    }
    object ExportData : Screen("export_data/{groupId?}") {
        fun createRoute(groupId: String? = null) = if (groupId != null) "export_data/$groupId" else "export_data/null"
    }
    object HelpSupport : Screen("help_support")
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
    object EditExpense : Screen("edit_expense/{expenseId}") {
        fun createRoute(expenseId: String) = "edit_expense/$expenseId"
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
                    navController.navigate(Screen.Welcome.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.Welcome.route) {
            WelcomeScreen(
                navController = navController,
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
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Welcome.route) { inclusive = true }
                    }
                },
                onNavigateToSignUp = {
                    navController.navigate(Screen.MobileSignUp.route) {
                        popUpTo(Screen.MobileLogin.route) { inclusive = true }
                    }
                },
                onNavigateToForgotPassword = {
                    navController.navigate(Screen.ForgotPassword.route)
                }
            )
        }
        
        composable(Screen.ForgotPassword.route) {
            ForgotPasswordScreen(
                navController = navController,
                onNavigateBack = {
                    navController.popBackStack()
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
                    navController.navigate(Screen.Home.route) {
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
            MainScreen(
                navController = navController,
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
                onSignOut = {
                    // Handle sign out
                    navController.navigate(Screen.Welcome.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.Settings.route) {
            SettingsScreenWrapper(
                navController = navController,
                onSignOut = {
                    // Handle sign out
                    navController.navigate(Screen.Welcome.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.EditProfile.route) {
            EditProfileScreen(
                navController = navController,
                onSaveProfile = {
                    // Navigate back after saving
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.Categories.route) {
            CategoryManagementScreen(
                navController = navController,
                onSaveCategories = { _ ->
                    // Save categories to your data store/repository
                    // Navigate back after saving
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.Budgets.route) {
            BudgetManagementScreen(
                navController = navController,
                onSaveBudgets = { _ ->
                    // Save budgets to your data store/repository
                    // Navigate back after saving
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.Search.route) {
            SearchScreen(
                navController = navController,
                onNavigateToExpense = { expenseId ->
                    navController.navigate(Screen.ExpenseDetail.createRoute(expenseId))
                },
                onNavigateToGroup = { groupId ->
                    navController.navigate(Screen.GroupDetail.createRoute(groupId))
                }
            )
        }
        
        composable(Screen.Analytics.route) {
            AnalyticsScreenWrapper(
                navController = navController
            )
        }
        
        composable(Screen.PhotoCapture.route) {
            PhotoCaptureScreen(
                navController = navController,
                onPhotosSelected = { photos ->
                    // Handle selected photos - in real implementation would pass back to expense screen
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.Settlement.route) { backStackEntry ->
            val groupId = backStackEntry.arguments?.getString("groupId") ?: ""
            SettlementScreen(
                navController = navController,
                groupId = groupId,
                onSettlementComplete = {
                    // Navigate back to group detail after settlement
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.ExportData.route) { backStackEntry ->
            val groupId = backStackEntry.arguments?.getString("groupId")?.takeIf { it != "null" }
            ExportDataScreen(
                navController = navController,
                groupId = groupId
            )
        }
        
        composable(Screen.HelpSupport.route) {
            HelpSupportScreen(
                navController = navController
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
        
        composable(Screen.EditExpense.route) { backStackEntry ->
            val expenseId = backStackEntry.arguments?.getString("expenseId") ?: ""
            EditExpenseScreen(
                navController = navController,
                expenseId = expenseId,
                onExpenseUpdated = {
                    // Navigate back after updating expense
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
                    navController.navigate(Screen.EditExpense.createRoute(expenseId))
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
                onNotificationClick = { _ ->
                    // Handle notification click based on type
                    // For demo purposes, navigate to group detail for expense notifications
                    // In real app, this would check notification type and navigate accordingly
                    navController.navigate(Screen.GroupDetail.createRoute("1"))
                }
            )
        }
    }
}
