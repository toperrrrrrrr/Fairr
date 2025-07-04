package com.example.fairr.ui.screens.profile

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.fairr.ui.theme.*
import com.example.fairr.utils.PhotoUtils
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    navController: NavController,
    onSaveProfile: () -> Unit = {}
) {
    var name by remember { mutableStateOf("John Doe") }
    var email by remember { mutableStateOf("john.doe@example.com") }
    var phone by remember { mutableStateOf("+1 (555) 123-4567") }
    var isLoading by remember { mutableStateOf(false) }
    var hasChanges by remember { mutableStateOf(false) }
    var showImagePicker by remember { mutableStateOf(false) }
    var isUploadingImage by remember { mutableStateOf(false) }
    
    val context = LocalContext.current

    // Track changes
    LaunchedEffect(name, email, phone) {
        hasChanges = true
    }
    
    // Image picker launcher for gallery
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            isUploadingImage = true
            // TODO: Implement actual image upload to Firebase Storage
            // Simulate upload completion
            MainScope().launch {
                delay(2000)
                isUploadingImage = false
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
            // TODO: Implement actual image upload to Firebase Storage
            MainScope().launch {
                delay(2000)
                isUploadingImage = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Edit Profile",
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
                actions = {
                    TextButton(
                        onClick = { 
                            isLoading = true
                            onSaveProfile()
                        },
                        enabled = hasChanges && !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = DarkGreen,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                "Save",
                                color = if (hasChanges) DarkGreen else PlaceholderText,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = NeutralWhite
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(LightBackground)
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            
            // Profile Picture Section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .shadow(2.dp, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = NeutralWhite)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box {
                        // Profile Avatar
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .background(
                                    DarkGreen.copy(alpha = 0.1f),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = name.split(" ").map { it.firstOrNull() ?: "" }.joinToString(""),
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                color = DarkGreen
                            )
                        }
                        
                        // Camera button with functionality
                        Surface(
                            onClick = { showImagePicker = true },
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .size(32.dp),
                            shape = CircleShape,
                            color = DarkGreen,
                            shadowElevation = 2.dp
                        ) {
                            if (isUploadingImage) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        color = NeutralWhite,
                                        strokeWidth = 2.dp
                                    )
                                }
                            } else {
                                Icon(
                                    Icons.Default.CameraAlt,
                                    contentDescription = "Change photo",
                                    tint = NeutralWhite,
                                    modifier = Modifier
                                        .padding(8.dp)
                                        .size(16.dp)
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
                    
                    Text(
                        text = "Tap to change profile picture",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Personal Information Section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .shadow(2.dp, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = NeutralWhite)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "Personal Information",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    // Name field
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { 
                            Text(
                                "Full Name",
                                color = PlaceholderText,
                                fontSize = 14.sp
                            ) 
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = "Name",
                                tint = PlaceholderText
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = DarkGreen,
                            unfocusedBorderColor = PlaceholderText.copy(alpha = 0.3f),
                            focusedLabelColor = DarkGreen
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Email field
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { 
                            Text(
                                "Email Address",
                                color = PlaceholderText,
                                fontSize = 14.sp
                            ) 
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Email,
                                contentDescription = "Email",
                                tint = PlaceholderText
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = DarkGreen,
                            unfocusedBorderColor = PlaceholderText.copy(alpha = 0.3f),
                            focusedLabelColor = DarkGreen
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Phone field
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { 
                            Text(
                                "Phone Number",
                                color = PlaceholderText,
                                fontSize = 14.sp
                            ) 
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Phone,
                                contentDescription = "Phone",
                                tint = PlaceholderText
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = DarkGreen,
                            unfocusedBorderColor = PlaceholderText.copy(alpha = 0.3f),
                            focusedLabelColor = DarkGreen
                        )
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Privacy Information
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .shadow(1.dp, RoundedCornerShape(12.dp)),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = DarkBlue.copy(alpha = 0.05f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "🔒 Privacy Notice",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Your personal information is secure and will only be visible to members of groups you join. We never share your data with third parties.",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        lineHeight = 16.sp
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun EditProfileScreenPreview() {
    FairrTheme {
        EditProfileScreen(
            navController = rememberNavController()
        )
    }
} 
