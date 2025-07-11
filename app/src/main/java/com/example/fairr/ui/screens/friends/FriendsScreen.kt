package com.example.fairr.ui.screens.friends

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.clickable
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
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
import com.example.fairr.navigation.Screen
import coil.compose.AsyncImage
import com.example.fairr.ui.components.FairrCardDefaults
import com.example.fairr.ui.components.ListItemCard
import com.example.fairr.ui.components.StandardCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendsScreen(
    navController: NavController,
    viewModel: FriendsViewModel = hiltViewModel(),
    @Suppress("UNUSED_PARAMETER") onNavigateBack: () -> Unit
) {
    val uiState = viewModel.uiState
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val pullToRefreshState = rememberPullToRefreshState()
    var searchQuery by remember { mutableStateOf("") }

    // State for dialogs
    var showReportDialog by remember { mutableStateOf<Friend?>(null) }
    var showAddFriendDialog by remember { mutableStateOf(false) }

    // Collect user messages
    LaunchedEffect(true) {
        viewModel.userMessage.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    // Pull to refresh handling
    LaunchedEffect(pullToRefreshState.isRefreshing) {
        if (pullToRefreshState.isRefreshing) {
            viewModel.refreshData()
            pullToRefreshState.endRefresh()
        }
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

    // Add friend modal dialog
    if (showAddFriendDialog) {
        AddFriendModal(
            email = uiState.let { if (it is FriendsUiState.Success) it.emailInput else "" },
            onEmailChange = viewModel::onEmailInputChange,
            onSendRequest = {
                viewModel.sendFriendRequest()
                showAddFriendDialog = false
            },
            onDismiss = { showAddFriendDialog = false },
            isLoading = false // Add loading state to ViewModel if needed
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Friends", style = MaterialTheme.typography.headlineSmall) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundPrimary)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddFriendDialog = true },
                containerColor = Primary,
                contentColor = PureWhite
            ) {
                Icon(
                    imageVector = Icons.Default.PersonAdd,
                    contentDescription = "Add Friend"
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = BackgroundPrimary
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(pullToRefreshState.nestedScrollConnection)
        ) {
            when (uiState) {
                is FriendsUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Primary)
                    }
                }

                is FriendsUiState.Error -> {
                    ModernErrorState(
                        title = "Unable to load friends",
                        message = uiState.message,
                        onRetry = { viewModel.refreshData() },
                        modifier = Modifier.fillMaxSize()
                    )
                }

                is FriendsUiState.Success -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Search section for friends
                        if (uiState.friends.isNotEmpty()) {
                            item {
                                ModernSearchSection(
                                    searchQuery = searchQuery,
                                    onSearchChange = { searchQuery = it },
                                    placeholder = "Search friends..."
                                )
                            }
                        }

                        // Friend requests section
                        if (uiState.pendingRequests.isNotEmpty()) {
                            item {
                                ModernFriendRequestsSection(
                                    title = "Pending Friend Requests",
                                    requests = uiState.pendingRequests,
                                    showActions = true,
                                    onAccept = viewModel::acceptFriendRequest,
                                    onReject = viewModel::rejectFriendRequest
                                )
                            }
                        }

                        // Friend suggestions section
                        if (uiState.friendSuggestions.isNotEmpty()) {
                            item {
                                ModernFriendSuggestionsSection(
                                    suggestions = uiState.friendSuggestions,
                                    onSendRequest = { suggestion ->
                                        viewModel.sendFriendRequestToUser(suggestion.email)
                                    }
                                )
                            }
                        }

                        // Friend Groups Management
                        item {
                            FriendGroupsCard(
                                onClick = {
                                    navController.navigate(Screen.FriendGroups.route)
                                }
                            )
                        }

                        // Friend activity feed section
                        if (uiState.friendActivities.isNotEmpty()) {
                            item {
                                ModernFriendActivityFeed(
                                    activities = uiState.friendActivities
                                )
                            }
                        }

                        // Friends list
                        item {
                            ModernFriendsList(
                                friends = uiState.friends,
                                searchQuery = searchQuery,
                                navController = navController,
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
                                onReportUser = { friend ->
                                    showReportDialog = friend
                                }
                            )
                        }
                    }
                }
            }

            PullToRefreshContainer(
                modifier = Modifier.align(Alignment.TopCenter),
                state = pullToRefreshState,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddFriendModal(
    email: String,
    onEmailChange: (String) -> Unit,
    onSendRequest: () -> Unit,
    onDismiss: () -> Unit,
    isLoading: Boolean
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    
    BasicAlertDialog(
        onDismissRequest = onDismiss
    ) {
        ModernCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = CardBackground,
            cornerRadius = 20
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(Primary.copy(alpha = 0.1f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PersonAdd,
                                contentDescription = null,
                                tint = Primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        
                        Text(
                            text = "Add Friend",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                    
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = IconTint
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                Text(
                    text = "Enter your friend's email address to send them a friend request.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
                
                Spacer(modifier = Modifier.height(20.dp))
                
                OutlinedTextField(
                    value = email,
                    onValueChange = onEmailChange,
                    label = { Text("Friend's Email") },
                    placeholder = { Text("Enter email address") },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Email,
                            contentDescription = null,
                            tint = IconTint
                        )
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Send
                    ),
                    keyboardActions = KeyboardActions(
                        onSend = {
                            keyboardController?.hide()
                            onSendRequest()
                        }
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Primary,
                        focusedLabelColor = Primary
                    )
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = TextSecondary
                        )
                    ) {
                        Text("Cancel")
                    }
                    
                    Button(
                        onClick = {
                            keyboardController?.hide()
                            onSendRequest()
                        },
                        enabled = email.isNotBlank() && !isLoading,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Primary,
                            contentColor = PureWhite
                        )
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = PureWhite
                            )
                        } else {
                            Icon(
                                Icons.AutoMirrored.Filled.Send,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Send Request")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ModernErrorState(
    title: String,
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.ErrorOutline,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = ComponentColors.Error
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(
                containerColor = Primary,
                contentColor = PureWhite
            )
        ) {
            Icon(
                Icons.Default.Refresh,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Try Again")
        }
    }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ModernSearchSection(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    placeholder: String
) {
    OutlinedTextField(
        value = searchQuery,
        onValueChange = onSearchChange,
        placeholder = { Text(placeholder) },
        leadingIcon = {
            Icon(
                Icons.Default.Search,
                contentDescription = "Search",
                tint = IconTint
            )
        },
        trailingIcon = {
            if (searchQuery.isNotEmpty()) {
                IconButton(onClick = { onSearchChange("") }) {
                    Icon(
                        Icons.Default.Clear,
                        contentDescription = "Clear search",
                        tint = IconTint
                    )
                }
            }
        },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Primary,
            unfocusedBorderColor = LightGray
        )
    )
}

@Composable
private fun ModernFriendRequestsSection(
    title: String,
    requests: List<FriendRequest>,
    showActions: Boolean,
    onAccept: (String) -> Unit,
    onReject: (String) -> Unit
) {
    ModernCard {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(ComponentColors.Warning.copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PendingActions,
                        contentDescription = null,
                        tint = ComponentColors.Warning,
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                
                Badge(
                    containerColor = ComponentColors.Warning,
                    contentColor = PureWhite
                ) {
                    Text(requests.size.toString())
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            requests.forEach { request ->
                ModernFriendRequestItem(
                    request = request,
                    showActions = showActions,
                    onAccept = { onAccept(request.id) },
                    onReject = { onReject(request.id) }
                )
                
                if (request != requests.last()) {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 12.dp),
                        color = LightGray.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}

@Composable
private fun ModernFriendRequestItem(
    request: FriendRequest,
    showActions: Boolean,
    onAccept: () -> Unit,
    onReject: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()) }
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Avatar
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(Primary.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = request.senderName.split(" ")
                    .take(2)
                    .joinToString("") { it.firstOrNull()?.uppercase() ?: "" },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Primary
            )
        }
        
        // User info
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = request.senderName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            Text(
                text = request.senderEmail,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = dateFormat.format(Date(request.sentAt)),
                style = MaterialTheme.typography.bodySmall,
                color = TextTertiary
            )
        }
        
        if (showActions) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(
                    onClick = onAccept,
                    modifier = Modifier
                        .size(40.dp)
                        .background(ComponentColors.Success.copy(alpha = 0.1f), CircleShape)
                ) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = "Accept",
                        tint = ComponentColors.Success,
                        modifier = Modifier.size(20.dp)
                    )
                }
                IconButton(
                    onClick = onReject,
                    modifier = Modifier
                        .size(40.dp)
                        .background(ComponentColors.Error.copy(alpha = 0.1f), CircleShape)
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Reject",
                        tint = ComponentColors.Error,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ModernFriendsList(
    friends: List<Friend>,
    searchQuery: String,
    navController: NavController,
    onRemoveFriend: (String) -> Unit,
    onReportUser: (Friend) -> Unit
) {
    val filteredFriends = remember(friends, searchQuery) {
        if (searchQuery.isEmpty()) {
            friends
        } else {
            friends.filter { friend ->
                friend.name.contains(searchQuery, ignoreCase = true) ||
                friend.email.contains(searchQuery, ignoreCase = true)
            }
        }
    }
    
    ModernCard {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(ComponentColors.Success.copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.People,
                        contentDescription = null,
                        tint = ComponentColors.Success,
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                Text(
                    text = "Friends",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                
                Badge(
                    containerColor = ComponentColors.Success,
                    contentColor = PureWhite
                ) {
                    Text(friends.size.toString())
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (filteredFriends.isEmpty()) {
                ModernEmptyFriendsState(
                    hasSearch = searchQuery.isNotEmpty(),
                    searchQuery = searchQuery
                )
            } else {
                filteredFriends.forEach { friend ->
                    ModernFriendItem(
                        friend = friend,
                        navController = navController,
                        onRemove = { onRemoveFriend(friend.id) },
                        onReport = { onReportUser(friend) }
                    )
                    
                    if (friend != filteredFriends.last()) {
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 12.dp),
                            color = LightGray.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ModernEmptyFriendsState(
    hasSearch: Boolean,
    searchQuery: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = if (hasSearch) Icons.Default.SearchOff else Icons.Default.People,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = TextTertiary
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = if (hasSearch) "No friends found" else "No friends yet",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = TextSecondary
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = if (hasSearch) {
                "No friends match \"$searchQuery\""
            } else {
                "Start adding friends to see them here"
            },
            style = MaterialTheme.typography.bodyMedium,
            color = TextTertiary
        )
    }
}

@Composable
private fun ModernFriendItem(
    friend: Friend,
    navController: NavController,
    onRemove: () -> Unit,
    onReport: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                navController.navigate(Screen.FriendProfile.createRoute(friend.id))
            }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Profile Image
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(Primary.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            if (friend.photoUrl != null && friend.photoUrl.isNotEmpty()) {
                AsyncImage(
                    model = friend.photoUrl,
                    contentDescription = "Profile picture of ${friend.name}",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                // Default avatar with initials
                Text(
                    text = friend.name.split(" ")
                        .take(2)
                        .joinToString("") { it.firstOrNull()?.uppercase() ?: "" },
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Primary
                )
            }
        }
        
        // Friend info
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = friend.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = friend.email,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            // Online status indicator (placeholder for future implementation)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(ComponentColors.Success, CircleShape)
                )
                Text(
                    text = "Active",
                    style = MaterialTheme.typography.bodySmall,
                    color = ComponentColors.Success
                )
            }
        }
        
        // Actions menu
        Box {
            IconButton(onClick = { showMenu = !showMenu }) {
                Icon(
                    Icons.Default.MoreVert,
                    contentDescription = "More options",
                    tint = IconTint
                )
            }
            
            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false }
            ) {
                DropdownMenuItem(
                    text = { 
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                tint = Primary
                            )
                            Text("View Profile")
                        }
                    },
                    onClick = {
                        showMenu = false
                        navController.navigate(Screen.FriendProfile.createRoute(friend.id))
                    }
                )
                DropdownMenuItem(
                    text = { 
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = null,
                                tint = ComponentColors.Error
                            )
                            Text("Remove Friend")
                        }
                    },
                    onClick = {
                        showMenu = false
                        onRemove()
                    }
                )
                DropdownMenuItem(
                    text = { 
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.Report,
                                contentDescription = null,
                                tint = ComponentColors.Error
                            )
                            Text("Report User")
                        }
                    },
                    onClick = {
                        showMenu = false
                        onReport()
                    }
                )
            }
        }
    }
}

@Composable
private fun ModernFriendActivityFeed(
    activities: List<FriendActivity>
) {
    val dateFormat = remember { SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()) }
    
    ModernCard {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(ComponentColors.Info.copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Timeline,
                        contentDescription = null,
                        tint = ComponentColors.Info,
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                Text(
                    text = "Recent Activity",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            activities.take(5).forEach { activity ->
                ModernFriendActivityItem(
                    activity = activity,
                    dateFormat = dateFormat
                )
                
                if (activity != activities.take(5).last()) {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 12.dp),
                        color = LightGray.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}

@Composable
private fun ModernFriendActivityItem(
    activity: FriendActivity,
    dateFormat: SimpleDateFormat
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Activity icon
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(
                    when (activity.type) {
                        FriendActivityType.EXPENSE_SHARED -> ComponentColors.Warning.copy(alpha = 0.1f)
                        FriendActivityType.GROUP_JOINED_TOGETHER -> ComponentColors.Success.copy(alpha = 0.1f)
                        FriendActivityType.FRIEND_ADDED -> ComponentColors.Info.copy(alpha = 0.1f)
                        else -> LightGray.copy(alpha = 0.1f)
                    },
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = activity.type.icon,
                fontSize = 18.sp
            )
        }
        
        // Activity content
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = activity.title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )
            Text(
                text = activity.description,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
            if (activity.amount != null) {
                Text(
                    text = "₱${String.format("%.2f", activity.amount)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Primary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
        
        // Timestamp
        Text(
            text = dateFormat.format(activity.timestamp.toDate()),
            style = MaterialTheme.typography.bodySmall,
            color = TextTertiary
        )
    }
}

@Composable
private fun ModernFriendSuggestionsSection(
    suggestions: List<FriendSuggestion>,
    onSendRequest: (FriendSuggestion) -> Unit
) {
    ModernCard {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(Primary.copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Recommend,
                        contentDescription = null,
                        tint = Primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "People You May Know",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Based on mutual groups and friends",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            suggestions.take(3).forEach { suggestion ->
                ModernFriendSuggestionItem(
                    suggestion = suggestion,
                    onSendRequest = { onSendRequest(suggestion) }
                )
                
                if (suggestion != suggestions.take(3).last()) {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 12.dp),
                        color = LightGray.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}

@Composable
private fun ModernFriendSuggestionItem(
    suggestion: FriendSuggestion,
    onSendRequest: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // User avatar
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(Primary.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = suggestion.name.split(" ")
                    .take(2)
                    .joinToString("") { it.firstOrNull()?.uppercase() ?: "" },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Primary
            )
        }
        
        // User info
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = suggestion.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            Text(
                text = suggestion.suggestionReason,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        }
        
        // Add friend button
        OutlinedButton(
            onClick = onSendRequest,
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = Primary,
                containerColor = Primary.copy(alpha = 0.1f)
            )
        ) {
            Icon(
                Icons.Default.PersonAdd,
                contentDescription = "Add Friend",
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Add")
        }
    }
} 

// Friend Groups Management Card
@Composable
private fun FriendGroupsCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    StandardCard(
        onClick = onClick,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(FairrCardDefaults.SPACING)
        ) {
            Box(
                modifier = Modifier
                    .size(FairrCardDefaults.AVATAR_SIZE)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Groups,
                    contentDescription = "Friend Groups",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(FairrCardDefaults.ICON_SIZE)
                )
            }
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Friend Groups",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Organize friends into categories",
                    style = MaterialTheme.typography.bodyMedium,
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
}

// Friend List Item
@Composable
private fun FriendListItem(
    friend: Friend,
    onFriendClick: (Friend) -> Unit,
    onMoreClick: (Friend) -> Unit,
    modifier: Modifier = Modifier
) {
    ListItemCard(
        title = friend.name,
        subtitle = friend.email,
        leading = {
            AsyncImage(
                model = friend.photoUrl,
                contentDescription = "Profile photo of ${friend.name}",
                modifier = Modifier
                    .size(FairrCardDefaults.AVATAR_SIZE)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        },
        trailing = {
            IconButton(onClick = { onMoreClick(friend) }) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "More options",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        onClick = { onFriendClick(friend) },
        modifier = modifier
    )
}

// Friend Request Item
@Composable
private fun FriendRequestItem(
    request: FriendRequest,
    onAccept: (FriendRequest) -> Unit,
    onReject: (FriendRequest) -> Unit,
    modifier: Modifier = Modifier
) {
    ListItemCard(
        title = request.senderName,
        subtitle = request.senderEmail,
        leading = {
            AsyncImage(
                model = request.senderPhotoUrl,
                contentDescription = "Profile photo of ${request.senderName}",
                modifier = Modifier
                    .size(FairrCardDefaults.AVATAR_SIZE)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        },
        trailing = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    onClick = { onAccept(request) },
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Accept request",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(
                    onClick = { onReject(request) },
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Reject request",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        modifier = modifier
    )
} 