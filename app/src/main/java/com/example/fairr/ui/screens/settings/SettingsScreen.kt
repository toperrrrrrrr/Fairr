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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    onSignOut: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    val settingsTabs = listOf("Profile", "Preferences", "Support")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when (selectedTab) {
                            0 -> "Profile"
                            1 -> "Preferences"
                            2 -> "Support"
                            else -> "Settings"
                        },
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BackgroundPrimary
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = PureWhite,
                tonalElevation = 8.dp
            ) {
                // Home Tab
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Home, contentDescription = "Home") },
                    label = { Text("Home") },
                    selected = false,
                    onClick = { navController.navigate("home") },
                    colors = NavigationBarItemDefaults.colors(
                        unselectedIconColor = IconTint,
                        unselectedTextColor = IconTint
                    )
                )
                
                // Groups Tab
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Group, contentDescription = "Groups") },
                    label = { Text("Groups") },
                    selected = false,
                    onClick = { navController.navigate("groups") },
                    colors = NavigationBarItemDefaults.colors(
                        unselectedIconColor = IconTint,
                        unselectedTextColor = IconTint
                    )
                )
                
                // Settings Tab
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Settings, contentDescription = "Settings") },
                    label = { Text("Settings") },
                    selected = true,
                    onClick = { },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Primary,
                        selectedTextColor = Primary,
                        unselectedIconColor = IconTint,
                        unselectedTextColor = IconTint
                    )
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundSecondary)
                .padding(padding)
        ) {
            // Settings Tabs
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = PureWhite,
                contentColor = Primary,
                indicator = { tabPositions ->
                    TabRowDefaults.Indicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = Primary
                    )
                }
            ) {
                settingsTabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { 
                            Text(
                                text = title,
                                fontSize = 14.sp,
                                fontWeight = if (selectedTab == index) FontWeight.Medium else FontWeight.Normal
                            )
                        },
                        selectedContentColor = Primary,
                        unselectedContentColor = TextSecondary
                    )
                }
            }

            // Tab Content
            when (selectedTab) {
                0 -> ProfileTabContent(
                    modifier = Modifier.padding(16.dp)
                )
                1 -> PreferencesTabContent(
                    modifier = Modifier.padding(16.dp)
                )
                2 -> SupportTabContent(
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}

@Composable
private fun ProfileTabContent(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
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
                    ModernButton(
                        text = "Edit Profile",
                        onClick = { },
                        backgroundColor = Primary.copy(alpha = 0.1f),
                        textColor = Primary
                    )
                }
            }
        }

        ModernCard {
            Column {
                SettingsItem(
                    title = "Personal Information",
                    icon = Icons.Default.Person,
                    onClick = { }
                )
                SettingsItem(
                    title = "Payment Methods",
                    icon = Icons.Default.Payment,
                    onClick = { }
                )
                SettingsItem(
                    title = "Account Security",
                    icon = Icons.Default.Security,
                    onClick = { }
                )
            }
        }
    }
}

@Composable
private fun PreferencesTabContent(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ModernCard {
            Column {
                SettingsItem(
                    title = "Notifications",
                    icon = Icons.Default.Notifications,
                    onClick = { }
                )
                SettingsItem(
                    title = "Currency",
                    icon = Icons.Default.AttachMoney,
                    onClick = { }
                )
                SettingsItem(
                    title = "Language",
                    icon = Icons.Default.Language,
                    onClick = { }
                )
                SettingsItem(
                    title = "Theme",
                    icon = Icons.Default.Palette,
                    onClick = { }
                )
            }
        }
    }
}

@Composable
private fun SupportTabContent(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ModernCard {
            Column {
                SettingsItem(
                    title = "Help Center",
                    icon = Icons.Default.Help,
                    onClick = { }
                )
                SettingsItem(
                    title = "Contact Support",
                    icon = Icons.Default.SupportAgent,
                    onClick = { }
                )
                SettingsItem(
                    title = "Privacy Policy",
                    icon = Icons.Default.Policy,
                    onClick = { }
                )
                SettingsItem(
                    title = "Terms of Service",
                    icon = Icons.Default.Description,
                    onClick = { }
                )
                SettingsItem(
                    title = "About",
                    icon = Icons.Default.Info,
                    onClick = { }
                )
                SettingsItem(
                    title = "Sign Out",
                    icon = Icons.Default.ExitToApp,
                    onClick = { },
                    textColor = ErrorRed,
                    iconTint = ErrorRed
                )
            }
        }
    }
}

@Composable
private fun SettingsItem(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit,
    textColor: Color = TextPrimary,
    iconTint: Color = Primary
) {
    Surface(
        onClick = onClick,
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = title,
                fontSize = 16.sp,
                color = textColor
            )
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
            navController = rememberNavController(),
            onSignOut = {}
        )
    }
} 