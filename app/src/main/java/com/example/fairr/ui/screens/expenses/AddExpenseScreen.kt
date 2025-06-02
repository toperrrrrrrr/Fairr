package com.example.fairr.ui.screens.expenses

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
import coil.compose.AsyncImage
import com.example.fairr.ui.components.*
import com.example.fairr.ui.screens.categories.ExpenseCategory
import com.example.fairr.ui.screens.categories.getDefaultCategories
import com.example.fairr.ui.theme.*
import com.example.fairr.utils.ReceiptPhoto
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Suppress("UNUSED_PARAMETER")
fun AddExpenseScreen(
    navController: NavController,
    groupId: String,
    onExpenseAdded: () -> Unit = {}
) {
    var description by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    val categories = remember { getDefaultCategories() }
    var selectedCategory by remember { mutableStateOf(categories.first()) }
    var selectedSplitType by remember { mutableStateOf("Equal Split") }
    var isLoading by remember { mutableStateOf(false) }
    var showCategoryDropdown by remember { mutableStateOf(false) }
    var receiptPhotos by remember { mutableStateOf<List<ReceiptPhoto>>(emptyList()) }
    var showOcrSuggestion by remember { mutableStateOf(false) }
    var suggestedData by remember { mutableStateOf<com.example.fairr.utils.ExtractedReceiptData?>(null) }
    
    val splitTypes = listOf("Equal Split", "Percentage", "Custom Amount")

    // Auto-fill from OCR data when photos are added
    LaunchedEffect(receiptPhotos) {
        val latestPhotoWithOcr = receiptPhotos.lastOrNull { it.extractedData != null }
        if (latestPhotoWithOcr?.extractedData != null && description.isBlank() && amount.isBlank()) {
            suggestedData = latestPhotoWithOcr.extractedData
            showOcrSuggestion = true
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
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundSecondary)
                .padding(padding)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))
            
            // Receipt Photos Section
            ModernCard(
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Receipt Photos",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    
                    ModernButton(
                        text = "Add Photo",
                        onClick = { 
                            navController.navigate("photo_capture")
                        },
                        icon = Icons.Default.PhotoCamera,
                        modifier = Modifier.height(40.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                if (receiptPhotos.isNotEmpty()) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
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
                } else {
                    // Empty state
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { 
                                navController.navigate("photo_capture")
                            }
                            .background(
                                Primary.copy(alpha = 0.05f),
                                RoundedCornerShape(12.dp)
                            )
                            .padding(20.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.PhotoCamera,
                            contentDescription = "Add Photo",
                            tint = Primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Tap to add receipt photos",
                            color = Primary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
            
            // OCR Suggestion Card
            if (showOcrSuggestion && suggestedData != null) {
                val currentSuggestedData = suggestedData!! // Create local immutable copy
                ModernCard(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    backgroundColor = SuccessGreen.copy(alpha = 0.1f)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.AutoFixHigh,
                                    contentDescription = "OCR Suggestion",
                                    tint = SuccessGreen,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Detected from receipt",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = SuccessGreen
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            currentSuggestedData.suggestedAmount?.let { suggestedAmount ->
                                Text(
                                    text = "Amount: $${String.format("%.2f", suggestedAmount)}",
                                    fontSize = 13.sp,
                                    color = TextSecondary
                                )
                            }
                            
                            currentSuggestedData.suggestedDescription?.let { suggestedDesc ->
                                Text(
                                    text = "Description: $suggestedDesc",
                                    fontSize = 13.sp,
                                    color = TextSecondary
                                )
                            }
                        }
                        
                        Row {
                            TextButton(
                                onClick = {
                                    currentSuggestedData.suggestedDescription?.let { desc -> description = desc }
                                    currentSuggestedData.suggestedAmount?.let { amt -> amount = amt.toString() }
                                    showOcrSuggestion = false
                                }
                            ) {
                                Text("Use", color = SuccessGreen, fontWeight = FontWeight.Medium)
                            }
                            
                            IconButton(
                                onClick = { showOcrSuggestion = false }
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Dismiss",
                                    tint = TextSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
            
            // Expense Details Form
            ModernCard(
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Text(
                    text = "Expense Details",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                ModernTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = "Description",
                    leadingIcon = Icons.Default.Description
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                ModernTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = "Amount ($)",
                    leadingIcon = Icons.Default.AttachMoney
                )
            }
            
            // Category Selection
            ModernCard(
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Text(
                    text = "Category",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                ExposedDropdownMenuBox(
                    expanded = showCategoryDropdown,
                    onExpandedChange = { showCategoryDropdown = it }
                ) {
                    ModernTextField(
                        value = selectedCategory.name,
                        onValueChange = { },
                        label = "Select Category",
                        leadingIcon = selectedCategory.icon,
                        trailingIcon = if (showCategoryDropdown) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        modifier = Modifier.menuAnchor()
                    )
                    
                    ExposedDropdownMenu(
                        expanded = showCategoryDropdown,
                        onDismissRequest = { showCategoryDropdown = false }
                    ) {
                        categories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category.name) },
                                onClick = {
                                    selectedCategory = category
                                    showCategoryDropdown = false
                                },
                                leadingIcon = {
                                    Icon(category.icon, contentDescription = null)
                                }
                            )
                        }
                    }
                }
            }
            
            // Split Method
            ModernCard(
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Text(
                    text = "Split Method",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    splitTypes.forEach { splitType ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedSplitType = splitType }
                                .background(
                                    if (selectedSplitType == splitType) Primary.copy(alpha = 0.1f) else Color.Transparent,
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedSplitType == splitType,
                                onClick = { selectedSplitType = splitType },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = Primary
                                )
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = splitType,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = TextPrimary
                                )
                                Text(
                                    text = when (splitType) {
                                        "Equal Split" -> "Split equally among all members"
                                        "Percentage" -> "Split by custom percentages"
                                        "Custom Amount" -> "Enter exact amounts for each member"
                                        else -> ""
                                    },
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                }
            }
            
            // Save Button
            ModernButton(
                text = if (isLoading) "Saving..." else "Save Expense",
                onClick = {
                    if (description.isNotBlank() && amount.isNotBlank()) {
                        isLoading = true
                        // Simulate API call
                        // In real implementation: save expense and navigate back
                        onExpenseAdded()
                    }
                },
                enabled = !isLoading && description.isNotBlank() && amount.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                icon = Icons.Default.Save
            )
            
            Spacer(modifier = Modifier.height(20.dp))
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

