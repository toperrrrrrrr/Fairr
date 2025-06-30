package com.example.fairr.ui.screens.friends

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.fairr.data.model.*
import com.example.fairr.data.friends.FriendSuggestion
import com.example.fairr.ui.model.Friend
import com.example.fairr.ui.model.FriendRequest
import com.example.fairr.ui.theme.*
import com.example.fairr.ui.components.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun FriendsScreen(
    navController: NavController,
    viewModel: FriendsViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState = viewModel.uiState
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // State for moderation dialogs
    var showBlockDialog by remember { mutableStateOf<Friend?>(null) }
    var showReportDialog by remember { mutableStateOf<Friend?>(null) }

    // Collect user messages
    LaunchedEffect(true) {
        viewModel.userMessage.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    // Block user dialog
    showBlockDialog?.let { friend ->
        BlockUserDialog(
            userName = friend.name,
            onConfirm = { reason ->
                viewModel.blockUser(friend.id, friend.name, friend.email, reason)
                showBlockDialog = null
            },
            onDismiss = { showBlockDialog = null }
        )
    }

    // Report user dialog
    showReportDialog?.let { friend ->
        ReportUserDialog(
            userName = friend.name,
            onConfirm = { reportType, reason, description ->
                viewModel.reportUser(friend.id, friend.name, friend.email, reportType, reason, description)
                showReportDialog = null
            },
            onDismiss = { showReportDialog = null }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Friends") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        when (uiState) {
            is FriendsUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is FriendsUiState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(uiState.message)
                }
            }

            is FriendsUiState.Success -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    // Add friend section
                    AddFriendSection(
                        email = uiState.emailInput,
                        onEmailChange = viewModel::onEmailInputChange,
                        onSendRequest = viewModel::sendFriendRequest
                    )

                    // Friend requests section
                    if (uiState.pendingRequests.isNotEmpty()) {
                        FriendRequestsSection(
                            title = "Pending Friend Requests",
                            requests = uiState.pendingRequests,
                            showActions = true,
                            onAccept = viewModel::acceptFriendRequest,
                            onReject = viewModel::rejectFriendRequest
                        )
                    }

                    // Accepted requests section
                    if (uiState.acceptedRequests.isNotEmpty()) {
                        FriendRequestsSection(
                            title = "Accepted Friend Requests",
                            requests = uiState.acceptedRequests,
                            showActions = false,
                            onAccept = { }, // No actions for accepted requests
                            onReject = { }
                        )
                    }

                    // Friend groups section
                    if (uiState.friendSuggestions.isNotEmpty()) {
                        FriendSuggestionsSection(
                            suggestions = uiState.friendSuggestions,
                            onSendRequest = { suggestion ->
                                viewModel.sendFriendRequestToUser(suggestion.email)
                            }
                        )
                    }

                    // Friend Groups Management
                    ModernCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .clickable { 
                                // Navigate to friend groups screen
                                navController.navigate("friend_groups")
                            },
                        backgroundColor = MaterialTheme.colorScheme.surface,
                        shadowElevation = 2,
                        cornerRadius = 12
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Groups,
                                contentDescription = "Friend Groups",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Friend Groups",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "Organize friends into categories",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Go to groups",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Friend activity feed section
                    if (uiState.friendActivities.isNotEmpty()) {
                        FriendActivityFeed(
                            activities = uiState.friendActivities
                        )
                    }

                    // Friends list
                    FriendsList(
                        friends = uiState.friends,
                        onRemoveFriend = { friendId ->
                            scope.launch {
                                val result = snackbarHostState.showSnackbar(
                                    message = "Remove friend?",
                                    actionLabel = "Remove",
                                    duration = SnackbarDuration.Long
                                )
                                if (result == SnackbarResult.ActionPerformed) {
                                    viewModel.removeFriend(friendId)
                                }
                            }
                        },
                        onBlockUser = { friend ->
                            showBlockDialog = friend
                        },
                        onReportUser = { friend ->
                            showReportDialog = friend
                        },
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}

@Composable
private fun AddFriendSection(
    email: String,
    onEmailChange: (String) -> Unit,
    onSendRequest: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = "Add Friend",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = email,
                onValueChange = onEmailChange,
                label = { Text("Friend's Email") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = onSendRequest,
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Send Request")
            }
        }
    }
}

@Composable
private fun FriendRequestsSection(
    title: String,
    requests: List<FriendRequest>,
    showActions: Boolean,
    onAccept: (String) -> Unit,
    onReject: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            LazyColumn {
                items(requests) { request ->
                    FriendRequestItem(
                        request = request,
                        showActions = showActions,
                        onAccept = { onAccept(request.id) },
                        onReject = { onReject(request.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun FriendRequestItem(
    request: FriendRequest,
    showActions: Boolean,
    onAccept: () -> Unit,
    onReject: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = request.senderName,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = request.senderEmail,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (showActions) {
            Row {
                IconButton(onClick = onAccept) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = "Accept",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = onReject) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Reject",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
private fun FriendsList(
    friends: List<Friend>,
    onRemoveFriend: (String) -> Unit,
    onBlockUser: (Friend) -> Unit,
    onReportUser: (Friend) -> Unit,
    viewModel: FriendsViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "Friends",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
        if (friends.isEmpty()) {
            Text(
                text = "No friends yet",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            LazyColumn {
                items(friends) { friend ->
                    FriendItem(
                        friend = friend,
                        onRemove = { onRemoveFriend(friend.id) },
                        onBlock = { onBlockUser(friend) },
                        onReport = { onReportUser(friend) },
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}

@Composable
private fun FriendItem(
    friend: Friend,
    onRemove: () -> Unit,
    onBlock: () -> Unit,
    onReport: () -> Unit,
    viewModel: FriendsViewModel
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = friend.name,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = friend.email,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            IconButton(onClick = onRemove) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Remove friend",
                    tint = MaterialTheme.colorScheme.error
                )
            }
            IconButton(onClick = onBlock) {
                Icon(
                    Icons.Default.Block,
                    contentDescription = "Block user",
                    tint = MaterialTheme.colorScheme.error
                )
            }
            IconButton(onClick = onReport) {
                Icon(
                    Icons.Default.Report,
                    contentDescription = "Report user",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun FriendActivityFeed(
    activities: List<FriendActivity>
) {
    val dateFormat = remember { SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = "Recent Friend Activity",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(12.dp))
            
            LazyColumn(
                modifier = Modifier.heightIn(max = 300.dp)
            ) {
                items(activities.take(10)) { activity ->
                    FriendActivityItem(
                        activity = activity,
                        dateFormat = dateFormat
                    )
                    if (activity != activities.last()) {
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 8.dp),
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FriendActivityItem(
    activity: FriendActivity,
    dateFormat: SimpleDateFormat
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Activity icon
        Text(
            text = activity.type.icon,
            fontSize = 20.sp,
            modifier = Modifier.padding(end = 12.dp)
        )
        
        // Activity content
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = activity.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
            )
            Text(
                text = activity.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            if (activity.amount != null) {
                Text(
                    text = "₱${String.format("%.2f", activity.amount)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
                )
            }
        }
        
        // Timestamp
        Text(
            text = dateFormat.format(activity.timestamp.toDate()),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}

@Composable
private fun FriendSuggestionsSection(
    suggestions: List<FriendSuggestion>,
    onSendRequest: (FriendSuggestion) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = "People You May Know",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "Based on mutual groups and friends",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(12.dp))
            
            LazyColumn(
                modifier = Modifier.heightIn(max = 400.dp)
            ) {
                items(suggestions.take(5)) { suggestion ->
                    FriendSuggestionItem(
                        suggestion = suggestion,
                        onSendRequest = { onSendRequest(suggestion) }
                    )
                    if (suggestion != suggestions.last()) {
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 8.dp),
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FriendSuggestionItem(
    suggestion: FriendSuggestion,
    onSendRequest: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // User avatar placeholder
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = suggestion.name.split(" ")
                    .take(2)
                    .joinToString("") { it.firstOrNull()?.uppercase() ?: "" },
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary
            )
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        // User info
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = suggestion.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
            )
            Text(
                text = suggestion.suggestionReason,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
        
        // Add friend button
        OutlinedButton(
            onClick = onSendRequest,
            modifier = Modifier.size(width = 100.dp, height = 36.dp),
            contentPadding = PaddingValues(8.dp)
        ) {
            Icon(
                Icons.Default.PersonAdd,
                contentDescription = "Add Friend",
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Add",
                fontSize = 12.sp
            )
        }
    }
} 