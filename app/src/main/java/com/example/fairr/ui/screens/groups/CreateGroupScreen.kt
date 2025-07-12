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
    var showAddMemberDialog by remember { mutableStateOf(false) }
    var newMemberEmail by remember { mutableStateOf("") }

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
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Header Section
                    item {
                        ModernCard(
                            backgroundColor = Primary,
                            cornerRadius = 20
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(24.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(80.dp)
                                        .background(
                                            color = PureWhite.copy(alpha = 0.2f),
                                            shape = RoundedCornerShape(20.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Group,
                                        contentDescription = "Group",
                                        tint = PureWhite,
                                        modifier = Modifier.size(40.dp)
                                    )
                                }
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                Text(
                                    text = "Create Your Group",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = PureWhite
                                )
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                Text(
                                    text = "Set up a group to start sharing expenses with friends and family",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = PureWhite.copy(alpha = 0.9f),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }

                    // Group Details Section
                    item {
                        ModernCard(
                            cornerRadius = 16
                        ) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Text(
                                    text = "Group Details",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary
                                )
                                
                                // Group name field
                                val groupNameValidation = remember(viewModel.groupName) {
                                    ValidationUtils.validateGroupName(viewModel.groupName)
                                }
                                val isGroupNameError = groupNameValidation is ValidationResult.Error && viewModel.groupName.isNotBlank()
                                
                                ModernTextField(
                                    value = viewModel.groupName,
                                    onValueChange = viewModel::onGroupNameChange,
                                    label = "Group Name",
                                    leadingIcon = Icons.Default.Group,
                                    errorMessage = if (isGroupNameError) {
                                        (groupNameValidation as ValidationResult.Error).message
                                    } else null
                                )

                                // Description field
                                ModernTextField(
                                    value = viewModel.groupDescription,
                                    onValueChange = viewModel::onGroupDescriptionChange,
                                    label = "Description (Optional)",
                                    leadingIcon = Icons.Default.Description
                                )
                            }
                        }
                    }

                    // Group Avatar Section
                    item {
                        ModernCard(
                            cornerRadius = 16
                        ) {
                            GroupAvatarPicker(
                                selectedAvatar = viewModel.groupAvatar,
                                onAvatarSelected = viewModel::onGroupAvatarChange,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    // Currency Section
                    item {
                        ModernCard(
                            cornerRadius = 16
                        ) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = "Currency",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary
                                )
                                
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Group Currency",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = TextSecondary
                                    )
                                    
                                    Text(
                                        text = com.example.fairr.util.CurrencyFormatter.getSymbol(viewModel.groupCurrency),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Primary
                                    )
                                }
                                
                                Text(
                                    text = "All expenses in this group will be tracked in ${com.example.fairr.util.CurrencyFormatter.getSymbol(viewModel.groupCurrency)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextTertiary
                                )
                            }
                        }
                    }

                    // Members Section
                    item {
                        ModernCard(
                            cornerRadius = 16
                        ) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "Invite Members",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = TextPrimary
                                        )
                                        Text(
                                            text = "${viewModel.members.size} member${if (viewModel.members.size != 1) "s" else ""}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TextSecondary
                                        )
                                    }
                                    
                                    OutlinedButton(
                                        onClick = { showAddMemberDialog = true },
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            contentColor = Primary
                                        ),
                                        border = androidx.compose.foundation.BorderStroke(
                                            1.dp, Primary
                                        ),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.PersonAdd,
                                            contentDescription = "Add Member",
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Add Member")
                                    }
                                }
                                
                                if (viewModel.members.isEmpty()) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 32.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Icon(
                                                Icons.Default.People,
                                                contentDescription = "No members",
                                                modifier = Modifier.size(48.dp),
                                                tint = TextTertiary
                                            )
                                            Spacer(modifier = Modifier.height(12.dp))
                                            Text(
                                                text = "No members yet",
                                                style = MaterialTheme.typography.bodyLarge,
                                                color = TextSecondary
                                            )
                                            Text(
                                                text = "Add members to start sharing expenses",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = TextTertiary
                                            )
                                        }
                                    }
                                } else {
                                    LazyColumn(
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        items(viewModel.members) { member ->
                                            MemberCard(
                                                member = member,
                                                onRemove = { viewModel.removeMember(member) }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Create Group Button
                    item {
                        val groupNameValidation = remember(viewModel.groupName) {
                            ValidationUtils.validateGroupName(viewModel.groupName)
                        }
                        val isValidToSubmit = groupNameValidation is ValidationResult.Success
                        
                        ModernButton(
                            text = "Create Group",
                            onClick = { viewModel.createGroup() },
                            enabled = isValidToSubmit,
                            icon = Icons.Default.Group,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                        )
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

    if (showAddMemberDialog) {
        val keyboardController = LocalSoftwareKeyboardController.current
        
        AlertDialog(
            onDismissRequest = { 
                keyboardController?.hide()
                showAddMemberDialog = false 
            },
            title = { 
                Text(
                    "Invite Member",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
            },
            text = {
                Column {
                    Text(
                        text = "Enter the email address of the person you want to invite. They will receive an invitation to join this group.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    val emailValidation = remember(newMemberEmail) {
                        ValidationUtils.validateEmail(newMemberEmail)
                    }
                    val isEmailError = emailValidation is ValidationResult.Error && newMemberEmail.isNotBlank()
                    
                    ModernTextField(
                        value = newMemberEmail,
                        onValueChange = { newMemberEmail = it },
                        label = "Email",
                        leadingIcon = Icons.Default.Email,
                        keyboardType = KeyboardType.Email,
                        errorMessage = if (isEmailError) {
                            (emailValidation as ValidationResult.Error).message
                        } else null
                    )
                }
            },
            confirmButton = {
                val emailValidation = remember(newMemberEmail) {
                    ValidationUtils.validateEmail(newMemberEmail)
                }
                val isEmailValid = emailValidation is ValidationResult.Success
                
                TextButton(
                    onClick = {
                        keyboardController?.hide()
                        if (newMemberEmail.isNotBlank() && isEmailValid) {
                            viewModel.addMember(
                                GroupMember(
                                    id = "temp_${System.currentTimeMillis()}",
                                    name = newMemberEmail.substringBefore("@"),
                                    email = newMemberEmail
                                )
                            )
                            newMemberEmail = ""
                            showAddMemberDialog = false
                        }
                    },
                    enabled = isEmailValid && newMemberEmail.isNotBlank()
                ) {
                    Text("Invite")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { 
                        keyboardController?.hide()
                        showAddMemberDialog = false 
                    }
                ) {
                    Text("Cancel")
                }
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
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.height(200.dp)
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
