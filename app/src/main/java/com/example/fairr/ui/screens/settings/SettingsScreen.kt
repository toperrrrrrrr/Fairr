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
                            tint = TextSecondary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PureWhite
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Profile Section
            item {
                ProfileCard(user = user, navController = navController)
            }
            
            item {
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Account Section
            item {
                SectionHeader("Account & Security")
            }
            
            item {
                SettingsCard {
                    Column {
                        SettingsItem(
                            icon = Icons.Default.Lock,
                            title = "Change Password",
                            subtitle = "Update your account password",
                            onClick = { /* Navigate to change password */ }
                        )
                        HorizontalDivider(modifier = Modifier.padding(start = 56.dp))
                        SettingsItem(
                            icon = Icons.Default.Security,
                            title = "Two-Factor Authentication",
                            subtitle = "Add extra security to your account",
                            onClick = { /* Navigate to 2FA settings */ }
                        )
                        HorizontalDivider(modifier = Modifier.padding(start = 56.dp))
                        SettingsItem(
                            icon = Icons.Default.Download,
                            title = "Download My Data",
                            subtitle = "Export your account data",
                            onClick = { /* Navigate to data export */ }
                        )
                        HorizontalDivider(modifier = Modifier.padding(start = 56.dp))
                        SettingsItem(
                            icon = Icons.Default.Delete,
                            title = "Delete Account",
                            subtitle = "Permanently delete your account",
                            titleColor = ErrorRed,
                            iconTint = ErrorRed,
                            onClick = { /* Show delete account dialog */ }
                        )
                    }
                }
            }

            // Preferences Section
            item {
                SectionHeader("Preferences")
            }
            
            item {
                SettingsCard {
                    Column {
                        SettingsSwitchItem(
                            icon = Icons.Default.DarkMode,
                            title = "Dark Mode",
                            subtitle = "Use dark theme",
                            checked = themeManager.isDarkTheme(),
                            onCheckedChange = { themeManager.toggleDarkMode() }
                        )
                        HorizontalDivider(modifier = Modifier.padding(start = 56.dp))
                        SettingsSwitchItem(
                            icon = Icons.Default.Notifications,
                            title = "Notifications",
                            subtitle = "Enable push notifications",
                            checked = notificationsEnabled,
                            onCheckedChange = { notificationsEnabled = it }
                        )
                        HorizontalDivider(modifier = Modifier.padding(start = 56.dp))
                        SettingsSwitchItem(
                            icon = Icons.AutoMirrored.Filled.VolumeUp,
                            title = "Sound",
                            subtitle = "Enable notification sounds",
                            checked = soundEnabled,
                            onCheckedChange = { soundEnabled = it }
                        )
                    }
                }
            }

            // App Management Section
            item {
                SectionHeader("App Management")
            }
            
            item {
                SettingsCard {
                    Column {
                        SettingsItem(
                            icon = Icons.Default.Category,
                            title = "Manage Categories",
                            subtitle = "Customize expense categories",
                            onClick = { 
                                navController.navigate("categories")
                            }
                        )
                        HorizontalDivider(modifier = Modifier.padding(start = 56.dp))
                        SettingsItem(
                            icon = Icons.Default.Archive,
                            title = "Backup & Restore",
                            subtitle = "Backup your data to cloud",
                            onClick = { /* Navigate to backup settings */ }
                        )
                    }
                }
            }

            // About Section
            item {
                SectionHeader("Support & Legal")
            }
            
            item {
                SettingsCard {
                    Column {
                        SettingsItem(
                            icon = Icons.AutoMirrored.Filled.Help,
                            title = "Help Center",
                            subtitle = "Get help and find answers",
                            onClick = { 
                                navController.navigate("help_support")
                            }
                        )
                        HorizontalDivider(modifier = Modifier.padding(start = 56.dp))
                        SettingsItem(
                            icon = Icons.Default.Email,
                            title = "Contact Support",
                            subtitle = "Send us a message",
                            onClick = { /* Open contact support */ }
                        )
                        HorizontalDivider(modifier = Modifier.padding(start = 56.dp))
                        SettingsItem(
                            icon = Icons.Default.Info,
                            title = "About FairShare",
                            subtitle = "Version 1.0.0 • Learn more",
                            onClick = { /* Show about dialog */ }
                        )
                        HorizontalDivider(modifier = Modifier.padding(start = 56.dp))
                        SettingsItem(
                            icon = Icons.Default.Gavel,
                            title = "Terms & Privacy Policy",
                            subtitle = "Read our terms and privacy policy",
                            onClick = { /* Navigate to terms */ }
                        )
                    }
                }
            }

            // Sign Out
            item {
                Spacer(modifier = Modifier.height(16.dp))
                
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(2.dp, RoundedCornerShape(12.dp)),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = PureWhite)
                ) {
                    SettingsItem(
                        icon = Icons.AutoMirrored.Filled.Logout,
                        title = "Sign Out",
                        subtitle = "Sign out of your account",
                        titleColor = ErrorRed,
                        iconTint = ErrorRed,
                        onClick = onSignOut
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        color = TextSecondary,
        modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
    )
}

@Composable
private fun SettingsCard(
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = PureWhite)
    ) {
        content()
    }
}

@Composable
private fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    titleColor: Color = TextPrimary,
    iconTint: Color = TextSecondary,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = titleColor
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        }
        
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = TextSecondary,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun SettingsSwitchItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = TextSecondary,
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        }
        
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = PureWhite,
                checkedTrackColor = DarkGreen,
                uncheckedThumbColor = PureWhite,
                uncheckedTrackColor = TextSecondary
            )
        )
    }
}

@Composable
private fun ProfileCard(
    user: UserProfile,
    navController: NavController
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = PureWhite)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Profile Avatar
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .background(
                            color = DarkGreen.copy(alpha = 0.1f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = user.name.split(" ").map { it.first() }.joinToString(""),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkGreen
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
                        color = TextSecondary
                    )
                }
                
                // Edit Button
                OutlinedButton(
                    onClick = { 
                        navController.navigate("edit_profile")
                    },
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = DarkGreen
                    ),
                    border = BorderStroke(1.dp, DarkGreen),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Edit",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Edit", fontSize = 14.sp)
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Stats Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatCard(
                    icon = Icons.Default.Group,
                    value = user.totalGroups.toString(),
                    label = "Groups",
                    iconColor = DarkGreen
                )
                StatCard(
                    icon = Icons.Default.Receipt,
                    value = "$${String.format("%.0f", user.totalExpenses)}",
                    label = "Total Expenses",
                    iconColor = DarkBlue
                )
            }
        }
    }
}

@Composable
private fun StatCard(
    icon: ImageVector,
    value: String,
    label: String,
    iconColor: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = iconColor,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = TextSecondary
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