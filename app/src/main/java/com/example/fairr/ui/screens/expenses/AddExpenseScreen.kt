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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.example.fairr.data.groups.GroupService
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import androidx.compose.material.icons.automirrored.filled.CallSplit
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import android.net.Uri
import com.example.fairr.utils.PhotoUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Suppress("UNUSED_PARAMETER")
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
                    onExpenseAdded()
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Add Expense",
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = IconTint
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BackgroundPrimary
                )
            )
        },
        bottomBar = {
            ModernButton(
                text = "Save Expense",
                onClick = {
                    val trimmedDescription = description.trim()
                    val amountValue = amount.replace(viewModel.getCurrencySymbol(), "")
                        .replace(",", "")
                        .trim()
                        .toDoubleOrNull()

                    if (trimmedDescription.length < 3) {
                        scope.launch {
                            snackbarHostState.showSnackbar("Description must be at least 3 characters")
                        }
                        return@ModernButton
                    }

                    if (amountValue == null || amountValue <= 0.0) {
                        scope.launch {
                            snackbarHostState.showSnackbar("Please enter a valid amount greater than 0")
                        }
                        return@ModernButton
                    }

                    // Validate split-specific data
                    val splitValidationError = validateSplitData(selectedSplitType, amountValue, state.groupMembers.map { it.toMap() })
                    if (splitValidationError != null) {
                        scope.launch {
                            snackbarHostState.showSnackbar(splitValidationError)
                        }
                        return@ModernButton
                    }

                    viewModel.addExpense(
                        groupId = groupId,
                        description = trimmedDescription,
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
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(48.dp)
                    .navigationBarsPadding(),
                enabled = description.isNotBlank() && amount.isNotBlank() && !state.isLoading
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
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Details Section (Description, Category, Split)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Description field
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

                    // Category selection
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Category",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        CategoryChip(
                            category = selectedCategory,
                            selected = true,
                            onClick = { showCategorySheet = true },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Recurrence section
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Recurrence",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Frequency dropdown
                            var expanded by remember { mutableStateOf(false) }
                            Box {
                                OutlinedButton(
                                    onClick = { expanded = true }
                                ) {
                                    Text(recurrenceFrequency.displayName)
                                }
                                DropdownMenu(
                                    expanded = expanded,
                                    onDismissRequest = { expanded = false }
                                ) {
                                    com.example.fairr.data.model.RecurrenceFrequency.values().forEach { freq ->
                                        DropdownMenuItem(
                                            text = { Text(freq.displayName) },
                                            onClick = {
                                                recurrenceFrequency = freq
                                                expanded = false
                                            }
                                        )
                                    }
                                }
                            }
                            // Interval input
                            if (recurrenceFrequency != com.example.fairr.data.model.RecurrenceFrequency.NONE) {
                                OutlinedTextField(
                                    value = recurrenceInterval.toString(),
                                    onValueChange = { val v = it.toIntOrNull(); if (v != null && v > 0) recurrenceInterval = v },
                                    label = { Text("Every") },
                                    singleLine = true,
                                    modifier = Modifier.width(80.dp)
                                )
                                Text(text = recurrenceFrequency.displayName.lowercase())
                            }
                        }
                        // End date picker
                        if (recurrenceFrequency != com.example.fairr.data.model.RecurrenceFrequency.NONE) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(onClick = { showEndDatePicker = true }) {
                                    Text(
                                        text = recurrenceEndDate?.let { java.text.SimpleDateFormat("MMM dd, yyyy").format(it) } ?: "No end date"
                                    )
                                }
                                if (recurrenceEndDate != null) {
                                    IconButton(onClick = { recurrenceEndDate = null }) {
                                        Icon(Icons.Default.Close, contentDescription = "Clear end date")
                                    }
                                }
                            }
                        }
                        // Recurrence summary
                        if (recurrenceFrequency != com.example.fairr.data.model.RecurrenceFrequency.NONE) {
                            val summary = buildString {
                                append("Repeats every $recurrenceInterval ")
                                append(recurrenceFrequency.displayName.lowercase())
                                if (recurrenceEndDate != null) {
                                    append(" until ")
                                    append(java.text.SimpleDateFormat("MMM dd, yyyy").format(recurrenceEndDate!!))
                                }
                            }
                            Text(text = summary, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        }
                    }

                    // Conversational sentence row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Paid by ",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        FilterChip(
                            selected = true,
                            onClick = { showPayerSheet = true },
                            label = { 
                                Text(
                                    selectedPaidBy.lowercase(),
                                    fontWeight = FontWeight.Medium
                                ) 
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Primary.copy(alpha = 0.12f),
                                selectedLabelColor = Primary
                            )
                        )

                        Text(
                            " and split ",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        FilterChip(
                            selected = true,
                            onClick = { showSplitSheet = true },
                            label = { 
                                Text(
                                    when(selectedSplitType) {
                                        "Equal Split" -> "equally"
                                        "Percentage" -> "by %"
                                        "Custom Amount" -> "custom"
                                        else -> selectedSplitType.lowercase()
                                    },
                                    fontWeight = FontWeight.Medium
                                ) 
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Primary.copy(alpha = 0.12f),
                                selectedLabelColor = Primary
                            )
                        )
                    }
                }

                // Calculator Section (Amount)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Amount",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        
                        // Currency Selector
                        var showCurrencyDropdown by remember { mutableStateOf(false) }
                        Box {
                            OutlinedButton(
                                onClick = { showCurrencyDropdown = true },
                                modifier = Modifier.width(100.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = Primary
                                ),
                                border = BorderStroke(1.dp, Primary.copy(alpha = 0.5f))
                            ) {
                                Text(
                                    text = viewModel.getGroupCurrency(),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Icon(
                                    Icons.Default.ArrowDropDown,
                                    contentDescription = "Select Currency",
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            
                            DropdownMenu(
                                expanded = showCurrencyDropdown,
                                onDismissRequest = { showCurrencyDropdown = false }
                            ) {
                                listOf("USD", "EUR", "GBP", "JPY", "PHP", "SGD", "CAD", "AUD").forEach { currency ->
                                    DropdownMenuItem(
                                        text = { 
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Text(
                                                    text = when(currency) {
                                                        "USD" -> "🇺🇸"
                                                        "EUR" -> "🇪🇺"
                                                        "GBP" -> "🇬🇧"
                                                        "JPY" -> "🇯🇵"
                                                        "PHP" -> "🇵🇭"
                                                        "SGD" -> "🇸🇬"
                                                        "CAD" -> "🇨🇦"
                                                        "AUD" -> "🇦🇺"
                                                        else -> "💰"
                                                    },
                                                    fontSize = 16.sp
                                                )
                                                Text(currency)
                                            }
                                        },
                                        onClick = {
                                            viewModel.updateExpenseCurrency(currency)
                                            showCurrencyDropdown = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                    
                    CompactCalculator(
                        value = amount,
                        onValueChange = { amount = it },
                        modifier = Modifier.fillMaxWidth(),
                        viewModel = viewModel
                    )
                }

                // Receipt Photos Section
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Receipt Photos",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    
                    // Add Receipt Button
                    OutlinedButton(
                        onClick = { showPhotoPickerDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Primary
                        ),
                        border = BorderStroke(1.dp, Primary.copy(alpha = 0.5f))
                    ) {
                        Icon(
                            Icons.Default.CameraAlt,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add Receipt Photo")
                    }
                    
                    // Display existing receipt photos
                    if (receiptPhotos.isNotEmpty()) {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(receiptPhotos) { photo ->
                                ReceiptPhotoCard(
                                    photo = photo,
                                    onRemove = {
                                        receiptPhotos = receiptPhotos.filter { it.id != photo.id }
                                    }
                                )
                            }
                        }
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
        modifier = Modifier
            .padding(16.dp)
    )

    // Modal sheet for payer selection
    if (showPayerSheet) {
        ModalBottomSheet(onDismissRequest = { showPayerSheet = false }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Who paid?",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                members.forEach { member ->
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
                                tint = if (member == selectedPaidBy) Primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        trailingContent = {
                            if (member == selectedPaidBy) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = Primary
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedPaidBy = member
                                showPayerSheet = false
                            }
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // Modal sheet for split selection
    if (showSplitSheet) {
        ModalBottomSheet(onDismissRequest = { showSplitSheet = false }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "How to split?",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
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
                            .padding(vertical = 4.dp)
                            .clickable {
                                selectedSplitType = type
                                showSplitSheet = false
                            },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) Primary.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface
                        ),
                        border = if (isSelected) BorderStroke(2.dp, Primary) else null
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
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // Modal sheet for category selection
    if (showCategorySheet) {
        ModalBottomSheet(onDismissRequest = { showCategorySheet = false }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Select Category",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                CategorySelectionGrid(
                    selectedCategory = selectedCategory,
                    onCategorySelected = { category ->
                        selectedCategory = category
                        showCategorySheet = false
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // Photo picker dialog
    if (showPhotoPickerDialog) {
        AlertDialog(
            onDismissRequest = { showPhotoPickerDialog = false },
            title = { Text("Add Receipt Photo") },
            text = { Text("Choose how you want to add a receipt photo") },
            confirmButton = {
                Column {
                    TextButton(
                        onClick = {
                            currentPhotoFile = PhotoUtils.createImageFile(context)
                            cameraLauncher.launch(PhotoUtils.getImageUri(context, currentPhotoFile!!))
                            showPhotoPickerDialog = false
                        }
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Take Photo")
                    }
                    TextButton(
                        onClick = {
                            galleryLauncher.launch("image/*")
                            showPhotoPickerDialog = false
                        }
                    ) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Choose from Gallery")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showPhotoPickerDialog = false }) {
                    Text("Cancel")
                }
            }
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
                TextButton(onClick = {
                    val millis = datePickerState.selectedDateMillis
                    if (millis != null) {
                        recurrenceEndDate = Date(millis)
                    }
                    showEndDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showEndDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
fun CompactCalculator(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AddExpenseViewModel = hiltViewModel()
) {
    var displayValue by remember { mutableStateOf(value) }
    var operation by remember { mutableStateOf<String?>(null) }
    var firstNumber by remember { mutableStateOf<Double?>(null) }
    var shouldResetInput by remember { mutableStateOf(false) }
    val keyboardController = LocalSoftwareKeyboardController.current

    fun hideKeyboard() {
        keyboardController?.hide()
    }

    fun performOperation() {
        if (firstNumber != null && operation != null && displayValue.isNotEmpty()) {
            val secondNumber = displayValue.toDoubleOrNull() ?: return
            val result = when (operation) {
                "+" -> firstNumber!! + secondNumber
                "-" -> firstNumber!! - secondNumber
                "×" -> firstNumber!! * secondNumber
                "÷" -> if (secondNumber != 0.0) firstNumber!! / secondNumber else return
                else -> return
            }
            displayValue = String.format("%.2f", result)
            onValueChange(displayValue)
            firstNumber = null
            operation = null
            shouldResetInput = true
        }
    }

    fun handleOperation(op: String) {
        hideKeyboard()
        if (displayValue.isEmpty()) return
        if (firstNumber != null) {
            performOperation()
        }
        firstNumber = displayValue.toDoubleOrNull()
        operation = op
        shouldResetInput = true
    }

    Column(
        modifier = modifier.padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Display
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Primary.copy(alpha = 0.05f),
                    RoundedCornerShape(8.dp)
                )
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.End
            ) {
                if (firstNumber != null && operation != null) {
                    Text(
                        text = "${viewModel.getCurrencySymbol()}${String.format("%.2f", firstNumber)} $operation",
                        fontSize = 14.sp,
                        color = TextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                }
                Text(
                    text = if (displayValue.isNotEmpty()) "${viewModel.getCurrencySymbol()} $displayValue" else "${viewModel.getCurrencySymbol()} 0.00",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }
        }

        // Number pad grid with operations
        val buttons = listOf(
            listOf("7", "8", "9", "÷"),
            listOf("4", "5", "6", "×"),
            listOf("1", "2", "3", "-"),
            listOf(".", "0", "⌫", "+")
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            buttons.forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    row.forEach { button ->
                        val isOperation = button in listOf("+", "-", "×", "÷")
                        val isDelete = button == "⌫"
                        
                        TextButton(
                            onClick = {
                                hideKeyboard()
                                when {
                                    isOperation -> handleOperation(button)
                                    isDelete -> {
                                        if (displayValue.isNotEmpty()) {
                                            displayValue = displayValue.dropLast(1)
                                            onValueChange(displayValue)
                                        }
                                    }
                                    button == "." -> {
                                        if (!displayValue.contains(".")) {
                                            displayValue += button
                                            onValueChange(displayValue)
                                        }
                                    }
                                    else -> {
                                        if (shouldResetInput) {
                                            displayValue = button
                                            shouldResetInput = false
                                        } else {
                                            displayValue += button
                                        }
                                        onValueChange(displayValue)
                                    }
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp)
                                .background(
                                    when {
                                        isOperation -> Primary.copy(alpha = 0.1f)
                                        isDelete -> ErrorRed.copy(alpha = 0.1f)
                                        else -> MaterialTheme.colorScheme.surface
                                    },
                                    RoundedCornerShape(8.dp)
                                ),
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = when {
                                    isOperation -> Primary
                                    isDelete -> ErrorRed
                                    else -> TextPrimary
                                }
                            )
                        ) {
                            Text(
                                text = button,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // Equal button
            TextButton(
                onClick = { 
                    hideKeyboard()
                    performOperation() 
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .background(Primary, RoundedCornerShape(8.dp)),
                colors = ButtonDefaults.textButtonColors(
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = "=",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
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
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier
            .size(80.dp)
            .shadow(1.dp, RoundedCornerShape(8.dp)),
        shape = RoundedCornerShape(8.dp),
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
                    .padding(2.dp)
                    .size(20.dp)
                    .background(ErrorRed, CircleShape)
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Remove",
                    tint = NeutralWhite,
                    modifier = Modifier.size(12.dp)
                )
            }
            
            // OCR indicator
            if (photo.extractedData != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(2.dp)
                        .background(DarkGreen, RoundedCornerShape(3.dp))
                        .padding(horizontal = 4.dp, vertical = 1.dp)
                ) {
                    Text(
                        text = "OCR",
                        fontSize = 8.sp,
                        color = NeutralWhite,
                        fontWeight = FontWeight.Bold
                    )
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

