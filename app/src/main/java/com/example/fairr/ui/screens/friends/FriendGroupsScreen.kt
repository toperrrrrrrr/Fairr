package com.example.fairr.ui.screens.friends

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.fairr.data.model.FriendGroup
import com.example.fairr.data.friends.FriendGroupService
import com.example.fairr.data.friends.FriendGroupResult
import com.example.fairr.ui.components.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FriendGroupsUiState(
    val friendGroups: List<FriendGroup> = emptyList(),
    val groupMemberCounts: Map<String, Int> = emptyMap(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class FriendGroupsViewModel @Inject constructor(
    private val friendGroupService: FriendGroupService
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(FriendGroupsUiState())
    val uiState: StateFlow<FriendGroupsUiState> = _uiState.asStateFlow()
    
    var snackbarMessage by mutableStateOf<String?>(null)
        private set
    
    init {
        loadFriendGroups()
        createDefaultGroupsIfNeeded()
    }
    
    private fun createDefaultGroupsIfNeeded() {
        viewModelScope.launch {
            friendGroupService.createDefaultGroups()
        }
    }
    
    fun loadFriendGroups() {
        viewModelScope.launch {
            friendGroupService.getFriendGroups()
                .onStart {
                    _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                }
                .catch { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load friend groups"
                    )
                }
                .collect { groups ->
                    // Get member counts for each group
                    val memberCounts = groups.associate { group ->
                        group.id to group.memberIds.size
                    }
                    
                    _uiState.value = _uiState.value.copy(
                        friendGroups = groups,
                        groupMemberCounts = memberCounts,
                        isLoading = false,
                        error = null
                    )
                }
        }
    }
    
    fun createFriendGroup(name: String, description: String, color: String, emoji: String) {
        viewModelScope.launch {
            when (val result = friendGroupService.createFriendGroup(name, description, color, emoji)) {
                is FriendGroupResult.Success -> {
                    snackbarMessage = result.message
                }
                is FriendGroupResult.Error -> {
                    snackbarMessage = result.message
                }
            }
        }
    }
    
    fun updateFriendGroup(groupId: String, name: String, description: String, color: String, emoji: String) {
        viewModelScope.launch {
            when (val result = friendGroupService.updateFriendGroup(groupId, name, description, color, emoji)) {
                is FriendGroupResult.Success -> {
                    snackbarMessage = result.message
                }
                is FriendGroupResult.Error -> {
                    snackbarMessage = result.message
                }
            }
        }
    }
    
    fun deleteFriendGroup(groupId: String) {
        viewModelScope.launch {
            when (val result = friendGroupService.deleteFriendGroup(groupId)) {
                is FriendGroupResult.Success -> {
                    snackbarMessage = result.message
                }
                is FriendGroupResult.Error -> {
                    snackbarMessage = result.message
                }
            }
        }
    }
    
    fun clearSnackbarMessage() {
        snackbarMessage = null
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendGroupsScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: FriendGroupsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    
    // State for dialogs
    var showCreateDialog by remember { mutableStateOf(false) }
    var editingGroup by remember { mutableStateOf<FriendGroup?>(null) }
    var deletingGroup by remember { mutableStateOf<FriendGroup?>(null) }
    
    // Handle snackbar messages
    LaunchedEffect(viewModel.snackbarMessage) {
        viewModel.snackbarMessage?.let { message ->
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Short
            )
            viewModel.clearSnackbarMessage()
        }
    }
    
    // Create Group Dialog
    if (showCreateDialog) {
        CreateFriendGroupDialog(
            onConfirm = { name, description, color, emoji ->
                viewModel.createFriendGroup(name, description, color, emoji)
                showCreateDialog = false
            },
            onDismiss = { showCreateDialog = false }
        )
    }
    
    // Edit Group Dialog
    editingGroup?.let { group ->
        EditFriendGroupDialog(
            group = group,
            onConfirm = { name, description, color, emoji ->
                viewModel.updateFriendGroup(group.id, name, description, color, emoji)
                editingGroup = null
            },
            onDismiss = { editingGroup = null }
        )
    }
    
    // Delete Confirmation Dialog
    deletingGroup?.let { group ->
        AlertDialog(
            onDismissRequest = { deletingGroup = null },
            title = {
                Text(
                    text = "Delete Friend Group?",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text("Are you sure you want to delete \"${group.name}\"? This action cannot be undone.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteFriendGroup(group.id)
                        deletingGroup = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingGroup = null }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Friend Groups",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Create Group"
                )
            }
        },
        snackbarHost = {
            SnackbarHost(snackbarHostState)
        },
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        FairrLoadingCard()
                    }
                }
                
                uiState.error != null -> {
                    ErrorState(
                        message = uiState.error ?: "Unknown error occurred",
                        onRetry = { viewModel.loadFriendGroups() },
                        modifier = Modifier.fillMaxSize()
                    )
                }
                
                uiState.friendGroups.isEmpty() -> {
                    EmptyFriendGroupsState(
                        onCreateGroup = { showCreateDialog = true },
                        modifier = Modifier.fillMaxSize()
                    )
                }
                
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(vertical = 16.dp)
                    ) {
                        item {
                            Text(
                                text = "Organize your friends into groups for easier management",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }
                        
                        items(
                            items = uiState.friendGroups,
                            key = { it.id }
                        ) { group ->
                            FriendGroupCard(
                                group = group,
                                memberCount = uiState.groupMemberCounts[group.id] ?: 0,
                                onEdit = { editingGroup = group },
                                onDelete = { deletingGroup = group },
                                onManageMembers = { 
                                    // Navigate to group member management screen
                                    navController.navigate("friend_group_members/${group.id}")
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyFriendGroupsState(
    onCreateGroup: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "👥",
                style = MaterialTheme.typography.displayMedium
            )
            
            Text(
                text = "No Friend Groups Yet",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            
            Text(
                text = "Create groups to organize your friends by family, work, school, or any category you like.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Button(onClick = onCreateGroup) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Create Your First Group")
            }
        }
    }
}

@Composable
private fun ErrorState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "❌",
                style = MaterialTheme.typography.displayMedium
            )
            
            Text(
                text = "Error Loading Groups",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Button(onClick = onRetry) {
                Text("Retry")
            }
        }
    }
} 