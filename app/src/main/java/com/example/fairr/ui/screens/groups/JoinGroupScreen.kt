package com.example.fairr.ui.screens.groups

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Key
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.fairr.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JoinGroupScreen(
    navController: NavController,
    onJoinSuccess: () -> Unit = {}
) {
    var inviteCode by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Join Group",
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(60.dp))
            
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .background(
                        MaterialTheme.colorScheme.primary,
                        RoundedCornerShape(24.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Group,
                    contentDescription = "Join Group",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(60.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Text(
                text = "Join a Group",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = "Enter the invite code shared by your friend to join their group",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp, vertical = 8.dp)
            )
            
            Spacer(modifier = Modifier.height(40.dp))
            
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    OutlinedTextField(
                        value = inviteCode,
                        onValueChange = {
                            inviteCode = it.uppercase()
                            errorMessage = ""
                        },
                        label = { Text("Invite Code") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(8.dp),
                        isError = errorMessage.isNotEmpty(),
                        leadingIcon = {
                            Icon(
                                Icons.Default.Key,
                                contentDescription = "Invite Code"
                            )
                        }
                    )
                    
                    if (errorMessage.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = errorMessage,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Button(
                        onClick = {
                            if (inviteCode.isBlank()) {
                                errorMessage = "Please enter an invite code"
                            } else {
                                isLoading = true
                                // TODO: Implement group joining logic
                                onJoinSuccess()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(8.dp),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Join Group")
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "💡 How it works",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Ask a group member to share their invite code from the group settings. The code is usually 6-8 characters long.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun JoinGroupScreenPreview() {
    FairrTheme {
        JoinGroupScreen(
            navController = rememberNavController()
        )
    }
} 
