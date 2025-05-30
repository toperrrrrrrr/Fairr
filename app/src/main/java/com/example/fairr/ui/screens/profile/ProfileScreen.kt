package com.example.fairr.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.fairr.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    onSignOut: () -> Unit = {}
) {
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
                        "Profile",
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    ) 
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PureWhite
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(LightBackground)
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            
            // Profile Header Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .shadow(2.dp, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = PureWhite)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Profile Avatar
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .background(
                                DarkGreen.copy(alpha = 0.1f),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = user.name.split(" ").map { it.first() }.joinToString(""),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarkGreen
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = user.name,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    
                    Text(
                        text = user.email,
                        fontSize = 14.sp,
                        color = TextSecondary
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Stats
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatItem(
                            label = "Groups",
                            value = user.totalGroups.toString()
                        )
                        StatItem(
                            label = "Total Expenses",
                            value = "$${String.format("%.0f", user.totalExpenses)}"
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Settings Section
            Text(
                text = "Settings",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Settings Options
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .shadow(1.dp, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = PureWhite)
            ) {
                Column {
                    SettingsItem(
                        icon = Icons.Default.Person,
                        title = "Edit Profile",
                        subtitle = "Update your personal information",
                        onClick = { /* TODO: Navigate to edit profile */ }
                    )
                    
                    Divider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = PlaceholderText.copy(alpha = 0.2f)
                    )
                    
                    SettingsItem(
                        icon = Icons.Default.Notifications,
                        title = "Notifications",
                        subtitle = "Manage notification preferences",
                        onClick = { /* TODO: Navigate to notifications */ }
                    )
                    
                    Divider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = PlaceholderText.copy(alpha = 0.2f)
                    )
                    
                    SettingsItem(
                        icon = Icons.Default.Security,
                        title = "Privacy & Security",
                        subtitle = "Password and security settings",
                        onClick = { /* TODO: Navigate to security */ }
                    )
                    
                    Divider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = PlaceholderText.copy(alpha = 0.2f)
                    )
                    
                    SettingsItem(
                        icon = Icons.Default.Help,
                        title = "Help & Support",
                        subtitle = "Get help with FairShare",
                        onClick = { /* TODO: Navigate to help */ }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // About Section
            Text(
                text = "About",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .shadow(1.dp, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = PureWhite)
            ) {
                Column {
                    SettingsItem(
                        icon = Icons.Default.Info,
                        title = "About FairShare",
                        subtitle = "Version 1.0.0",
                        onClick = { /* TODO: Show about dialog */ }
                    )
                    
                    Divider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = PlaceholderText.copy(alpha = 0.2f)
                    )
                    
                    SettingsItem(
                        icon = Icons.Default.Star,
                        title = "Rate App",
                        subtitle = "Leave a review on the app store",
                        onClick = { /* TODO: Open app store */ }
                    )
                    
                    Divider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = PlaceholderText.copy(alpha = 0.2f)
                    )
                    
                    SettingsItem(
                        icon = Icons.Default.Share,
                        title = "Share App",
                        subtitle = "Tell friends about FairShare",
                        onClick = { /* TODO: Share app */ }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Sign Out Button
            Button(
                onClick = onSignOut,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ErrorRed,
                    contentColor = PureWhite
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    Icons.Default.ExitToApp,
                    contentDescription = "Sign out",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Sign Out",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun StatItem(
    label: String,
    value: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            fontSize = 20.sp,
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = androidx.compose.ui.graphics.Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        DarkGreen.copy(alpha = 0.1f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = title,
                    tint = DarkGreen,
                    modifier = Modifier.size(20.dp)
                )
            }
            
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
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
            
            Icon(
                Icons.Default.KeyboardArrowRight,
                contentDescription = "Navigate",
                tint = PlaceholderText,
                modifier = Modifier.size(20.dp)
            )
        }
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
fun ProfileScreenPreview() {
    FairrTheme {
        ProfileScreen(
            navController = rememberNavController()
        )
    }
} 