package com.example.fairr.ui.screens.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.fairr.ui.components.*
import com.example.fairr.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    onSignOut: () -> Unit = {}
) {
    val themeManager = LocalThemeManager.current
    var notificationsEnabled by remember { mutableStateOf(true) }
    var soundEnabled by remember { mutableStateOf(true) }

    // Sample user data
    val user = remember {
        UserProfile(
            name = "John Doe",
            email = "john.doe@example.com",
            totalGroups = 5,
            totalExpenses = 1250.75
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Settings",
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack, 
                            contentDescription = "Back",
                            tint = IconTint
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BackgroundPrimary
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundSecondary)
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Profile Section
            item {
                ModernProfileCard(user = user, navController = navController)
            }

            // Account Section
            item {
                Text(
                    text = "Account & Security",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                ModernCard {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(0.dp)
                    ) {
                        ModernListItem(
                            title = "Change Password",
                            subtitle = "Update your account password",
                            leadingIcon = Icons.Default.Lock,
                            onClick = { /* Navigate to change password */ }
                        )
                        HorizontalDivider(color = DividerColor)
                        ModernListItem(
                            title = "Two-Factor Authentication",
                            subtitle = "Add extra security to your account",
                            leadingIcon = Icons.Default.Security,
                            onClick = { /* Navigate to 2FA settings */ }
                        )
                        HorizontalDivider(color = DividerColor)
                        ModernListItem(
                            title = "Download My Data",
                            subtitle = "Export your account data",
                            leadingIcon = Icons.Default.Download,
                            onClick = { /* Navigate to data export */ }
                        )
                        HorizontalDivider(color = DividerColor)
                        ModernListItem(
                            title = "Delete Account",
                            subtitle = "Permanently delete your account",
                            leadingIcon = Icons.Default.Delete,
                            onClick = { /* Show delete account dialog */ }
                        )
                    }
                }
            }

            // Preferences Section
            item {
                Text(
                    text = "Preferences",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                ModernCard {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(0.dp)
                    ) {
                        ModernSwitchItem(
                            title = "Dark Mode",
                            subtitle = "Use dark theme",
                            icon = Icons.Default.DarkMode,
                            checked = themeManager.isDarkTheme(),
                            onCheckedChange = { themeManager.toggleDarkMode() }
                        )
                        HorizontalDivider(color = DividerColor)
                        ModernSwitchItem(
                            title = "Notifications",
                            subtitle = "Enable push notifications",
                            icon = Icons.Default.Notifications,
                            checked = notificationsEnabled,
                            onCheckedChange = { notificationsEnabled = it }
                        )
                        HorizontalDivider(color = DividerColor)
                        ModernSwitchItem(
                            title = "Sound",
                            subtitle = "Enable notification sounds",
                            icon = Icons.AutoMirrored.Filled.VolumeUp,
                            checked = soundEnabled,
                            onCheckedChange = { soundEnabled = it }
                        )
                    }
                }
            }

            // App Management Section
            item {
                Text(
                    text = "App Management",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                ModernCard {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(0.dp)
                    ) {
                        ModernListItem(
                            title = "Manage Categories",
                            subtitle = "Customize expense categories",
                            leadingIcon = Icons.Default.Category,
                            onClick = { 
                                navController.navigate("categories")
                            }
                        )
                        HorizontalDivider(color = DividerColor)
                        ModernListItem(
                            title = "Budget Management",
                            subtitle = "Set and track spending budgets",
                            leadingIcon = Icons.Default.AccountBalance,
                            onClick = { 
                                navController.navigate("budgets")
                            }
                        )
                        HorizontalDivider(color = DividerColor)
                        ModernListItem(
                            title = "Backup & Restore",
                            subtitle = "Backup your data to cloud",
                            leadingIcon = Icons.Default.Archive,
                            onClick = { /* Navigate to backup settings */ }
                        )
                    }
                }
            }

            // Support Section
            item {
                Text(
                    text = "Support & About",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                ModernCard {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(0.dp)
                    ) {
                        ModernListItem(
                            title = "Help & Support",
                            subtitle = "Get help and contact support",
                            leadingIcon = Icons.Default.Help,
                            onClick = { 
                                navController.navigate("support")
                            }
                        )
                        HorizontalDivider(color = DividerColor)
                        ModernListItem(
                            title = "Privacy Policy",
                            subtitle = "Read our privacy policy",
                            leadingIcon = Icons.Default.Policy,
                            onClick = { /* Navigate to privacy policy */ }
                        )
                        HorizontalDivider(color = DividerColor)
                        ModernListItem(
                            title = "Terms of Service",
                            subtitle = "Read our terms of service",
                            leadingIcon = Icons.Default.Description,
                            onClick = { /* Navigate to terms */ }
                        )
                        HorizontalDivider(color = DividerColor)
                        ModernListItem(
                            title = "About",
                            subtitle = "Version 1.0.0",
                            leadingIcon = Icons.Default.Info,
                            onClick = { /* Show about dialog */ }
                        )
                    }
                }
            }

            // Sign Out Section
            item {
                ModernButton(
                    text = "Sign Out",
                    onClick = onSignOut,
                    backgroundColor = ErrorRed,
                    textColor = TextOnDark,
                    icon = Icons.AutoMirrored.Filled.ExitToApp,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
fun ModernProfileCard(
    user: UserProfile,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    ModernCard(
        modifier = modifier.clickable { 
            navController.navigate("profile_edit")
        }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Profile Avatar
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(
                        color = Primary.copy(alpha = 0.1f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = user.name.split(" ").map { it.first() }.joinToString(""),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Primary
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // User Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = user.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                
                Text(
                    text = user.email,
                    fontSize = 14.sp,
                    color = TextSecondary,
                    modifier = Modifier.padding(top = 2.dp)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "${user.totalGroups} groups",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                    Text(
                        text = "$${String.format("%.0f", user.totalExpenses)} spent",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            }
            
            // Edit Icon
            Icon(
                Icons.Default.Edit,
                contentDescription = "Edit Profile",
                tint = IconTint,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun ModernSwitchItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 12.dp, horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = IconTint,
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )
            
            Text(
                text = subtitle,
                fontSize = 14.sp,
                color = TextSecondary,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
        
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Primary,
                checkedTrackColor = Primary.copy(alpha = 0.5f),
                uncheckedThumbColor = MediumGray,
                uncheckedTrackColor = LightGray
            )
        )
    }
}

data class UserProfile(
    val name: String,
    val email: String,
    val totalGroups: Int,
    val totalExpenses: Double
)

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    FairrTheme {
        SettingsScreen(
            navController = rememberNavController()
        )
    }
} 