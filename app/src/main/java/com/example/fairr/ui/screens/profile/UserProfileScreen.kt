package com.example.fairr.ui.screens.profile

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.automirrored.filled.ContactSupport
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.fairr.ui.components.*
import com.example.fairr.ui.components.dialogs.GDPRAccountDeletionDialog
import com.example.fairr.ui.theme.*
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.fairr.ui.viewmodels.ProfileViewModel
import com.example.fairr.utils.PhotoUtils
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import com.example.fairr.ui.components.ErrorType
import com.example.fairr.ui.components.ErrorUtils
import com.example.fairr.ui.components.StandardErrorState

data class UserProfile(
    val id: String,
    val fullName: String,
    val email: String,
    val profileImageUrl: String? = null,
    val phoneNumber: String? = null,
    val joinDate: String,
    val totalGroups: Int = 0,
    val totalExpenses: Int = 0,
    val isEmailVerified: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen(
    navController: NavController,
    userId: String? = null,
    onProfileUpdated: () -> Unit = {},
    profileViewModel: ProfileViewModel = hiltViewModel()
) {
    var isEditing by remember { mutableStateOf(false) }
    var showChangePasswordDialog by remember { mutableStateOf(false) }
    var showDeleteAccountDialog by remember { mutableStateOf(false) }
    var showSuccessMessage by remember { mutableStateOf(false) }
    var successMessage by remember { mutableStateOf("") }
    
    // Use real data from ProfileViewModel
    val userState by profileViewModel.userState.collectAsState()
    
    // Convert ProfileViewModel's UserState to UserProfile for existing components
    val userProfile = remember(userState) {
        UserProfile(
            id = userId ?: "current",
            fullName = userState.displayName ?: "User",
            email = userState.email ?: "",
            profileImageUrl = userState.photoUrl,
            phoneNumber = userState.phoneNumber,
            joinDate = userState.joinDate ?: "Recently",
            totalGroups = userState.totalGroups,
            totalExpenses = userState.totalExpenses,
            isEmailVerified = userState.isEmailVerified
        )
    }
    
    val scrollState = rememberScrollState()

    // Show loading state
    if (userState.isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = Primary)
        }
        return
    }

    // Show error state
    userState.error?.let { errorMessage ->
        StandardErrorState(
            errorType = ErrorUtils.getErrorType(errorMessage),
            customMessage = ErrorUtils.getUserFriendlyMessage(errorMessage),
            onRetry = { profileViewModel.refreshUserData() },
            modifier = Modifier.fillMaxSize()
        )
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        if (isEditing) "Edit Profile" else "Profile",
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = { 
                        if (isEditing) {
                            isEditing = false
                        } else {
                            navController.popBackStack()
                        }
                    }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary
                        )
                    }
                },
                actions = {
                    if (!isEditing) {
                        IconButton(onClick = { isEditing = true }) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = "Edit Profile",
                                tint = DarkGreen
                            )
                        }
                    } else {
                        TextButton(
                            onClick = { 
                                isEditing = false
                                successMessage = "Profile updated successfully"
                                showSuccessMessage = true
                                onProfileUpdated() // Call the callback when profile is updated
                            }
                        ) {
                            Text("Save", color = DarkGreen, fontWeight = FontWeight.Medium)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = NeutralWhite
                )
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(LightBackground)
                    .padding(padding)
                    .verticalScroll(scrollState)
            ) {
                // Profile Header
                ProfileHeaderCard(
                    userProfile = userProfile,
                    isEditing = isEditing,
                    onProfileUpdated = { updatedProfile ->
                        // Refresh user data to get updated info from Firebase
                        profileViewModel.refreshUserData()
                        successMessage = "Profile updated successfully"
                        showSuccessMessage = true
                    }
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Account Settings
                if (!isEditing) {
                    AccountSettingsSection(
                        onChangePassword = { showChangePasswordDialog = true },
                        onNotificationSettings = { /* Navigate to notification settings */ },
                        onPrivacySettings = { navController.navigate("privacy_policy") },
                        onDeleteAccount = { showDeleteAccountDialog = true }
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // App Settings
                    AppSettingsSection(
                        onThemeSettings = { /* Navigate to theme settings */ },
                        onLanguageSettings = { /* Navigate to language settings */ },
                        onCurrencySettings = { navController.navigate("currency_selection") },
                        onExportData = { navController.navigate("export_data") }
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // About & Support
                    AboutSection(
                        onHelpSupport = { navController.navigate("help_support") },
                        onAboutApp = { /* Navigate to about */ },
                        onTermsPrivacy = { navController.navigate("privacy_policy") },
                        onContactUs = { navController.navigate("contact_support") }
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Logout Button
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .clickable { /* Handle logout */ },
                        colors = CardDefaults.cardColors(containerColor = NeutralWhite)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.Logout,
                                contentDescription = "Logout",
                                tint = ErrorRed,
                                modifier = Modifier.size(24.dp)
                            )
                            
                            Spacer(modifier = Modifier.width(16.dp))
                            
                            Text(
                                text = "Logout",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = ErrorRed
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
            
            // Success Message
            FairrSuccessMessage(
                message = successMessage,
                isVisible = showSuccessMessage,
                onDismiss = { showSuccessMessage = false },
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    }
    
    // Change Password Dialog
    if (showChangePasswordDialog) {
        ChangePasswordDialog(
            onDismiss = { showChangePasswordDialog = false },
            onPasswordChanged = {
                showChangePasswordDialog = false
                successMessage = "Password changed successfully"
                showSuccessMessage = true
            }
        )
    }
    
    // Delete Account Dialog
    if (showDeleteAccountDialog) {
        GDPRAccountDeletionDialog(
            onDismiss = { showDeleteAccountDialog = false },
            onAccountDeleted = {
                showDeleteAccountDialog = false
                // Navigate back to auth screen
                navController.navigate("auth") {
                    popUpTo(0) { inclusive = true }
                }
            }
        )
    }
}

@Composable
fun ProfileHeaderCard(
    userProfile: UserProfile,
    isEditing: Boolean,
    onProfileUpdated: (UserProfile) -> Unit,
    modifier: Modifier = Modifier
) {
    var editedName by remember { mutableStateOf(userProfile.fullName) }
    var editedPhone by remember { mutableStateOf(userProfile.phoneNumber ?: "") }
    var showImagePicker by remember { mutableStateOf(false) }
    var isUploadingImage by remember { mutableStateOf(false) }
    
    val context = LocalContext.current
    
    // Image picker launcher for gallery
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { selectedUri ->
            isUploadingImage = true
            // TODO: Implement actual image upload to Firebase Storage
            // For now, we'll simulate the upload
            val imageFile = PhotoUtils.createImageFile(context)
            try {
                // Compress and save the image
                val compressedBitmap = PhotoUtils.compressImage(context, selectedUri)
                compressedBitmap?.let { bitmap ->
                    PhotoUtils.saveBitmapToFile(bitmap, imageFile)
                    // Simulate upload completion
                    MainScope().launch {
                        kotlinx.coroutines.delay(2000) // Simulate upload time
                        isUploadingImage = false
                        onProfileUpdated(userProfile.copy(profileImageUrl = selectedUri.toString()))
                    }
                }
            } catch (e: Exception) {
                isUploadingImage = false
                // Handle error
            }
        }
    }
    
    // Camera launcher
    val cameraImageFile = remember { PhotoUtils.createImageFile(context) }
    val cameraUri = remember { PhotoUtils.getImageUri(context, cameraImageFile) }
    
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            isUploadingImage = true
            try {
                // Compress the captured image
                val compressedBitmap = PhotoUtils.compressImageFromFile(cameraImageFile)
                compressedBitmap?.let { bitmap ->
                    PhotoUtils.saveBitmapToFile(bitmap, cameraImageFile)
                    // Simulate upload completion
                    MainScope().launch {
                        kotlinx.coroutines.delay(2000) // Simulate upload time
                        isUploadingImage = false
                        onProfileUpdated(userProfile.copy(profileImageUrl = cameraUri.toString()))
                    }
                }
            } catch (e: Exception) {
                isUploadingImage = false
                // Handle error
            }
        }
    }

    ModernCard(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile Image with upload functionality
            Box(
                modifier = Modifier.size(100.dp),
                contentAlignment = Alignment.Center
            ) {
                if (userProfile.profileImageUrl != null) {
                    AsyncImage(
                        model = userProfile.profileImageUrl,
                        contentDescription = "Profile Image",
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .border(3.dp, DarkGreen, CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .background(DarkGreen, CircleShape)
                            .border(3.dp, DarkGreen.copy(alpha = 0.3f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = userProfile.fullName.split(" ").map { it.first() }.joinToString(""),
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeutralWhite
                        )
                    }
                }
                
                // Loading overlay for image upload
                if (isUploadingImage) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .background(Color.Black.copy(alpha = 0.5f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = NeutralWhite,
                            strokeWidth = 2.dp
                        )
                    }
                }
                
                if (isEditing && !isUploadingImage) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(DarkGreen, CircleShape)
                            .align(Alignment.BottomEnd)
                            .clickable { showImagePicker = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.CameraAlt,
                            contentDescription = "Change Photo",
                            tint = NeutralWhite,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
            
            // Image picker dialog
            if (showImagePicker) {
                AlertDialog(
                    onDismissRequest = { showImagePicker = false },
                    title = { Text("Change Profile Picture") },
                    text = { Text("Choose how you'd like to update your profile picture") },
                    confirmButton = {
                        TextButton(onClick = {
                            showImagePicker = false
                            galleryLauncher.launch("image/*")
                        }) {
                            Text("Gallery")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = {
                            showImagePicker = false
                            cameraLauncher.launch(cameraUri)
                        }) {
                            Text("Camera")
                        }
                    }
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (isEditing) {
                // Editable fields
                OutlinedTextField(
                    value = editedName,
                    onValueChange = { editedName = it },
                    label = { Text("Full Name") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DarkGreen,
                        focusedLabelColor = DarkGreen
                    )
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                OutlinedTextField(
                    value = editedPhone,
                    onValueChange = { editedPhone = it },
                    label = { Text("Phone Number") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DarkGreen,
                        focusedLabelColor = DarkGreen
                    )
                )
            } else {
                // Display mode
                Text(
                    text = userProfile.fullName,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = userProfile.email,
                        fontSize = 14.sp,
                        color = TextSecondary
                    )
                    
                    if (userProfile.isEmailVerified) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            Icons.Default.Verified,
                            contentDescription = "Verified",
                            tint = SuccessGreen,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                
                userProfile.phoneNumber?.let { phone ->
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = phone,
                        fontSize = 14.sp,
                        color = TextSecondary
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Stats Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatItem(
                        label = "Groups",
                        value = userProfile.totalGroups.toString(),
                        icon = Icons.Default.Group
                    )
                    
                    StatItem(
                        label = "Expenses",
                        value = userProfile.totalExpenses.toString(),
                        icon = Icons.Default.Receipt
                    )
                    
                    StatItem(
                        label = "Since",
                        value = userProfile.joinDate,
                        icon = Icons.Default.DateRange
                    )
                }
            }
        }
    }
}

@Composable
fun StatItem(
    label: String,
    value: String,
    icon: ImageVector
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            icon,
            contentDescription = label,
            tint = DarkGreen,
            modifier = Modifier.size(20.dp)
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = value,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary
        )
        
        Text(
            text = label,
            fontSize = 12.sp,
            color = TextSecondary
        )
    }
}

@Composable
fun AccountSettingsSection(
    onChangePassword: () -> Unit,
    onNotificationSettings: () -> Unit,
    onPrivacySettings: () -> Unit,
    onDeleteAccount: () -> Unit
) {
    SettingsSection(
        title = "Account",
        items = listOf(
            SettingsItem("Change Password", Icons.Default.Lock, onChangePassword),
            SettingsItem("Notification Settings", Icons.Default.Notifications, onNotificationSettings),
            SettingsItem("Privacy Settings", Icons.Default.Security, onPrivacySettings),
            SettingsItem("Delete Account", Icons.Default.DeleteForever, onDeleteAccount, isDestructive = true)
        )
    )
}

@Composable
fun AppSettingsSection(
    onThemeSettings: () -> Unit,
    onLanguageSettings: () -> Unit,
    onCurrencySettings: () -> Unit,
    onExportData: () -> Unit
) {
    SettingsSection(
        title = "App Settings",
        items = listOf(
            SettingsItem("Theme", Icons.Default.Palette, onThemeSettings),
            SettingsItem("Language", Icons.Default.Language, onLanguageSettings),
            SettingsItem("Currency", Icons.Default.AttachMoney, onCurrencySettings),
            SettingsItem("Export Data", Icons.Default.Download, onExportData)
        )
    )
}

@Composable
fun AboutSection(
    onHelpSupport: () -> Unit,
    onAboutApp: () -> Unit,
    onTermsPrivacy: () -> Unit,
    onContactUs: () -> Unit
) {
    SettingsSection(
        title = "About & Support",
        items = listOf(
            SettingsItem("Help & Support", Icons.AutoMirrored.Filled.Help, onHelpSupport),
            SettingsItem("About Fairr", Icons.Default.Info, onAboutApp),
            SettingsItem("Terms & Privacy", Icons.Default.Policy, onTermsPrivacy),
            SettingsItem("Contact Us", Icons.AutoMirrored.Filled.ContactSupport, onContactUs)
        )
    )
}

data class SettingsItem(
    val title: String,
    val icon: ImageVector,
    val onClick: () -> Unit,
    val isDestructive: Boolean = false
)

@Composable
fun SettingsSection(
    title: String,
    items: List<SettingsItem>
) {
    Column {
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            colors = CardDefaults.cardColors(containerColor = NeutralWhite)
        ) {
            Column {
                items.forEachIndexed { index, item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { item.onClick() }
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            item.icon,
                            contentDescription = item.title,
                            tint = if (item.isDestructive) ErrorRed else TextSecondary,
                            modifier = Modifier.size(24.dp)
                        )
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        Text(
                            text = item.title,
                            fontSize = 16.sp,
                            color = if (item.isDestructive) ErrorRed else TextPrimary,
                            modifier = Modifier.weight(1f)
                        )
                        
                        Icon(
                            Icons.Default.ChevronRight,
                            contentDescription = "Navigate",
                            tint = PlaceholderText,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    
                    if (index < items.size - 1) {
                        HorizontalDivider(
                            color = PlaceholderText.copy(alpha = 0.1f),
                            modifier = Modifier.padding(horizontal = 20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ChangePasswordDialog(
    onDismiss: () -> Unit,
    onPasswordChanged: () -> Unit
) {
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showCurrentPassword by remember { mutableStateOf(false) }
    var showNewPassword by remember { mutableStateOf(false) }
    var showConfirmPassword by remember { mutableStateOf(false) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Change Password",
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
        },
        text = {
            Column {
                OutlinedTextField(
                    value = currentPassword,
                    onValueChange = { currentPassword = it },
                    label = { Text("Current Password") },
                    visualTransformation = if (showCurrentPassword) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { showCurrentPassword = !showCurrentPassword }) {
                            Icon(
                                if (showCurrentPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = "Toggle password visibility"
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = { Text("New Password") },
                    visualTransformation = if (showNewPassword) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { showNewPassword = !showNewPassword }) {
                            Icon(
                                if (showNewPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = "Toggle password visibility"
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Confirm New Password") },
                    visualTransformation = if (showConfirmPassword) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { showConfirmPassword = !showConfirmPassword }) {
                            Icon(
                                if (showConfirmPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = "Toggle password visibility"
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (newPassword == confirmPassword && newPassword.isNotEmpty()) {
                        onPasswordChanged()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = DarkGreen),
                enabled = newPassword == confirmPassword && newPassword.length >= 6 && currentPassword.isNotEmpty()
            ) {
                Text("Change Password", color = NeutralWhite)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        },
        containerColor = NeutralWhite
    )
}

@Preview(showBackground = true)
@Composable
fun UserProfileScreenPreview() {
    FairrTheme {
        UserProfileScreen(navController = rememberNavController())
    }
} 
