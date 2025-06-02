package com.example.fairr.ui.screens.groups

import androidx.compose.foundation.background
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
import com.example.fairr.ui.model.GroupMember
import com.example.fairr.ui.model.GroupSettingsData

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
    onLeaveGroup: () -> Unit = {}
) {
    // Sample group data
    val group = remember {
        GroupSettingsData(
            id = groupId,
            name = "Weekend Trip",
            description = "Fun weekend getaway with friends",
            inviteCode = "ABC123",
            currency = "$",
            createdBy = "Alice Johnson",
            isUserAdmin = true,
            memberCount = 4
        )
    }
    
    val members = remember {
        listOf(
            GroupMember("1", "Alice Johnson", "alice@example.com", true, true),
            GroupMember("2", "Bob Smith", "bob@example.com", false, false),
            GroupMember("3", "Charlie Brown", "charlie@example.com", false, false),
            GroupMember("4", "Diana Prince", "diana@example.com", false, false)
        )
    }
    
    val clipboardManager = LocalClipboardManager.current
    var showLeaveDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Group Settings",
                        style = MaterialTheme.typography.titleLarge
                    ) 
                },
                navigationIcon = {
                    IconButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(LightBackground)
                .padding(padding),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            // Group Info Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .shadow(2.dp, RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = ComponentColors.CardBackgroundElevated
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = group.name,
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = TextPrimary
                                )
                                if (group.description.isNotEmpty()) {
                                    Text(
                                        text = group.description,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextSecondary,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }
                            }
                            
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(
                                        ComponentColors.IconBackgroundSuccess,
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Group,
                                    contentDescription = "Group",
                                    tint = ComponentColors.Success,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            DetailItem(
                                label = "Created By",
                                value = group.createdBy
                            )
                            DetailItem(
                                label = "Currency",
                                value = group.currency
                            )
                            DetailItem(
                                label = "Members",
                                value = group.memberCount.toString()
                            )
                        }
                    }
                }
            }
            
            // Invite Code Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .shadow(1.dp, RoundedCornerShape(12.dp)),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = ComponentColors.CardBackgroundElevated
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Invite Code",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = TextPrimary
                                )
                                Text(
                                    text = group.inviteCode,
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = ComponentColors.Info,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }
                            
                            FilledTonalButton(
                                onClick = {
                                    clipboardManager.setText(AnnotatedString(group.inviteCode))
                                },
                                modifier = Modifier.height(48.dp)
                            ) {
                                Icon(
                                    Icons.Default.ContentCopy,
                                    contentDescription = "Copy",
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Copy")
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
                    color = TextPrimary,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
            
            items(members) { member ->
                MemberCard(
                    member = member,
                    isUserAdmin = group.isUserAdmin,
                    onRemoveMember = { /* TODO */ },
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
            
            // Group Actions Section
            item {
                Text(
                    text = "Actions",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
            
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .shadow(1.dp, RoundedCornerShape(12.dp)),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = ComponentColors.CardBackgroundElevated
                    )
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
                                onClick = { /* TODO */ },
                                textColor = ComponentColors.Error
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
                                textColor = ComponentColors.Error
                            )
                        }
                    }
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
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
                        onLeaveGroup()
                    }
                ) {
                    Text(
                        "Leave",
                        color = ComponentColors.Error
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showLeaveDialog = false }
                ) {
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
        modifier = modifier
            .fillMaxWidth()
            .shadow(1.dp, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = ComponentColors.CardBackgroundElevated
        )
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
                        if (member.isAdmin) 
                            ComponentColors.IconBackgroundSuccess
                        else if (member.isCurrentUser)
                            ComponentColors.IconBackgroundInfo
                        else
                            ComponentColors.AvatarBackground,
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = member.name.split(" ").map { it.first() }.joinToString(""),
                    style = MaterialTheme.typography.titleMedium,
                    color = if (member.isAdmin)
                        ComponentColors.Success
                    else if (member.isCurrentUser)
                        ComponentColors.Info
                    else
                        TextPrimary
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = member.name + if (member.isCurrentUser) " (You)" else "",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary
                )
                Text(
                    text = if (member.isAdmin) "Admin" else "Member",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
            
            if (isUserAdmin && !member.isCurrentUser) {
                IconButton(
                    onClick = { onRemoveMember(member) },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Remove member",
                        tint = ComponentColors.Error
                    )
                }
            }
        }
    }
}

@Composable
fun SettingsActionItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    textColor: androidx.compose.ui.graphics.Color = TextPrimary
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
            Icon(
                icon,
                contentDescription = title,
                tint = textColor,
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = textColor
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (textColor == ComponentColors.Error)
                        textColor.copy(alpha = 0.7f)
                    else
                        TextSecondary
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
