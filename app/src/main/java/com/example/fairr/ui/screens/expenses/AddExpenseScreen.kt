package com.example.fairr.ui.screens.expenses

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import com.example.fairr.ui.screens.categories.ExpenseCategory
import com.example.fairr.ui.screens.categories.getDefaultCategories
import com.example.fairr.ui.theme.*
import com.example.fairr.utils.ReceiptPhoto
import com.example.fairr.util.CurrencyFormatter
import java.io.File
import java.util.*
import kotlinx.coroutines.launch
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.navigationBarsPadding

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
    val categories = remember { getDefaultCategories() }
    var selectedCategory by remember { mutableStateOf(categories.first()) }
    var selectedSplitType by remember { mutableStateOf("Equal Split") }
    var showCategoryDropdown by remember { mutableStateOf(false) }
    var receiptPhotos by remember { mutableStateOf<List<ReceiptPhoto>>(emptyList()) }
    var showOcrSuggestion by remember { mutableStateOf(false) }
    var suggestedData by remember { mutableStateOf<com.example.fairr.utils.ExtractedReceiptData?>(null) }
    val state = viewModel.state
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    
    val splitTypes = listOf("Equal Split", "Percentage", "Custom Amount")

    // Auto-fill from OCR data when photos are added
    LaunchedEffect(receiptPhotos) {
        val latestPhotoWithOcr = receiptPhotos.lastOrNull { it.extractedData != null }
        if (latestPhotoWithOcr?.extractedData != null && description.isBlank() && amount.isBlank()) {
            suggestedData = latestPhotoWithOcr.extractedData
            showOcrSuggestion = true
        }
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
                    val amountValue = amount.replace(viewModel.getCurrencySymbol(), "")
                        .replace(",", "")
                        .trim()
                        .toDoubleOrNull()
                    
                    if (amountValue == null) {
                        scope.launch {
                            snackbarHostState.showSnackbar("Please enter a valid amount")
                        }
                        return@ModernButton
                    }
                    
                    viewModel.addExpense(
                        groupId = groupId,
                        description = description,
                        amount = amountValue,
                        date = Date()
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
                verticalArrangement = Arrangement.spacedBy(8.dp)
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

                // Category Dropdown
                ExposedDropdownMenuBox(
                    expanded = showCategoryDropdown,
                    onExpandedChange = { showCategoryDropdown = it }
                ) {
                    OutlinedTextField(
                        value = selectedCategory.name,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category", fontSize = 14.sp) },
                        leadingIcon = {
                            Icon(
                                selectedCategory.icon,
                                contentDescription = null,
                                tint = selectedCategory.color,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showCategoryDropdown) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Primary,
                            focusedLabelColor = Primary
                        )
                    )
                    
                    ExposedDropdownMenu(
                        expanded = showCategoryDropdown,
                        onDismissRequest = { showCategoryDropdown = false }
                    ) {
                        categories.forEach { category ->
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            category.icon,
                                            contentDescription = null,
                                            tint = category.color,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Text(category.name)
                                    }
                                },
                                onClick = {
                                    selectedCategory = category
                                    showCategoryDropdown = false
                                }
                            )
                        }
                    }
                }

                // Split Type Selection
                Column {
                    Text(
                        text = "Split Type",
                        fontSize = 14.sp,
                        color = TextSecondary
                    )
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
                    ) {
                        splitTypes.forEach { type ->
                            FilterChip(
                                selected = selectedSplitType == type,
                                onClick = { selectedSplitType = type },
                                label = { Text(type, fontSize = 14.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Primary,
                                    selectedLabelColor = PureWhite
                                )
                            )
                        }
                    }
                }
            }

            // Calculator Section (Amount)
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Amount",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                CompactCalculator(
                    value = amount,
                    onValueChange = { amount = it },
                    modifier = Modifier.fillMaxWidth()
                )
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
}

@Composable
fun CompactCalculator(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var displayValue by remember { mutableStateOf(value) }
    var operation by remember { mutableStateOf<String?>(null) }
    var firstNumber by remember { mutableStateOf<Double?>(null) }
    var shouldResetInput by remember { mutableStateOf(false) }

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
                        text = "₱${String.format("%.2f", firstNumber)} $operation",
                        fontSize = 14.sp,
                        color = TextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                }
                Text(
                    text = if (displayValue.isNotEmpty()) "₱ $displayValue" else "₱ 0.00",
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
                                .height(48.dp)
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
                onClick = { performOperation() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
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

