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
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import com.example.fairr.navigation.Screen
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    var notificationsEnabled by remember { mutableStateOf(true) }
    var soundEnabled by remember { mutableStateOf(true) }
    var isDarkMode by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Settings",
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BackgroundPrimary
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundSecondary)
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Profile Section
            item {
                Text(
                    text = "Profile",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                
                ModernCard {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "John Doe",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "john.doe@example.com",
                                    fontSize = 14.sp,
                                    color = TextSecondary
                                )
                            }
                            Button(
                                onClick = { navController.navigate(Screen.EditProfile.route) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Primary.copy(alpha = 0.1f),
                                    contentColor = Primary
                                )
                            ) {
                                Text("Edit Profile")
                            }
                        }
                    }
                }
            }

            // Account Section
            item {
                Text(
                    text = "Account",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                
                ModernCard {
                    Column {
                        ListItem(
                            headlineContent = { Text("Personal Information") },
                            leadingContent = { 
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = null,
                                    tint = Primary
                                )
                            },
                            modifier = Modifier.clickable { 
                                navController.navigate(Screen.UserProfile.route)
                            }
                        )
                        ListItem(
                            headlineContent = { Text("Category Management") },
                            supportingContent = { Text("Manage expense categories") },
                            leadingContent = { 
                                Icon(
                                    Icons.Default.Label,
                                    contentDescription = null,
                                    tint = Primary
                                )
                            },
                            modifier = Modifier.clickable { 
                                navController.navigate(Screen.CategoryManagement.route)
                            }
                        )
                        ListItem(
                            headlineContent = { Text("Export Data") },
                            supportingContent = { Text("Export your expense data") },
                            leadingContent = { 
                                Icon(
                                    Icons.Default.FileDownload,
                                    contentDescription = null,
                                    tint = Primary
                                )
                            },
                            modifier = Modifier.clickable { 
                                navController.navigate(Screen.ExportData.route)
                            }
                        )
                    }
                }
            }

            // App Settings Section
            item {
                Text(
                    text = "App Settings",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                
                ModernCard {
                    Column {
                        ListItem(
                            headlineContent = { Text("Dark Mode") },
                            supportingContent = { Text("Use dark theme") },
                            leadingContent = { 
                                Icon(
                                    Icons.Default.DarkMode,
                                    contentDescription = null,
                                    tint = Primary
                                )
                            },
                            trailingContent = {
                                Switch(
                                    checked = isDarkMode,
                                    onCheckedChange = { isDarkMode = it }
                                )
                            }
                        )
                        ListItem(
                            headlineContent = { Text("Default Currency") },
                            supportingContent = { Text(viewModel.selectedCurrency) },
                            leadingContent = { 
                                Icon(
                                    Icons.Default.CurrencyExchange,
                                    contentDescription = null,
                                    tint = Primary
                                )
                            },
                            modifier = Modifier.clickable {
                                navController.navigate(Screen.CurrencySelection.route)
                            }
                        )
                        ListItem(
                            headlineContent = { Text("Notifications") },
                            supportingContent = { Text("Enable push notifications") },
                            leadingContent = { 
                                Icon(
                                    Icons.Default.Notifications,
                                    contentDescription = null,
                                    tint = Primary
                                )
                            },
                            trailingContent = {
                                Switch(
                                    checked = notificationsEnabled,
                                    onCheckedChange = { notificationsEnabled = it }
                                )
                            }
                        )
                        ListItem(
                            headlineContent = { Text("Sound") },
                            supportingContent = { Text("Enable sound effects") },
                            leadingContent = { 
                                Icon(
                                    Icons.AutoMirrored.Filled.VolumeUp,
                                    contentDescription = "Volume",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            },
                            trailingContent = {
                                Switch(
                                    checked = soundEnabled,
                                    onCheckedChange = { soundEnabled = it }
                                )
                            }
                        )
                    }
                }
            }

            // Support Section
            item {
                Text(
                    text = "Support",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                
                ModernCard {
                    Column {
                        ListItem(
                            headlineContent = { Text("Help Center") },
                            leadingContent = { 
                                Icon(
                                    Icons.AutoMirrored.Filled.Help,
                                    contentDescription = null,
                                    tint = Primary
                                )
                            },
                            modifier = Modifier.clickable { navController.navigate(Screen.HelpSupport.route) }
                        )
                        ListItem(
                            headlineContent = { Text("Contact Support") },
                            leadingContent = { 
                                Icon(
                                    Icons.AutoMirrored.Filled.Chat,
                                    contentDescription = null,
                                    tint = Primary
                                )
                            },
                            modifier = Modifier.clickable { 
                                navController.navigate(Screen.ContactSupport.route)
                            }
                        )
                        ListItem(
                            headlineContent = { Text("Privacy Policy") },
                            leadingContent = { 
                                Icon(
                                    Icons.Default.PrivacyTip,
                                    contentDescription = null,
                                    tint = Primary
                                )
                            },
                            modifier = Modifier.clickable { 
                                navController.navigate(Screen.PrivacyPolicy.route)
                            }
                        )
                    }
                }
            }

            // Sign Out Button
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onSignOut,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ErrorRed,
                        contentColor = PureWhite
                    )
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ExitToApp,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Sign Out")
                }
                Spacer(modifier = Modifier.height(16.dp))
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
                uncheckedThumbColor = NeutralGray,
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
            navController = rememberNavController(),
            onSignOut = {}
        )
    }
} 


