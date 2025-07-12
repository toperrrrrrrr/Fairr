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
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.material3.SnackbarHostState
import kotlinx.coroutines.launch
import com.example.fairr.ui.components.LoadingSpinner
import com.example.fairr.ui.components.KeyboardDismissibleBox
import com.example.fairr.ui.components.*
import com.example.fairr.navigation.Screen
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.items
import com.example.fairr.ui.components.EmojiPickerItem
import com.example.fairr.ui.components.GroupEmojiAvatar
import com.example.fairr.ui.components.EmojiCollections
import com.example.fairr.util.ValidationUtils
import com.example.fairr.util.ValidationResult

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateGroupScreen(
    navController: NavController,
    viewModel: CreateGroupViewModel = hiltViewModel()
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var showEmojiPicker by remember { mutableStateOf(false) }
    val availableCurrencies = listOf("PHP", "USD", "EUR", "JPY") // Add more as needed

    LaunchedEffect(Unit) {
        viewModel.navigationEvents.collect { groupId ->
            navController.navigate(Screen.GroupDetail.createRoute(groupId)) {
                popUpTo(Screen.CreateGroup.route) { 
                    inclusive = true
                }
            }
        }
    }

    // Handle user messages from ViewModel
    LaunchedEffect(Unit) {
        viewModel.userMessage.collect { message ->
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Short
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Create Group",
                        style = MaterialTheme.typography.headlineSmall,
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
                    containerColor = BackgroundPrimary
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = BackgroundPrimary
    ) { paddingValues ->
        KeyboardDismissibleBox {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Row for emoji avatar and group name
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Small emoji avatar button
                        IconButton(
                            onClick = { showEmojiPicker = true },
                            modifier = Modifier.size(48.dp)
                        ) {
                            GroupEmojiAvatar(
                                avatar = viewModel.groupAvatar,
                                groupName = viewModel.groupName.ifBlank { "G" },
                                size = 40.dp
                            )
                        }
                        // Group name text box
                        ModernTextField(
                            value = viewModel.groupName,
                            onValueChange = viewModel::onGroupNameChange,
                            label = "Group Name",
                            leadingIcon = Icons.Default.Group,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    // Description text box
                    ModernTextField(
                        value = viewModel.groupDescription,
                        onValueChange = viewModel::onGroupDescriptionChange,
                        label = "Description",
                        leadingIcon = Icons.Default.Description
                    )
                    // Currency selector (DropdownMenu)
                    var expanded by remember { mutableStateOf(false) }
                    Box {
                        OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
                            Text(viewModel.groupCurrency)
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        }
                        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            availableCurrencies.forEach { currency ->
                                DropdownMenuItem(
                                    text = { Text(currency) },
                                    onClick = {
                                        viewModel.onGroupCurrencyChange(currency)
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                    // Invite members section
                    Text(
                        text = "Invite members",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ModernTextField(
                            value = viewModel.newMemberEmail,
                            onValueChange = viewModel::onNewMemberEmailChange,
                            label = "Email",
                            leadingIcon = Icons.Default.Email,
                            keyboardType = KeyboardType.Email,
                            modifier = Modifier.weight(1f)
                        )
                        Button(
                            onClick = {
                                if (ValidationUtils.validateEmail(viewModel.newMemberEmail) is ValidationResult.Success) {
                                    viewModel.addMember(
                                        GroupMember(
                                            id = "temp_${System.currentTimeMillis()}",
                                            name = viewModel.newMemberEmail.substringBefore("@"),
                                            email = viewModel.newMemberEmail
                                        )
                                    )
                                    viewModel.onNewMemberEmailChange("")
                                } else {
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Invalid email address")
                                    }
                                }
                            },
                            enabled = viewModel.newMemberEmail.isNotBlank()
                        ) {
                            Text("Invite")
                        }
                    }
                    // List of invited members (future offline functionality)
                    if (viewModel.members.isNotEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            viewModel.members.forEach { member ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(member.email, modifier = Modifier.weight(1f))
                                    IconButton(onClick = { viewModel.removeMember(member) }) {
                                        Icon(Icons.Default.Close, contentDescription = "Remove")
                                    }
                                }
                            }
                        }
                    }
                    // Spacer to push the button to the bottom
                    Spacer(modifier = Modifier.weight(1f))
                    // Create group button
                    Button(
                        onClick = { viewModel.createGroup() },
                        enabled = viewModel.groupName.isNotBlank(),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Create Group")
                    }
                }
            }

            when (val state = viewModel.uiState) {
                is CreateGroupUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        ModernLoadingCard(
                            title = "Creating Group",
                            subtitle = "Please wait while we set up your group...",
                            modifier = Modifier.padding(32.dp)
                        )
                    }
                }
                is CreateGroupUiState.Error -> {
                    LaunchedEffect(state) {
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                message = state.message,
                                duration = SnackbarDuration.Short
                            )
                        }
                    }
                }
                is CreateGroupUiState.Success -> {
                    LaunchedEffect(state) {
                        scope.launch {
                            val message = if (state.invitesSent > 0) {
                                "Group created successfully! ${state.invitesSent} invite(s) sent."
                            } else {
                                "Group created successfully!"
                            }
                            snackbarHostState.showSnackbar(
                                message = message,
                                duration = SnackbarDuration.Short
                            )
                        }
                    }
                    
                    // Show success banner
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        ModernSuccessBanner(
                            message = if (state.invitesSent > 0) {
                                "Group created successfully! ${state.invitesSent} invite(s) sent."
                            } else {
                                "Group created successfully!"
                            },
                            onDismiss = { /* Auto-dismiss after navigation */ }
                        )
                    }
                }
                else -> {}
            }
        }
    }

    // Emoji picker dialog
    if (showEmojiPicker) {
        EmojiPickerDialog(
            onDismiss = { showEmojiPicker = false },
            onEmojiSelected = {
                viewModel.onGroupAvatarChange(it)
                showEmojiPicker = false
            }
        )
    }
}

@Composable
private fun MemberCard(
    member: GroupMember,
    onRemove: () -> Unit
) {
    ModernCard(
        backgroundColor = BackgroundSecondary,
        cornerRadius = 12
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
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            color = Primary.copy(alpha = 0.1f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = "Member",
                        tint = Primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column {
                    Text(
                        text = member.name,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )
                    Text(
                        text = member.email,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
            
            IconButton(
                onClick = onRemove,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Remove Member",
                    tint = ComponentColors.Error,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun GroupAvatarPicker(
    selectedAvatar: String,
    onAvatarSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val emojiOptions = EmojiCollections.groupEmojis
    
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Group Avatar",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary
        )
        
        // Avatar preview
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            GroupEmojiAvatar(
                avatar = selectedAvatar,
                groupName = "Group",
                size = 60.dp
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Text(
                text = if (selectedAvatar.isNotEmpty()) "Selected avatar" else "Choose an emoji for your group",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        }
        
        // Emoji grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(8),
            contentPadding = PaddingValues(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(emojiOptions) { emoji ->
                EmojiPickerItem(
                    emoji = emoji,
                    isSelected = selectedAvatar == emoji,
                    onClick = { onAvatarSelected(emoji) },
                    size = 40.dp
                )
            }
        }
        
        // Debug info (can be removed later)
        Text(
            text = "Available emojis: ${emojiOptions.size}",
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
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
