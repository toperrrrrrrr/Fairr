package com.example.fairr.ui.screens.groups

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.fairr.ui.theme.*
import com.example.fairr.ui.model.GroupMember
import com.example.fairr.ui.model.CreateGroupData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateGroupScreen(
    navController: NavController,
    onGroupCreated: () -> Unit = {}
) {
    var groupData by remember { mutableStateOf(CreateGroupData()) }
    var newMemberEmail by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var showAddMemberDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Dark header section with back button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
                )
        ) {
            // Back button
            IconButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.TopStart)
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
            
            // Create Group title and icon
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.align(Alignment.Center)
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Group,
                        contentDescription = "Group",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(30.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = "Create Group",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }

        // Form content
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Group name field
            item {
                OutlinedTextField(
                    value = groupData.name,
                    onValueChange = { groupData = groupData.copy(name = it) },
                    label = { Text("Group Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            // Description field
            item {
                OutlinedTextField(
                    value = groupData.description,
                    onValueChange = { groupData = groupData.copy(description = it) },
                    label = { Text("Description (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5
                )
            }

            // Currency field
            item {
                OutlinedTextField(
                    value = groupData.currency,
                    onValueChange = { groupData = groupData.copy(currency = it) },
                    label = { Text("Currency") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            // Members section
            item {
                Text(
                    text = "Members",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            // Add member button
            item {
                OutlinedButton(
                    onClick = { showAddMemberDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        Icons.Default.PersonAdd,
                        contentDescription = "Add Member",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add Member")
                }
            }

            // Members list
            items(groupData.members) { member ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = member.email,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        IconButton(
                            onClick = {
                                groupData = groupData.copy(
                                    members = groupData.members.filter { it != member }
                                )
                            }
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Remove Member",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }

            // Create group button
            item {
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = { 
                        if (validateGroup(groupData)) {
                            isLoading = true
                            // TODO: Implement actual group creation API call
                            onGroupCreated()
                        } else {
                            errorMessage = "Please fill in all required fields"
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = "Create Group",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Error message
                errorMessage?.let { error ->
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }
    }

    if (showAddMemberDialog) {
        AlertDialog(
            onDismissRequest = { showAddMemberDialog = false },
            title = { Text("Add Member") },
            text = {
                OutlinedTextField(
                    value = newMemberEmail,
                    onValueChange = { newMemberEmail = it },
                    label = { Text("Email") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newMemberEmail.isNotBlank()) {
                            groupData = groupData.copy(
                                members = groupData.members + GroupMember(
                                    id = "temp_${System.currentTimeMillis()}",
                                    name = newMemberEmail.substringBefore("@"),
                                    email = newMemberEmail
                                )
                            )
                            newMemberEmail = ""
                            showAddMemberDialog = false
                        }
                    }
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddMemberDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

private fun validateGroup(data: CreateGroupData): Boolean {
    return data.name.isNotBlank() && data.currency.isNotBlank() && data.members.isNotEmpty()
}

@Preview(showBackground = true)
@Composable
fun CreateGroupScreenPreview() {
    FairrTheme {
        CreateGroupScreen(
            navController = rememberNavController()
        )
    }
} 
