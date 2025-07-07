package com.example.fairr.ui.screens.categories

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.fairr.data.category.CategoryItem
import com.example.fairr.data.category.CategoryUsageStats
import com.example.fairr.ui.components.ErrorBanner
import com.example.fairr.ui.components.FairrActionTopAppBar
import com.example.fairr.ui.components.EnhancedLoadingState
import com.example.fairr.ui.components.SuccessBanner
import com.example.fairr.ui.theme.*
import com.example.fairr.ui.viewmodel.CategoryManagementViewModel
import com.example.fairr.util.CurrencyFormatter
import androidx.compose.material3.HorizontalDivider

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryManagementScreen(
    navController: NavController,
    viewModel: CategoryManagementViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val categoryUsageStats by viewModel.categoryUsageStats.collectAsStateWithLifecycle()
    
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf<CategoryItem?>(null) }
    
    // Clear success message after showing it
    LaunchedEffect(uiState.successMessage) {
        if (uiState.successMessage != null) {
            kotlinx.coroutines.delay(3000)
            viewModel.clearSuccessMessage()
        }
    }
    
    Scaffold(
        topBar = {
            FairrActionTopAppBar(
                title = "Manage Categories",
                navController = navController,
                actionIcon = Icons.Default.Refresh,
                actionContentDescription = "Refresh Categories",
                onActionClick = { viewModel.refresh() }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = DarkGreen,
                contentColor = NeutralWhite,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Category")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(LightBackground)
                .padding(padding)
        ) {
            // Error Banner
            if (uiState.error != null) {
                ErrorBanner(
                    message = uiState.error!!,
                    onDismiss = { viewModel.clearError() },
                    modifier = Modifier.padding(16.dp)
                )
            }
            
            // Success Banner
            if (uiState.successMessage != null) {
                SuccessBanner(
                    message = uiState.successMessage!!,
                    onDismiss = { viewModel.clearSuccessMessage() },
                    modifier = Modifier.padding(16.dp)
                )
            }
            
            // Loading State
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    EnhancedLoadingState(
                        message = "Loading categories...",
                        subtitle = "Please wait while we fetch your categories"
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Header Card
                    item {
                        CategoryManagementHeader(
                            totalCategories = categories.size,
                            customCategories = categories.count { !it.isDefault }
                        )
                    }
                    
                    // Default Categories Section
                    item {
                        CategorySectionHeader(
                            title = "Default Categories",
                            subtitle = "Built-in expense categories",
                            count = categories.count { it.isDefault }
                        )
                    }
                    
                    items(categories.filter { it.isDefault }) { category ->
                        CategoryCard(
                            category = category,
                            usageStats = categoryUsageStats[category.id],
                            onEdit = null, // Default categories can't be edited
                            onDelete = null // Default categories can't be deleted
                        )
                    }
                    
                    // Custom Categories Section
                    val customCategories = categories.filter { !it.isDefault }
                    if (customCategories.isNotEmpty()) {
                        item {
                            CategorySectionHeader(
                                title = "Custom Categories",
                                subtitle = "Your personalized expense categories",
                                count = customCategories.size
                            )
                        }
                        
                        items(customCategories) { category ->
                            CategoryCard(
                                category = category,
                                usageStats = categoryUsageStats[category.id],
                                onEdit = {
                                    selectedCategory = category
                                    showEditDialog = true
                                },
                                onDelete = {
                                    selectedCategory = category
                                    showDeleteDialog = true
                                }
                            )
                        }
                    }
                    
                    // Empty State for Custom Categories
                    if (customCategories.isEmpty()) {
                        item {
                            EmptyCustomCategoriesCard(
                                onAddCategory = { showAddDialog = true }
                            )
                        }
                    }
                    
                    // Bottom spacing for FAB
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }
    }
    
    // Add Category Dialog
    if (showAddDialog) {
        AddEditCategoryDialog(
            category = null,
            viewModel = viewModel,
            onDismiss = { showAddDialog = false },
            onSave = { name, icon, color ->
                viewModel.createCategory(name, icon, color)
                showAddDialog = false
            }
        )
    }
    
    // Edit Category Dialog
    selectedCategory?.let { category ->
        if (showEditDialog) {
            AddEditCategoryDialog(
                category = category,
                viewModel = viewModel,
                onDismiss = { 
                    showEditDialog = false
                    selectedCategory = null
                },
                onSave = { name, icon, color ->
                    viewModel.updateCategory(category.id, name, icon, color)
                    showEditDialog = false
                    selectedCategory = null
                }
            )
        }
    }
    
    // Delete Confirmation Dialog
    selectedCategory?.let { category ->
        if (showDeleteDialog) {
            DeleteCategoryDialog(
                category = category,
                usageStats = categoryUsageStats[category.id],
                onConfirm = {
                    viewModel.deleteCategory(category.id)
                    showDeleteDialog = false
                    selectedCategory = null
                },
                onDismiss = { 
                    showDeleteDialog = false
                    selectedCategory = null
                }
            )
        }
    }
}

@Composable
fun CategoryManagementHeader(
    totalCategories: Int,
    customCategories: Int
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = NeutralWhite)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.Category,
                contentDescription = "Categories",
                tint = DarkGreen,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Expense Categories",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = "Organize your expenses with custom categories",
                fontSize = 14.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Statistics
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                CategoryStat(
                    label = "Total Categories",
                    value = totalCategories.toString(),
                    color = DarkGreen
                )
                CategoryStat(
                    label = "Custom Categories",
                    value = customCategories.toString(),
                    color = DarkBlue
                )
            }
        }
    }
}

@Composable
fun CategoryStat(
    label: String,
    value: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = TextSecondary
        )
    }
}

@Composable
fun CategorySectionHeader(
    title: String,
    subtitle: String,
    count: Int
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = "$count categories",
                fontSize = 12.sp,
                color = TextSecondary,
                modifier = Modifier
                    .background(
                        color = LightGray,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
        Text(
            text = subtitle,
            fontSize = 14.sp,
            color = TextSecondary,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}

@Composable
fun CategoryCard(
    category: CategoryItem,
    usageStats: CategoryUsageStats?,
    onEdit: (() -> Unit)?,
    onDelete: (() -> Unit)?
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(1.dp, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = NeutralWhite)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Main category info
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Category Icon
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            color = Color(android.graphics.Color.parseColor(category.color)).copy(alpha = 0.1f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = category.icon,
                        fontSize = 24.sp
                    )
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                // Category Info
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = category.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )
                    Text(
                        text = if (category.isDefault) "Default category" else "Custom category",
                        fontSize = 12.sp,
                        color = if (category.isDefault) DarkGreen else DarkBlue
                    )
                }
                
                // Action Buttons
                Row {
                    if (onEdit != null) {
                        IconButton(onClick = onEdit) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = "Edit",
                                tint = TextSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    
                    if (onDelete != null) {
                        IconButton(onClick = onDelete) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = ErrorRed,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
            
            // Usage Statistics
            if (usageStats != null && usageStats.totalExpenses > 0) {
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    thickness = 1.dp,
                    color = DividerColor
                )
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${usageStats.totalExpenses} expenses",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                    if (usageStats.totalAmount > 0) {
                        Text(
                            text = CurrencyFormatter.format("PHP", usageStats.totalAmount),
                            fontSize = 12.sp,
                            color = TextSecondary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyCustomCategoriesCard(
    onAddCategory: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onAddCategory() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = NeutralWhite),
        border = BorderStroke(1.dp, LightGray)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.Add,
                contentDescription = "Add Category",
                tint = DarkGreen,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Create Custom Categories",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )
            Text(
                text = "Add personalized expense categories to better organize your spending",
                fontSize = 14.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditCategoryDialog(
    category: CategoryItem?,
    viewModel: CategoryManagementViewModel,
    onDismiss: () -> Unit,
    onSave: (String, String, String) -> Unit
) {
    var name by remember { mutableStateOf(category?.name ?: "") }
    var selectedIcon by remember { mutableStateOf(category?.icon ?: "📦") }
    var selectedColor by remember { mutableStateOf(category?.color ?: "#95A5A6") }
    
    val isEditing = category != null
    val iconOptions = viewModel.getIconOptions()
    val colorOptions = viewModel.getColorOptions()
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Text(if (isEditing) "Edit Category" else "Add Category") 
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Name Input
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Category Name") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DarkGreen,
                        focusedLabelColor = DarkGreen
                    )
                )
                
                // Icon Selection
                Text(
                    text = "Choose Icon",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary
                )
                
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(iconOptions) { icon ->
                        IconOption(
                            icon = icon,
                            isSelected = icon == selectedIcon,
                            onSelect = { selectedIcon = icon }
                        )
                    }
                }
                
                // Color Selection
                Text(
                    text = "Choose Color",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary
                )
                
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(colorOptions) { color ->
                        ColorOption(
                            color = color,
                            isSelected = color == selectedColor,
                            onSelect = { selectedColor = color }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onSave(name.trim(), selectedIcon, selectedColor)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = DarkGreen),
                enabled = name.isNotBlank()
            ) {
                Text(if (isEditing) "Update" else "Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun IconOption(
    icon: String,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Surface(
        onClick = onSelect,
        modifier = Modifier.size(40.dp),
        shape = CircleShape,
        color = if (isSelected) DarkGreen.copy(alpha = 0.1f) else Color.Transparent,
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) DarkGreen else PlaceholderText.copy(alpha = 0.3f)
        )
    ) {
        Box(
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = icon,
                fontSize = 20.sp
            )
        }
    }
}

@Composable
fun ColorOption(
    color: String,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Surface(
        onClick = onSelect,
        modifier = Modifier.size(32.dp),
        shape = CircleShape,
        color = Color(android.graphics.Color.parseColor(color)),
        border = BorderStroke(
            width = if (isSelected) 3.dp else 1.dp,
            color = if (isSelected) NeutralWhite else PlaceholderText.copy(alpha = 0.3f)
        )
    ) {
        if (isSelected) {
            Box(
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = NeutralWhite,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun DeleteCategoryDialog(
    category: CategoryItem,
    usageStats: CategoryUsageStats?,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete Category") },
        text = { 
            Column {
                Text("Are you sure you want to delete \"${category.name}\"?")
                
                if (usageStats != null && usageStats.totalExpenses > 0) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "This category has been used in ${usageStats.totalExpenses} expenses. Deleting it will not affect existing expenses.",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "This action cannot be undone.",
                    fontSize = 12.sp,
                    color = ErrorRed,
                    fontWeight = FontWeight.Medium
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)
            ) {
                Text("Delete")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun CategoryManagementScreenPreview() {
    FairrTheme {
        CategoryManagementScreen(navController = rememberNavController())
    }
} 
