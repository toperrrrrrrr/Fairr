package com.example.fairr.ui.screens.groups

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.fairr.ui.theme.*
import com.example.fairr.data.model.GroupMember
import com.example.fairr.data.model.GroupRole
import com.example.fairr.ui.viewmodel.GroupSettingsEvent
import com.example.fairr.ui.viewmodel.GroupSettingsViewModel
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
private fun DetailItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupSettingsScreen(
    navController: NavController,
    groupId: String,
    viewModel: GroupSettingsViewModel = hiltViewModel()
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showLeaveDialog by remember { mutableStateOf(false) }
    val clipboardManager = LocalClipboardManager.current
    
    val uiState by viewModel.uiState.collectAsState()
    val group = uiState.group
    val members = uiState.members

    LaunchedEffect(groupId) {
        viewModel.loadGroup(groupId)
    }

    // Handle UI events
    LaunchedEffect(Unit) {
        viewModel.uiEvents.collect { event ->
            when (event) {
                is GroupSettingsEvent.NavigateBack -> navController.popBackStack()
                is GroupSettingsEvent.ShowError -> {
                    // Show error message
                }
                is GroupSettingsEvent.GroupDeleted -> {
                    // Navigate to main screen and clear back stack
                    navController.navigate("main") {
                        popUpTo("main") { inclusive = true }
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Group Settings") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Group Info Section
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = group.name,
                            style = MaterialTheme.typography.headlineSmall
                        )
                        if (group.description.isNotEmpty()) {
                            Text(
                                text = group.description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        // Invite Code Section
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Invite Code",
                                    style = MaterialTheme.typography.labelMedium
                                )
                                Text(
                                    text = group.inviteCode,
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                            IconButton(
                                onClick = {
                                    clipboardManager.setText(AnnotatedString(group.inviteCode))
                                }
                            ) {
                                Icon(
                                    Icons.Default.ContentCopy,
                                    contentDescription = "Copy Invite Code"
                                )
                            }
                        }
                    }
                }
            }

            // Members Section
            item {
                Text(
                    text = "Members (${members.size})",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(16.dp)
                )
            }

            items(members) { member ->
                MemberCard(
                    member = member,
                    isUserAdmin = group.isUserAdmin,
                    onRemoveMember = { viewModel.removeMember(it) },
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            // Actions Section
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Column {
                        if (group.isUserAdmin) {
                            SettingsActionItem(
                                icon = Icons.Default.Edit,
                                title = "Edit Group",
                                subtitle = "Change group name, description, or currency",
                                onClick = { /* TODO */ }
                            )
                            
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                            
                            SettingsActionItem(
                                icon = Icons.Default.Delete,
                                title = "Delete Group",
                                subtitle = "Permanently delete this group",
                                onClick = { showDeleteDialog = true },
                                textColor = MaterialTheme.colorScheme.error
                            )
                        }
                        
                        if (!group.isUserAdmin || members.size > 1) {
                            if (group.isUserAdmin) {
                                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                            }
                            
                            SettingsActionItem(
                                icon = Icons.AutoMirrored.Filled.ExitToApp,
                                title = "Leave Group",
                                subtitle = if (group.isUserAdmin && members.size > 1)
                                    "Transfer ownership before leaving"
                                else
                                    "Leave this group",
                                onClick = { showLeaveDialog = true },
                                textColor = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }
    }

    // Delete Group Dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Group") },
            text = { 
                Text(
                    "Are you sure you want to delete this group? This action cannot be undone and will delete all associated expenses.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.deleteGroup()
                    }
                ) {
                    Text(
                        "Delete",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Leave Group Dialog
    if (showLeaveDialog) {
        AlertDialog(
            onDismissRequest = { showLeaveDialog = false },
            title = { Text("Leave Group") },
            text = { 
                Text(
                    "Are you sure you want to leave this group? You won't be able to rejoin unless you're invited again.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLeaveDialog = false
                        viewModel.leaveGroup()
                    }
                ) {
                    Text(
                        "Leave",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showLeaveDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun MemberCard(
    member: GroupMember,
    isUserAdmin: Boolean,
    onRemoveMember: (GroupMember) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
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
            Column {
                Text(
                    text = member.name,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = member.email,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (isUserAdmin && member.role != GroupRole.ADMIN) {
                IconButton(onClick = { onRemoveMember(member) }) {
                    Icon(
                        Icons.Default.PersonRemove,
                        contentDescription = "Remove Member",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
fun SettingsActionItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        color = Color.Transparent
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
                tint = textColor
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = textColor
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GroupSettingsScreenPreview() {
    FairrTheme {
        GroupSettingsScreen(
            navController = rememberNavController(),
            groupId = "1"
        )
    }
} 
