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
import com.example.fairr.data.model.Group
import com.example.fairr.data.model.GroupMember
import com.example.fairr.data.model.GroupRole
import com.example.fairr.ui.viewmodel.GroupSettingsEvent
import com.example.fairr.ui.viewmodel.GroupSettingsViewModel
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.firebase.auth.FirebaseAuth
import com.example.fairr.ui.components.ModernCard
import com.example.fairr.ui.components.GroupEmojiAvatar
import com.example.fairr.ui.components.UserEmojiAvatar
import com.example.fairr.navigation.Screen

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
    var showInviteDialog by remember { mutableStateOf(false) }
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
                    // Show error message - you can implement a snackbar here
                }
                is GroupSettingsEvent.ShowSuccess -> {
                    // Show success message - you can implement a snackbar here
                }
                is GroupSettingsEvent.GroupDeleted -> {
                    // Navigate to main screen and clear back stack
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Main.route) { 
                            this.inclusive = true 
                        }
                    }
                }
                is GroupSettingsEvent.GroupUpdated -> {
                    // Group was updated successfully
                }
                is GroupSettingsEvent.MemberRemoved -> {
                    // Member was removed successfully
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
                .padding(padding),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
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
                        // Group header with avatar
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Group Avatar
                            GroupEmojiAvatar(
                                avatar = group.avatar,
                                groupName = group.name,
                                size = 60.dp
                            )
                            
                            Spacer(modifier = Modifier.width(16.dp))
                            
                            Column(modifier = Modifier.weight(1f)) {
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
                            }
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
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Members (${members.size})",
                        style = MaterialTheme.typography.titleLarge
                    )
                    
                    if (group.isUserAdmin) {
                        OutlinedButton(
                            onClick = { showInviteDialog = true }
                        ) {
                            Icon(
                                Icons.Default.PersonAdd,
                                contentDescription = "Invite Members",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Invite")
                        }
                    }
                }
            }

            items(members) { member ->
                MemberCard(
                    member = member,
                    isUserAdmin = group.isUserAdmin,
                    onRemoveMember = { viewModel.showRemoveMemberDialog(it) },
                    onPromote = { viewModel.showPromoteMemberDialog(it) },
                    onDemote = { viewModel.showDemoteMemberDialog(it) },
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
                                onClick = { viewModel.showEditGroupDialog() }
                            )
                            
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                            
                            SettingsActionItem(
                                icon = if (group.isArchived) Icons.Default.Unarchive else Icons.Default.Archive,
                                title = if (group.isArchived) "Unarchive Group" else "Archive Group",
                                subtitle = if (group.isArchived) 
                                    "Make this group active again" 
                                else 
                                    "Hide this group from active list",
                                onClick = { 
                                    if (group.isArchived) {
                                        viewModel.unarchiveGroup()
                                    } else {
                                        viewModel.archiveGroup()
                                    }
                                }
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

    // Remove Member Dialog
    if (uiState.showRemoveMemberDialog && uiState.memberToRemove != null) {
        AlertDialog(
            onDismissRequest = { viewModel.hideRemoveMemberDialog() },
            title = { Text("Remove Member") },
            text = { 
                Text(
                    "Are you sure you want to remove ${uiState.memberToRemove?.name} from this group? They will need to be invited again to rejoin.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        uiState.memberToRemove?.let { member ->
                            viewModel.removeMember(member)
                        }
                    }
                ) {
                    Text(
                        "Remove",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.hideRemoveMemberDialog() }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Edit Group Dialog
    if (uiState.showEditGroupDialog) {
        EditGroupDialog(
            group = group,
            onDismiss = { viewModel.hideEditGroupDialog() },
            onSave = { name, description, currency ->
                viewModel.updateGroup(name, description, currency)
            }
        )
    }

    // Promote Member Dialog
    if (uiState.showPromoteMemberDialog && uiState.memberToPromote != null) {
        AlertDialog(
            onDismissRequest = { viewModel.hidePromoteMemberDialog() },
            title = { Text("Promote to Admin") },
            text = {
                Text(
                    "Are you sure you want to promote ${uiState.memberToPromote?.name} to admin?",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        uiState.memberToPromote?.let { viewModel.promoteMember(it) }
                    }
                ) {
                    Text("Promote")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.hidePromoteMemberDialog() }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Demote Member Dialog
    if (uiState.showDemoteMemberDialog && uiState.memberToDemote != null) {
        AlertDialog(
            onDismissRequest = { viewModel.hideDemoteMemberDialog() },
            title = { Text("Demote from Admin") },
            text = {
                Text(
                    "Are you sure you want to demote ${uiState.memberToDemote?.name} from admin?",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        uiState.memberToDemote?.let { viewModel.demoteMember(it) }
                    }
                ) {
                    Text("Demote")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.hideDemoteMemberDialog() }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Invite Members Dialog
    if (showInviteDialog) {
        InviteGroupDialog(
            groupName = group.name,
            onDismiss = { showInviteDialog = false },
            onInvite = { email, message ->
                viewModel.sendGroupInvitation(email, message)
                showInviteDialog = false
            }
        )
    }
}

@Composable
fun MemberCard(
    member: GroupMember,
    isUserAdmin: Boolean,
    onRemoveMember: (GroupMember) -> Unit,
    onPromote: (GroupMember) -> Unit = {},
    onDemote: (GroupMember) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
    
    ModernCard(
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                // User Avatar
                UserEmojiAvatar(
                    name = member.name,
                    size = 40.dp
                )
                
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = member.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        if (member.role == GroupRole.ADMIN) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                            ) {
                                Text(
                                    text = "Admin",
                                    color = MaterialTheme.colorScheme.primary,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                        if (member.userId == currentUserId) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "You",
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                    Text(
                        text = member.email,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Action buttons
            if (isUserAdmin && member.userId != currentUserId) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (member.role != GroupRole.ADMIN) {
                        IconButton(onClick = { onPromote(member) }) {
                            Icon(
                                Icons.Default.Star,
                                contentDescription = "Promote to Admin",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        IconButton(onClick = { onRemoveMember(member) }) {
                            Icon(
                                Icons.Default.PersonRemove,
                                contentDescription = "Remove Member",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    } else {
                        IconButton(onClick = { onDemote(member) }) {
                            Icon(
                                Icons.Default.StarOutline,
                                contentDescription = "Demote from Admin",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditGroupDialog(
    group: Group,
    onDismiss: () -> Unit,
    onSave: (name: String, description: String, currency: String) -> Unit
) {
    var name by remember { mutableStateOf(group.name) }
    var description by remember { mutableStateOf(group.description) }
    var currency by remember { mutableStateOf(group.currency) }
    var isNameError by remember { mutableStateOf(false) }
    var isCurrencyError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Group") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Group Name
                OutlinedTextField(
                    value = name,
                    onValueChange = { 
                        name = it
                        isNameError = false
                    },
                    label = { Text("Group Name") },
                    isError = isNameError,
                    supportingText = {
                        if (isNameError) {
                            Text("Group name cannot be empty")
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                // Group Description
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 3
                )

                // Currency
                OutlinedTextField(
                    value = currency,
                    onValueChange = { 
                        currency = it
                        isCurrencyError = false
                    },
                    label = { Text("Currency") },
                    isError = isCurrencyError,
                    supportingText = {
                        if (isCurrencyError) {
                            Text("Currency cannot be empty")
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    // Validate inputs
                    var hasError = false
                    if (name.isBlank()) {
                        isNameError = true
                        hasError = true
                    }
                    if (currency.isBlank()) {
                        isCurrencyError = true
                        hasError = true
                    }
                    
                    if (!hasError) {
                        onSave(name.trim(), description.trim(), currency.trim())
                    }
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun InviteGroupDialog(
    groupName: String,
    onDismiss: () -> Unit,
    onInvite: (String, String) -> Unit
) {
    var email by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var isEmailError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Invite to $groupName") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Email Field
                OutlinedTextField(
                    value = email,
                    onValueChange = { 
                        email = it
                        isEmailError = false
                    },
                    label = { Text("Email Address") },
                    placeholder = { Text("friend@example.com") },
                    isError = isEmailError,
                    supportingText = {
                        if (isEmailError) {
                            Text("Please enter a valid email address")
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = {
                        Icon(
                            Icons.Default.Email,
                            contentDescription = "Email"
                        )
                    }
                )

                // Optional Message
                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    label = { Text("Message (Optional)") },
                    placeholder = { Text("Hey! Join our group to split expenses...") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 3,
                    leadingIcon = {
                        Icon(
                            Icons.Default.Message,
                            contentDescription = "Message"
                        )
                    }
                )
                
                // Info text
                Text(
                    text = "The person will receive an email invitation with a link to join the group.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    // Basic email validation
                    if (email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                        isEmailError = true
                    } else {
                        onInvite(email.trim(), message.trim())
                    }
                }
            ) {
                Text("Send Invite")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
} 
