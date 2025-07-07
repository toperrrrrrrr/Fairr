package com.example.fairr.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Welcome : Screen("welcome")
    object Login : Screen("login")
    object Main : Screen("main")
    object Onboarding : Screen("onboarding")
    object Settings : Screen("settings")
    object CreateGroup : Screen("create_group")
    object JoinGroup : Screen("join_group")
    object Search : Screen("search")
    object Notifications : Screen("notifications")
    object Settlements : Screen("settlements")
    object Settlement : Screen("settlement/{settlementId}") {
        fun createRoute(settlementId: String) = "settlement/$settlementId"
    }
    object GroupActivity : Screen("group_activity/{groupId}") {
        fun createRoute(groupId: String) = "group_activity/$groupId"
    }
    object RecurringExpenseManagement : Screen("recurring_expense_management/{groupId}") {
        fun createRoute(groupId: String) = "recurring_expense_management/$groupId"
    }
    object RecurringExpenseAnalytics : Screen("recurring_expense_analytics/{groupId}") {
        fun createRoute(groupId: String) = "recurring_expense_analytics/$groupId"
    }
    object EditProfile : Screen("edit_profile")
    object CurrencySelection : Screen("currency_selection")
    object CategoryManagement : Screen("category_management")
    object ExportData : Screen("export_data")
    object HelpSupport : Screen("help_support")
    object ContactSupport : Screen("contact_support")
    object PrivacyPolicy : Screen("privacy_policy")
    object ForgotPassword : Screen("forgot_password")
    object ExpenseDetail : Screen("expense/{expenseId}") {
        fun createRoute(expenseId: String) = "expense/$expenseId"
    }
    object GroupDetail : Screen("group/{groupId}") {
        fun createRoute(groupId: String) = "group/$groupId"
    }
    object GroupSettings : Screen("group/{groupId}/settings") {
        fun createRoute(groupId: String) = "group/$groupId/settings"
    }
    object Friends : Screen("friends")
    object AddExpense : Screen("add_expense/{groupId}") {
        fun createRoute(groupId: String) = "add_expense/$groupId"
    }
} 