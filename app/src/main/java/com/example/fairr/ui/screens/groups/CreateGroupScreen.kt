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
                    color = DarkBackground,
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
                    tint = NeutralWhite
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
                            color = DarkGreen,
                            shape = RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Group,
                        contentDescription = "Group",
                        tint = NeutralWhite,
                        modifier = Modifier.size(30.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = "Create Group",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = NeutralWhite
                )
            }
        }

        // White form section with scroll
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(MaterialTheme.colorScheme.background)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Group name field
            item {
                OutlinedTextField(
                    value = groupData.name,
                    onValueChange = { groupData = groupData.copy(name = it) },
                    label = { 
                        Text(
                            "Group Name",
                            color = PlaceholderText,
                            fontSize = 14.sp
                        ) 
                    },
                    placeholder = {
                        Text(
                            "e.g., Weekend Trip, Apartment Expenses",
                            color = PlaceholderText,
                            fontSize = 14.sp
                        )
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Group,
                            contentDescription = "Group name",
                            tint = PlaceholderText
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DarkGreen,
                        unfocusedBorderColor = PlaceholderText,
                        focusedLabelColor = DarkGreen
                    ),
                    isError = errorMessage != null && groupData.name.isBlank()
                )
            }

            // Group description field
            item {
                OutlinedTextField(
                    value = groupData.description,
                    onValueChange = { groupData = groupData.copy(description = it) },
                    label = { 
                        Text(
                            "Description (Optional)",
                            color = PlaceholderText,
                            fontSize = 14.sp
                        ) 
                    },
                    placeholder = {
                        Text(
                            "What's this group for?",
                            color = PlaceholderText,
                            fontSize = 14.sp
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    maxLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DarkGreen,
                        unfocusedBorderColor = PlaceholderText,
                        focusedLabelColor = DarkGreen
                    )
                )
            }

            // Currency selection
            item {
                Text(
                    text = "Currency",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                )

                // Currency options
                val currencies = listOf("USD", "EUR", "GBP", "CAD", "AUD")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    currencies.forEach { currency ->
                        FilterChip(
                            onClick = { groupData = groupData.copy(currency = currency) },
                            label = { Text(currency) },
                            selected = groupData.currency == currency,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = DarkGreen,
                                selectedLabelColor = NeutralWhite
                            )
                        )
                    }
                }
            }

            // Members section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Members",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )
                    
                    TextButton(
                        onClick = { showAddMemberDialog = true }
                    ) {
                        Icon(
                            Icons.Default.PersonAdd,
                            contentDescription = "Add Member",
                            tint = DarkGreen
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "Add Member",
                            color = DarkGreen,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Members list
            items(groupData.members) { member ->
                MemberCard(
                    email = member.email,
                    isAdmin = member.isAdmin,
                    onRemove = {
                        groupData = groupData.copy(
                            members = groupData.members.filter { it.email != member.email }
                        )
                    }
                )
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
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DarkGreen,
                        contentColor = NeutralWhite
                    ),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = NeutralWhite,
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
                        color = ErrorRed,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }
    }

    // Add Member Dialog
    if (showAddMemberDialog) {
        AlertDialog(
            onDismissRequest = { showAddMemberDialog = false },
            title = { Text("Add Member") },
            text = {
                OutlinedTextField(
                    value = newMemberEmail,
                    onValueChange = { newMemberEmail = it },
                    label = { Text("Email Address") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ComponentColors.TextFieldBorderFocused,
                        unfocusedBorderColor = ComponentColors.TextFieldBorderUnfocused,
                        focusedLabelColor = ComponentColors.TextFieldLabelFocused
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (isValidEmail(newMemberEmail)) {
                            val newMember = GroupMember(
                                id = "temp_${System.currentTimeMillis()}",
                                name = newMemberEmail.substringBefore("@"),
                                email = newMemberEmail,
                                isAdmin = false,
                                isCurrentUser = false
                            )
                            groupData = groupData.copy(
                                members = groupData.members + newMember
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

@Composable
private fun MemberCard(
    email: String,
    isAdmin: Boolean,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = ComponentColors.CardBackgroundElevated
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(
                            if (isAdmin) ComponentColors.IconBackgroundSuccess
                            else ComponentColors.AvatarBackground,
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        tint = if (isAdmin) ComponentColors.Success else TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Column {
                    Text(
                        text = email,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimary
                    )
                    if (isAdmin) {
                        Text(
                            text = "Admin",
                            style = MaterialTheme.typography.labelSmall,
                            color = ComponentColors.Success,
                            modifier = Modifier
                                .background(
                                    ComponentColors.IconBackgroundSuccess,
                                    RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
            
            IconButton(
                onClick = onRemove,
                enabled = !isAdmin,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Remove member",
                    tint = if (isAdmin) 
                        TextSecondary.copy(alpha = 0.5f) 
                    else 
                        ComponentColors.Error
                )
            }
        }
    }
}

private fun validateGroup(group: CreateGroupData): Boolean {
    return group.name.isNotBlank()
}

private fun isValidEmail(email: String): Boolean {
    return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
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
