package com.example.fairr.ui.screens.camera

import android.Manifest
import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.fairr.ui.theme.*
import com.example.fairr.utils.ExtractedReceiptData
import com.example.fairr.utils.PhotoUtils
import com.example.fairr.utils.ReceiptPhoto
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun PhotoCaptureScreen(
    navController: NavController,
    onPhotosSelected: (List<ReceiptPhoto>) -> Unit = {}
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()
    
    var capturedPhotos by remember { mutableStateOf<List<ReceiptPhoto>>(emptyList()) }
    var isProcessingPhoto by remember { mutableStateOf(false) }
    var showCamera by remember { mutableStateOf(false) }
    var showOcrResults by remember { mutableStateOf(false) }
    var selectedPhotoForOcr by remember { mutableStateOf<ReceiptPhoto?>(null) }
    
    // Permission handling
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)
    
    // Gallery launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            coroutineScope.launch {
                processImageFromUri(context, uri) { receiptPhoto ->
                    capturedPhotos = capturedPhotos + receiptPhoto
                }
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Receipt Photos",
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
                    if (capturedPhotos.isNotEmpty()) {
                        TextButton(
                            onClick = { 
                                onPhotosSelected(capturedPhotos)
                                navController.popBackStack()
                            }
                        ) {
                            Text(
                                "Done (${capturedPhotos.size})",
                                color = DarkGreen,
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBackground)
                .padding(padding)
        ) {
            if (showCamera && cameraPermissionState.status.isGranted) {
                // Simplified camera preview for demo purposes
                // In production, this would be a full CameraX implementation
                SimplifiedCameraPreview(
                    onImageCaptured = { bitmap ->
                        coroutineScope.launch {
                            isProcessingPhoto = true
                            val filename = "receipt_${System.currentTimeMillis()}.jpg"
                            val filePath = PhotoUtils.saveBitmapToInternalStorage(context, bitmap, filename)
                            
                            if (filePath != null) {
                                val receiptPhoto = ReceiptPhoto(filePath = filePath)
                                capturedPhotos = capturedPhotos + receiptPhoto
                                
                                // Process OCR in background
                                PhotoUtils.extractTextFromImage(
                                    bitmap = bitmap,
                                    onSuccess = { extractedData ->
                                        val updatedPhoto = receiptPhoto.copy(extractedData = extractedData)
                                        capturedPhotos = capturedPhotos.map { 
                                            if (it.id == receiptPhoto.id) updatedPhoto else it 
                                        }
                                    },
                                    onFailure = { /* Handle OCR failure silently */ }
                                )
                            }
                            isProcessingPhoto = false
                            showCamera = false
                        }
                    },
                    onBackPressed = { showCamera = false }
                )
            } else {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Header Card
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .shadow(2.dp, RoundedCornerShape(16.dp)),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = NeutralWhite)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.PhotoCamera,
                                contentDescription = "Camera",
                                tint = DarkGreen,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Capture Receipts",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Take photos or select from gallery to auto-extract expense details",
                                fontSize = 14.sp,
                                color = TextSecondary,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                    
                    // Action Buttons
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = {
                                if (cameraPermissionState.status.isGranted) {
                                    showCamera = true
                                } else {
                                    cameraPermissionState.launchPermissionRequest()
                                }
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = DarkGreen),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                Icons.Default.PhotoCamera,
                                contentDescription = "Camera",
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Camera")
                        }
                        
                        OutlinedButton(
                            onClick = { galleryLauncher.launch("image/*") },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = DarkGreen),
                            border = androidx.compose.foundation.BorderStroke(1.dp, DarkGreen),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                Icons.Default.PhotoLibrary,
                                contentDescription = "Gallery",
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Gallery")
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Captured Photos
                    if (capturedPhotos.isNotEmpty()) {
                        Text(
                            text = "Captured Photos (${capturedPhotos.size})",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = NeutralWhite,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(capturedPhotos) { photo ->
                                PhotoCard(
                                    photo = photo,
                                    onRemove = { 
                                        capturedPhotos = capturedPhotos.filter { it.id != photo.id }
                                    },
                                    onShowOcr = {
                                        selectedPhotoForOcr = photo
                                        showOcrResults = true
                                    }
                                )
                            }
                        }
                    } else {
                        // Empty state
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.Receipt,
                                contentDescription = "No photos",
                                modifier = Modifier.size(64.dp),
                                tint = PlaceholderText
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Text(
                                text = "No Photos Captured",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Medium,
                                color = NeutralWhite
                            )
                            
                            Text(
                                text = "Start by taking a photo or selecting from gallery",
                                fontSize = 14.sp,
                                color = PlaceholderText,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                    
                    // Permission rationale
                    if (cameraPermissionState.status.shouldShowRationale) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            colors = CardDefaults.cardColors(containerColor = WarningOrange.copy(alpha = 0.1f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Camera Permission Required",
                                    fontWeight = FontWeight.Medium,
                                    color = WarningOrange
                                )
                                Text(
                                    text = "To capture receipt photos, please grant camera permission.",
                                    fontSize = 14.sp,
                                    color = TextSecondary,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                TextButton(
                                    onClick = { cameraPermissionState.launchPermissionRequest() }
                                ) {
                                    Text("Grant Permission", color = WarningOrange)
                                }
                            }
                        }
                    }
                }
            }
            
            // Loading indicator
            if (isProcessingPhoto) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = NeutralWhite)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(color = DarkGreen)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Processing photo...", color = TextPrimary)
                        }
                    }
                }
            }
        }
    }
    
    // OCR Results Dialog
    if (showOcrResults && selectedPhotoForOcr != null) {
        OcrResultsDialog(
            photo = selectedPhotoForOcr!!,
            onDismiss = { 
                showOcrResults = false
                selectedPhotoForOcr = null
            }
        )
    }
}

@Composable
fun PhotoCard(
    photo: ReceiptPhoto,
    onRemove: () -> Unit,
    onShowOcr: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(120.dp)
            .height(160.dp)
            .shadow(2.dp, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = NeutralWhite)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Photo
            AsyncImage(
                model = File(photo.filePath),
                contentDescription = "Receipt photo",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            
            // Remove button
            IconButton(
                onClick = onRemove,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
                    .size(24.dp)
                    .background(ErrorRed, CircleShape)
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Remove",
                    tint = NeutralWhite,
                    modifier = Modifier.size(16.dp)
                )
            }
            
            // OCR indicator
            if (photo.extractedData != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(4.dp)
                        .background(DarkGreen, RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "OCR",
                        fontSize = 10.sp,
                        color = NeutralWhite,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                IconButton(
                    onClick = onShowOcr,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(4.dp)
                        .size(24.dp)
                        .background(DarkGreen.copy(alpha = 0.8f), CircleShape)
                ) {
                    Icon(
                        Icons.Default.Visibility,
                        contentDescription = "View OCR",
                        tint = NeutralWhite,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun SimplifiedCameraPreview(
    onImageCaptured: (Bitmap) -> Unit,
    onBackPressed: () -> Unit
) {
    // Simplified camera preview for demo purposes
    // In production, this would be a full CameraX implementation
    Box(modifier = Modifier.fillMaxSize()) {
        // Placeholder camera preview
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.PhotoCamera,
                contentDescription = "Camera Preview",
                tint = Color.White,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Camera Preview",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "Tap capture button to simulate photo",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
        }
        
        // Back button
        IconButton(
            onClick = onBackPressed,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
                .background(Color.Black.copy(alpha = 0.5f), CircleShape)
        ) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Color.White
            )
        }
        
        // Capture button
        FloatingActionButton(
            onClick = {
                // Simulate photo capture with a placeholder bitmap
                try {
                    val placeholderBitmap = android.graphics.Bitmap.createBitmap(400, 300, android.graphics.Bitmap.Config.ARGB_8888)
                    // Fill with a simple pattern to make it visible
                    placeholderBitmap.eraseColor(android.graphics.Color.LTGRAY)
                    onImageCaptured(placeholderBitmap)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
                .size(72.dp),
            containerColor = NeutralWhite,
            contentColor = DarkGreen,
            shape = CircleShape
        ) {
            Icon(
                Icons.Default.PhotoCamera,
                contentDescription = "Capture",
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

private suspend fun processImageFromUri(
    context: Context,
    uri: Uri,
    onPhotoProcessed: (ReceiptPhoto) -> Unit
) {
    try {
        val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, uri))
        } else {
            MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
        }
        
        val compressedBitmap = PhotoUtils.compressImage(bitmap)
        val filename = "receipt_${System.currentTimeMillis()}.jpg"
        val filePath = PhotoUtils.saveBitmapToInternalStorage(context, compressedBitmap, filename)
        
        if (filePath != null) {
            val receiptPhoto = ReceiptPhoto(filePath = filePath)
            onPhotoProcessed(receiptPhoto)
            
            // Process OCR
            PhotoUtils.extractTextFromImage(
                bitmap = compressedBitmap,
                onSuccess = { extractedData ->
                    val updatedPhoto = receiptPhoto.copy(extractedData = extractedData)
                    onPhotoProcessed(updatedPhoto)
                },
                onFailure = { /* Handle failure silently */ }
            )
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

@Composable
fun OcrResultsDialog(
    photo: ReceiptPhoto,
    onDismiss: () -> Unit
) {
    val extractedData = photo.extractedData
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Extracted Information") },
        text = {
            Column {
                extractedData?.suggestedAmount?.let { amount ->
                    Text(
                        text = "Suggested Amount: $${String.format("%.2f", amount)}",
                        fontWeight = FontWeight.Medium,
                        color = SuccessGreen
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                
                extractedData?.suggestedDescription?.let { description ->
                    Text(
                        text = "Description: $description",
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                
                extractedData?.extractedDate?.let { date ->
                    Text(
                        text = "Date: $date",
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                
                if (extractedData?.detectedAmounts?.isNotEmpty() == true) {
                    Text(
                        text = "Other amounts found: ${extractedData.detectedAmounts.joinToString(", ") { "$%.2f".format(it) }}",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun PhotoCaptureScreenPreview() {
    FairrTheme {
        PhotoCaptureScreen(navController = rememberNavController())
    }
} 
