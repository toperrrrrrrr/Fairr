package com.example.fairr.ui.screens.expenses

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.fairr.ui.components.*
import com.example.fairr.ui.theme.*
import com.example.fairr.utils.ReceiptPhoto
import com.example.fairr.util.CurrencyFormatter
import java.io.File
import java.util.*
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.ExperimentalUnitApi
import com.example.fairr.data.model.RecurrenceRule
import com.example.fairr.data.model.RecurrenceFrequency
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.rememberDatePickerState
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import androidx.compose.material.icons.automirrored.filled.CallSplit
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import android.net.Uri
import com.example.fairr.utils.PhotoUtils
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseScreen(
    groupId: String,
    navController: NavController,
    onExpenseAdded: () -> Unit = {},
    viewModel: AddExpenseViewModel = hiltViewModel()
) {
    var description by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var selectedSplitType by remember { mutableStateOf("Equal Split") }
    var receiptPhotos by remember { mutableStateOf<List<ReceiptPhoto>>(emptyList()) }
    var showOcrSuggestion by remember { mutableStateOf(false) }
    var suggestedData by remember { mutableStateOf<com.example.fairr.utils.ExtractedReceiptData?>(null) }
    var selectedPaidBy by remember { mutableStateOf("You") }
    var selectedCategory by remember { mutableStateOf(com.example.fairr.data.model.ExpenseCategory.FOOD) }
    // Recurrence state
    var recurrenceFrequency by remember { mutableStateOf(com.example.fairr.data.model.RecurrenceFrequency.NONE) }
    var recurrenceInterval by remember { mutableStateOf(1) }
    var recurrenceEndDate by remember { mutableStateOf<Date?>(null) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    var showCalculatorSheet by remember { mutableStateOf(false) }
    val state = viewModel.state
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    
    val splitTypes = listOf("Equal Split", "Percentage", "Custom Amount")
    val members = state.groupMembers.map { it.displayName }.ifEmpty { listOf("You") }
    var showPayerSheet by remember { mutableStateOf(false) }
    var showSplitSheet by remember { mutableStateOf(false) }
    var showCategorySheet by remember { mutableStateOf(false) }
    var showPhotoPickerDialog by remember { mutableStateOf(false) }

    // Photo capture functionality
    val context = LocalContext.current
    var currentPhotoFile by remember { mutableStateOf<File?>(null) }
    
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && currentPhotoFile != null) {
            currentPhotoFile?.let { file ->
                val photo = ReceiptPhoto(filePath = file.absolutePath)
                receiptPhotos = receiptPhotos + photo
                
                // Process OCR in background
                scope.launch {
                    try {
                        val bitmap = PhotoUtils.loadBitmapFromPath(file.absolutePath)
                        if (bitmap != null) {
                            PhotoUtils.extractTextFromImage(
                                bitmap = bitmap,
                                onSuccess = { extractedData ->
                                    receiptPhotos = receiptPhotos.map { 
                                        if (it.filePath == file.absolutePath) {
                                            it.copy(extractedData = extractedData)
                                        } else it
                                    }
                                },
                                onFailure = { /* OCR failed, continue without it */ }
                            )
                        }
                    } catch (e: Exception) {
                        // OCR processing failed, continue without it
                    }
                }
            }
        }
    }
    
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { selectedUri ->
            try {
                val inputStream = context.contentResolver.openInputStream(selectedUri)
                val file = PhotoUtils.createImageFile(context)
                val outputStream = java.io.FileOutputStream(file)
                
                inputStream?.use { input ->
                    outputStream.use { output ->
                        input.copyTo(output)
                    }
                }
                
                val photo = ReceiptPhoto(filePath = file.absolutePath)
                receiptPhotos = receiptPhotos + photo
                
                // Process OCR in background
                scope.launch {
                    try {
                        val bitmap = PhotoUtils.loadBitmapFromPath(file.absolutePath)
                        if (bitmap != null) {
                            PhotoUtils.extractTextFromImage(
                                bitmap = bitmap,
                                onSuccess = { extractedData ->
                                    receiptPhotos = receiptPhotos.map { 
                                        if (it.filePath == file.absolutePath) {
                                            it.copy(extractedData = extractedData)
                                        } else it
                                    }
                                },
                                onFailure = { /* OCR failed, continue without it */ }
                            )
                        }
                    } catch (e: Exception) {
                        // OCR processing failed, continue without it
                    }
                }
            } catch (e: Exception) {
                scope.launch {
                    snackbarHostState.showSnackbar("Failed to load image from gallery")
                }
            }
        }
    }

    // Auto-fill from OCR data when photos are added
    LaunchedEffect(receiptPhotos) {
        val latestPhotoWithOcr = receiptPhotos.lastOrNull { it.extractedData != null }
        if (latestPhotoWithOcr?.extractedData != null && description.isBlank() && amount.isBlank()) {
            suggestedData = latestPhotoWithOcr.extractedData
            showOcrSuggestion = true
        }
    }

    // Load group members when screen opens
    LaunchedEffect(groupId) {
        viewModel.loadGroupMembers(groupId)
        viewModel.loadGroupCurrency(groupId)
    }

    // Handle events
    LaunchedEffect(true) {
        viewModel.events.collect { event ->
            when (event) {
                is AddExpenseEvent.ShowError -> {
                    snackbarHostState.showSnackbar(event.message)
                }
                AddExpenseEvent.ExpenseSaved -> {
                    onExpenseAdded() // Notify parent to refresh
                    navController.popBackStack() // Navigate back immediately
                }
            }
        }
    }

    Scaffold(
        snackbarHost = {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                SnackbarHost(
                    hostState = snackbarHostState,
                    modifier = Modifier.padding(bottom = 16.dp),
                    snackbar = { data ->
                        Snackbar(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            containerColor = Primary,
                            contentColor = Color.White,
                            content = {
                                Text(
                                    text = data.visuals.message,
                                    style = MaterialTheme.typography.bodyMedium,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        )
                    }
                )
            }
        },
        topBar = {
            TopAppBar(
                title = { Text("Add Expense") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            // Convert amount string to Double
                            val amountValue = amount.toDoubleOrNull() ?: 0.0
                            viewModel.addExpense(
                                groupId = groupId,
                                description = description,
                                amount = amountValue,
                                date = Date(),
                                paidBy = viewModel.getMemberIdByDisplayName(selectedPaidBy),
                                splitType = selectedSplitType,
                                category = selectedCategory,
                                isRecurring = recurrenceFrequency != com.example.fairr.data.model.RecurrenceFrequency.NONE,
                                recurrenceRule = if (recurrenceFrequency != com.example.fairr.data.model.RecurrenceFrequency.NONE)
                                    buildRecurrenceRule(recurrenceFrequency.displayName, recurrenceInterval, recurrenceEndDate?.time)
                                else null,
                                receiptPhotos = receiptPhotos
                            )
                        },
                        enabled = description.isNotBlank() && amount.isNotBlank()
                    ) {
                        Text("Save", color = if (description.isNotBlank() && amount.isNotBlank()) Primary else Color.Gray)
                    }
                }
            )
        }
    ) { padding ->
        KeyboardDismissibleBox {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(BackgroundSecondary)
                    .padding(padding)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // --- Top Row: Receipt, Recurrence, Category ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Receipt Button
                    OutlinedButton(
                        onClick = { showPhotoPickerDialog = true },
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Receipt", fontSize = 13.sp)
                    }
                    // Recurrence Button
                    OutlinedButton(
                        onClick = { /* Open recurrence modal/section */ showEndDatePicker = true },
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Recurrence", fontSize = 13.sp)
                    }
                    // Category Button
                    OutlinedButton(
                        onClick = { showCategorySheet = true },
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        Icon(Icons.Default.Category, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Category", fontSize = 13.sp)
                    }
                }

                // --- Description Field ---
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description", fontSize = 14.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = LocalTextStyle.current.copy(fontSize = 16.sp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Primary,
                        focusedLabelColor = Primary
                    )
                )

                // --- Amount Field as Calculator Trigger ---
                OutlinedButton(
                    onClick = { showCalculatorSheet = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(53.dp), // Reduced from 56.dp by ~5%
                    border = BorderStroke(1.dp, Primary.copy(alpha = 0.5f)),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = when(viewModel.getGroupCurrency()) {
                                    "USD" -> "$"
                                    "EUR" -> "€"
                                    "GBP" -> "£"
                                    "JPY" -> "¥"
                                    "PHP" -> "₱"
                                    "SGD" -> "S$"
                                    "CAD" -> "C$"
                                    "AUD" -> "A$"
                                    else -> "$"
                                },
                                fontSize = 18.sp,
                                color = Primary,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = if (amount.isBlank()) "Enter Amount" else amount,
                                fontSize = 18.sp,
                                color = if (amount.isBlank()) TextSecondary else TextPrimary
                            )
                        }
                        Icon(
                            Icons.Default.Calculate,
                            contentDescription = "Calculator",
                            tint = Primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // --- Paid By and Split By Buttons ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { showPayerSheet = true },
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Paid by: $selectedPaidBy", fontSize = 13.sp)
                    }
                    OutlinedButton(
                        onClick = { showSplitSheet = true },
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.CallSplit, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Split: $selectedSplitType", fontSize = 13.sp)
                    }
                }
            }
        }
    }
    
    if (state.isLoading) {
        FairrLoadingDialog(
            isVisible = true,
            message = "Saving expense...",
            onDismiss = { /* Do nothing, let the ViewModel control this */ }
        )
    }
    
    // Snackbar host
    SnackbarHost(
        hostState = snackbarHostState,
        modifier = Modifier.padding(16.dp)
    )

    // Modal sheet for payer selection
    if (showPayerSheet) {
        ModalBottomSheet(
            onDismissRequest = { showPayerSheet = false },
            dragHandle = { BottomSheetDefaults.DragHandle() }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .navigationBarsPadding()
            ) {
                Text(
                    text = "Who paid?",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
                members.forEach { member ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedPaidBy = member
                                showPayerSheet = false
                            },
                        color = if (member == selectedPaidBy) 
                            Primary.copy(alpha = 0.08f) 
                        else 
                            MaterialTheme.colorScheme.surface
                    ) {
                        ListItem(
                            headlineContent = { 
                                Text(
                                    member,
                                    style = MaterialTheme.typography.bodyLarge
                                ) 
                            },
                            leadingContent = {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = null,
                                    tint = if (member == selectedPaidBy) 
                                        Primary 
                                    else 
                                        MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(24.dp)
                                )
                            },
                            trailingContent = {
                                if (member == selectedPaidBy) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = Primary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            },
                            modifier = Modifier.heightIn(min = 56.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Modal sheet for split selection
    if (showSplitSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSplitSheet = false },
            dragHandle = { BottomSheetDefaults.DragHandle() }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .navigationBarsPadding()
            ) {
                Text(
                    text = "How to split?",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
                
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    splitTypes.forEach { type ->
                        val isSelected = type == selectedSplitType
                        val splitDescription = when(type) {
                            "Equal Split" -> "Split the expense equally among all members"
                            "Percentage" -> "Split by percentage (e.g., 50%, 30%, 20%)"
                            "Custom Amount" -> "Set specific amounts for each person"
                            else -> "Custom split method"
                        }
                        
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 72.dp)
                                .clickable {
                                    selectedSplitType = type
                                    showSplitSheet = false
                                },
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) 
                                    Primary.copy(alpha = 0.08f) 
                                else 
                                    MaterialTheme.colorScheme.surface
                            ),
                            border = if (isSelected) 
                                BorderStroke(2.dp, Primary) 
                            else 
                                BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Icon with background
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .background(
                                            if (isSelected) Primary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        when(type) {
                                            "Equal Split" -> Icons.Default.Group
                                            "Percentage" -> Icons.Default.Percent
                                            "Custom Amount" -> Icons.Default.Calculate
                                            else -> Icons.AutoMirrored.Filled.CallSplit
                                        },
                                        contentDescription = null,
                                        tint = if (isSelected) Primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                
                                Spacer(modifier = Modifier.width(16.dp))
                                
                                // Content
                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = type,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) Primary else MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = splitDescription,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(top = 2.dp)
                                    )
                                }
                                
                                // Selection indicator
                                if (isSelected) {
                                    Icon(
                                        Icons.Default.CheckCircle,
                                        contentDescription = "Selected",
                                        tint = Primary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // Modal sheet for category selection
    if (showCategorySheet) {
        ModalBottomSheet(
            onDismissRequest = { showCategorySheet = false },
            dragHandle = { BottomSheetDefaults.DragHandle() }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .navigationBarsPadding()
            ) {
                Text(
                    text = "Select Category",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
                
                CategorySelectionGrid(
                    selectedCategory = selectedCategory,
                    onCategorySelected = { category ->
                        selectedCategory = category
                        showCategorySheet = false
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                )
                
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Photo picker dialog
    if (showPhotoPickerDialog) {
        AlertDialog(
            onDismissRequest = { showPhotoPickerDialog = false },
            title = { 
                Text(
                    "Add Receipt Photo",
                    style = MaterialTheme.typography.titleLarge
                )
            },
            text = { 
                Text(
                    "Choose how you want to add a receipt photo",
                    style = MaterialTheme.typography.bodyLarge
                )
            },
            confirmButton = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    FilledTonalButton(
                        onClick = {
                            currentPhotoFile = PhotoUtils.createImageFile(context)
                            currentPhotoFile?.let { photoFile ->
                                cameraLauncher.launch(PhotoUtils.getImageUri(context, photoFile))
                                showPhotoPickerDialog = false
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                Icons.Default.CameraAlt,
                                contentDescription = "Camera",
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                "Take Photo",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                    FilledTonalButton(
                        onClick = {
                            galleryLauncher.launch("image/*")
                            showPhotoPickerDialog = false
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                Icons.Default.PhotoLibrary,
                                contentDescription = "Gallery",
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                "Choose from Gallery",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showPhotoPickerDialog = false },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp)
                ) {
                    Text(
                        "Cancel",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            },
            properties = DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = true
            )
        )
    }

    // Date picker for recurrence end date
    if (showEndDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = recurrenceEndDate?.time,
            initialDisplayedMonthMillis = recurrenceEndDate?.time
        )
        DatePickerDialog(
            onDismissRequest = { showEndDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val millis = datePickerState.selectedDateMillis
                        if (millis != null) {
                            recurrenceEndDate = Date(millis)
                        }
                        showEndDatePicker = false
                    },
                    modifier = Modifier.heightIn(min = 48.dp)
                ) { 
                    Text(
                        "OK",
                        style = MaterialTheme.typography.labelLarge
                    ) 
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showEndDatePicker = false },
                    modifier = Modifier.heightIn(min = 48.dp)
                ) { 
                    Text(
                        "Cancel",
                        style = MaterialTheme.typography.labelLarge
                    ) 
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // --- Calculator Modal Sheet ---
    if (showCalculatorSheet) {
        ModalBottomSheet(
            onDismissRequest = { showCalculatorSheet = false },
            dragHandle = { BottomSheetDefaults.DragHandle() },
            windowInsets = WindowInsets(0),
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .navigationBarsPadding(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Currency symbol and amount display
                Text(
                    text = "${when(viewModel.getGroupCurrency()) {
                        "USD" -> "$"
                        "EUR" -> "€"
                        "GBP" -> "£"
                        "JPY" -> "¥"
                        "PHP" -> "₱"
                        "SGD" -> "S$"
                        "CAD" -> "C$"
                        "AUD" -> "A$"
                        else -> "$"
                    }} ${if (amount.isBlank()) "0.00" else amount}",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.End
                )

                CompactCalculator(
                    value = amount,
                    onValueChange = { amount = it },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun CompactCalculator(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var displayValue by remember { mutableStateOf(value) }
    var expression by remember { mutableStateOf("") }
    var lastWasOperation by remember { mutableStateOf(false) }

    fun calculateExpression(expr: String): String {
        try {
            val sanitizedExpr = expr
                .replace("×", "*")
                .replace("÷", "/")
            
            // Split the expression into numbers and operators
            val numbers = sanitizedExpr.split("""[-+*/]""".toRegex()).filter { it.isNotEmpty() }
            val operators = sanitizedExpr.replace("""[0-9.]""".toRegex(), "").toList()
            
            if (numbers.isEmpty()) return "0"
            
            var result = numbers[0].toDouble()
            var opIndex = 0
            
            for (i in 1 until numbers.size) {
                val num = numbers[i].toDouble()
                when (operators.getOrNull(opIndex++)) {
                    '+' -> result += num
                    '-' -> result -= num
                    '*' -> result *= num
                    '/' -> if (num != 0.0) result /= num else return "Error"
                }
            }
            
            return String.format("%.2f", result)
        } catch (e: Exception) {
            return "Error"
        }
    }

    LaunchedEffect(value) {
        if (value != displayValue) {
            displayValue = value
            expression = value
        }
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Display current expression
        Text(
            text = expression.ifEmpty { "0" },
            style = MaterialTheme.typography.titleLarge,
            color = TextPrimary,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            textAlign = TextAlign.End
        )

        val buttons = listOf(
            listOf("7", "8", "9", "÷"),
            listOf("4", "5", "6", "×"),
            listOf("1", "2", "3", "-"),
            listOf(".", "0", "⌫", "+")
        )

        buttons.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                row.forEach { button ->
                    val isOperation = button in listOf("+", "-", "×", "÷")
                    val isDelete = button == "⌫"
                    
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .height(42.dp),
                        shape = RoundedCornerShape(8.dp),
                        color = when {
                            isOperation -> Primary.copy(alpha = 0.1f)
                            isDelete -> MaterialTheme.colorScheme.errorContainer
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        },
                        onClick = {
                            when {
                                isOperation -> {
                                    if (!lastWasOperation && expression.isNotEmpty()) {
                                        expression += button
                                        lastWasOperation = true
                                    }
                                }
                                isDelete -> {
                                    if (expression.isNotEmpty()) {
                                        expression = expression.dropLast(1)
                                        lastWasOperation = expression.lastOrNull()?.toString()
                                            ?.matches("""[-+×÷]""".toRegex()) ?: false
                                    }
                                }
                                button == "." -> {
                                    val lastNumber = expression.split("""[-+×÷]""".toRegex()).last()
                                    if (!lastNumber.contains(".")) {
                                        expression += if (lastNumber.isEmpty()) "0." else "."
                                        lastWasOperation = false
                                    }
                                }
                                else -> {
                                    expression += button
                                    lastWasOperation = false
                                }
                            }
                        }
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = button,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = when {
                                        isOperation -> FontWeight.Bold
                                        isDelete -> FontWeight.Medium
                                        else -> FontWeight.Normal
                                    },
                                    color = when {
                                        isOperation -> Primary
                                        isDelete -> MaterialTheme.colorScheme.error
                                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                                    }
                                )
                            )
                        }
                    }
                }
            }
        }

        // Equals button
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(42.dp),
            shape = RoundedCornerShape(8.dp),
            color = Primary,
            onClick = { 
                if (expression.isNotEmpty()) {
                    val result = calculateExpression(expression)
                    expression = result
                    displayValue = result
                    onValueChange(result)
                    lastWasOperation = false
                }
            }
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "=",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AddExpenseScreenPreview() {
    FairrTheme {
        AddExpenseScreen(
            navController = rememberNavController(),
            groupId = "1"
        )
    }
}

@Composable
fun ReceiptPhotoCard(
    photo: ReceiptPhoto,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .size(100.dp)
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(12.dp),
                spotColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Photo
            AsyncImage(
                model = File(photo.filePath),
                contentDescription = "Receipt photo",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            
            // Semi-transparent overlay at the top
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(32.dp)
                    .align(Alignment.TopCenter)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.4f),
                                Color.Transparent
                            )
                        )
                    )
            )
            
            // Remove button with ripple effect
            IconButton(
                onClick = onRemove,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
                    .size(28.dp)
                    .background(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = CircleShape
                    )
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Remove photo",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(16.dp)
                )
            }
            
            // OCR indicator with improved visibility
            if (photo.extractedData != null) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(4.dp),
                    shape = RoundedCornerShape(4.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.9f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Text(
                            text = "OCR",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        )
                    }
                }
            }
        }
    }
}

// Extension function to convert MemberInfo to Map for validation
fun MemberInfo.toMap(): Map<String, Any> {
    return mapOf(
        "userId" to userId,
        "displayName" to displayName,
        "percentage" to 0.0, // Default percentage for validation
        "customAmount" to 0.0 // Default custom amount for validation
    )
}

fun validateSplitData(splitType: String, amount: Double, memberSplits: List<Map<String, Any>>): String? {
    return when (splitType) {
        "Percentage" -> {
            val totalPercentage = memberSplits.sumOf { (it["percentage"] as? Number)?.toDouble() ?: 0.0 }
            when {
                totalPercentage < 99.9 -> "Total percentage must be 100% (currently ${String.format("%.1f", totalPercentage)}%)"
                totalPercentage > 100.1 -> "Total percentage cannot exceed 100% (currently ${String.format("%.1f", totalPercentage)}%)"
                else -> null
            }
        }
        "Custom Amount" -> {
            val totalCustomAmount = memberSplits.sumOf { (it["customAmount"] as? Number)?.toDouble() ?: 0.0 }
            when {
                totalCustomAmount < amount * 0.99 -> "Total custom amounts must equal the expense amount (currently ${String.format("%.2f", totalCustomAmount)} vs ${String.format("%.2f", amount)})"
                totalCustomAmount > amount * 1.01 -> "Total custom amounts cannot exceed the expense amount (currently ${String.format("%.2f", totalCustomAmount)} vs ${String.format("%.2f", amount)})"
                else -> null
            }
        }
        else -> null
    }
}

private fun buildRecurrenceRule(frequency: String, interval: Int, endDate: Long?): RecurrenceRule {
    val freq = when (frequency) {
        "Daily" -> RecurrenceFrequency.DAILY
        "Weekly" -> RecurrenceFrequency.WEEKLY
        "Monthly" -> RecurrenceFrequency.MONTHLY
        "Yearly" -> RecurrenceFrequency.YEARLY
        else -> RecurrenceFrequency.WEEKLY
    }
    
    val endTimestamp = endDate?.let { com.google.firebase.Timestamp(java.util.Date(it)) }
    
    return RecurrenceRule(
        frequency = freq,
        interval = interval,
        endDate = endTimestamp
    )
} 

