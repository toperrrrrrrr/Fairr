package com.example.fairr.ui.screens.groups

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.fairr.ui.theme.*

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
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PureWhite
                )
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
                GroupInfoCard(
                    group = group,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
            
            // Invite Code Card
            item {
                InviteCodeCard(
                    inviteCode = group.inviteCode,
                    onCopyCode = {
                        clipboardManager.setText(AnnotatedString(group.inviteCode))
                    },
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
            
            // Members Section
            item {
                Text(
                    text = "Members (${members.size})",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
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
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
            
            item {
                GroupActionsCard(
                    isUserAdmin = group.isUserAdmin,
                    onEditGroup = { /* TODO */ },
                    onLeaveGroup = { showLeaveDialog = true },
                    onDeleteGroup = { /* TODO */ },
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
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
            text = { Text("Are you sure you want to leave this group? You won't be able to rejoin unless you're invited again.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLeaveDialog = false
                        onLeaveGroup()
                    }
                ) {
                    Text("Leave", color = ErrorRed)
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
fun GroupInfoCard(
    group: GroupSettingsData,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
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
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = group.name,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    if (group.description.isNotEmpty()) {
                        Text(
                            text = group.description,
                            fontSize = 14.sp,
                            color = TextSecondary,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
                
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            DarkGreen.copy(alpha = 0.1f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Group,
                        contentDescription = "Group",
                        tint = DarkGreen,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Group details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                DetailItem(
                    label = "Created by",
                    value = group.createdBy
                )
                DetailItem(
                    label = "Currency",
                    value = group.currency
                )
            }
        }
    }
}

@Composable
fun InviteCodeCard(
    inviteCode: String,
    onCopyCode: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = PureWhite)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Invite Code",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            
            Text(
                text = "Share this code with friends to invite them to the group",
                fontSize = 12.sp,
                color = TextSecondary,
                modifier = Modifier.padding(top = 4.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = LightBackground,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = inviteCode,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        modifier = Modifier.padding(16.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Button(
                    onClick = onCopyCode,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DarkGreen,
                        contentColor = PureWhite
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        Icons.Default.ContentCopy,
                        contentDescription = "Copy",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Copy", fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
fun MemberCard(
    member: GroupMember,
    isUserAdmin: Boolean,
    onRemoveMember: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(1.dp, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = PureWhite)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        if (member.isCurrentUser) DarkGreen.copy(alpha = 0.2f) 
                        else PlaceholderText.copy(alpha = 0.2f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = "Member",
                    tint = if (member.isCurrentUser) DarkGreen else PlaceholderText,
                    modifier = Modifier.size(20.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Member info
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (member.isCurrentUser) "${member.name} (You)" else member.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )
                    if (member.isAdmin) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            color = DarkBlue.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "ADMIN",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = DarkBlue,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                Text(
                    text = member.email,
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
            
            // Actions
            if (isUserAdmin && !member.isCurrentUser) {
                IconButton(onClick = onRemoveMember) {
                    Icon(
                        Icons.Default.PersonRemove,
                        contentDescription = "Remove member",
                        tint = ErrorRed,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun GroupActionsCard(
    isUserAdmin: Boolean,
    onEditGroup: () -> Unit,
    onLeaveGroup: () -> Unit,
    onDeleteGroup: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(1.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = PureWhite)
    ) {
        Column {
            if (isUserAdmin) {
                SettingsActionItem(
                    icon = Icons.Default.Edit,
                    title = "Edit Group",
                    subtitle = "Change group name and description",
                    onClick = onEditGroup
                )
                
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = PlaceholderText.copy(alpha = 0.2f)
                )
            }
            
            SettingsActionItem(
                icon = Icons.Default.ExitToApp,
                title = "Leave Group",
                subtitle = "Remove yourself from this group",
                onClick = onLeaveGroup,
                textColor = ErrorRed
            )
            
            if (isUserAdmin) {
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = PlaceholderText.copy(alpha = 0.2f)
                )
                
                SettingsActionItem(
                    icon = Icons.Default.Delete,
                    title = "Delete Group",
                    subtitle = "Permanently delete this group",
                    onClick = onDeleteGroup,
                    textColor = ErrorRed
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
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
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = textColor
                )
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = if (textColor == ErrorRed) ErrorRed.copy(alpha = 0.7f) else TextSecondary
                )
            }
        }
    }
}

// Data classes
data class GroupSettingsData(
    val id: String,
    val name: String,
    val description: String,
    val inviteCode: String,
    val currency: String,
    val createdBy: String,
    val isUserAdmin: Boolean,
    val memberCount: Int
)

data class GroupMember(
    val id: String,
    val name: String,
    val email: String,
    val isAdmin: Boolean,
    val isCurrentUser: Boolean
)

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