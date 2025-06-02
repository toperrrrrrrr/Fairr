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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.fairr.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryManagementScreen(
    navController: NavController,
    onSaveCategories: (List<ExpenseCategory>) -> Unit = {}
) {
    var categories by remember { mutableStateOf(getDefaultCategories()) }
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf<ExpenseCategory?>(null) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Manage Categories",
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
                            onSaveCategories(categories)
                            navController.popBackStack()
                        }
                    ) {
                        Text(
                            "Save",
                            color = DarkGreen,
                            fontWeight = FontWeight.Medium
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = NeutralWhite
                )
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(LightBackground)
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
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
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
            
            items(categories) { category ->
                CategoryCard(
                    category = category,
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
            
            item {
                Spacer(modifier = Modifier.height(80.dp)) // Space for FAB
            }
        }
    }
    
    // Add Category Dialog
    if (showAddDialog) {
        AddEditCategoryDialog(
            category = null,
            onDismiss = { showAddDialog = false },
            onSave = { newCategory ->
                categories = categories + newCategory
                showAddDialog = false
            }
        )
    }
    
    // Edit Category Dialog
    if (showEditDialog && selectedCategory != null) {
        AddEditCategoryDialog(
            category = selectedCategory,
            onDismiss = { showEditDialog = false },
            onSave = { updatedCategory ->
                categories = categories.map { 
                    if (it.id == selectedCategory!!.id) updatedCategory else it 
                }
                showEditDialog = false
                selectedCategory = null
            }
        )
    }
    
    // Delete Confirmation Dialog
    if (showDeleteDialog && selectedCategory != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Category") },
            text = { 
                Text("Are you sure you want to delete \"${selectedCategory!!.name}\"? This action cannot be undone.") 
            },
            confirmButton = {
                Button(
                    onClick = {
                        categories = categories.filter { it.id != selectedCategory!!.id }
                        showDeleteDialog = false
                        selectedCategory = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryCard(
    category: ExpenseCategory,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(1.dp, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = NeutralWhite)
    ) {
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
                    .background(category.color.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    category.icon,
                    contentDescription = category.name,
                    tint = category.color,
                    modifier = Modifier.size(24.dp)
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
                    color = if (category.isDefault) DarkGreen else TextSecondary
                )
            }
            
            // Action Buttons
            Row {
                IconButton(onClick = onEdit) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                if (!category.isDefault) {
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
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditCategoryDialog(
    category: ExpenseCategory?,
    onDismiss: () -> Unit,
    onSave: (ExpenseCategory) -> Unit
) {
    var name by remember { mutableStateOf(category?.name ?: "") }
    var selectedIcon by remember { mutableStateOf(category?.icon ?: Icons.Default.Category) }
    var selectedColor by remember { mutableStateOf(category?.color ?: DarkGreen) }
    
    val isEditing = category != null
    
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
                    items(getCategoryIcons()) { icon ->
                        IconOption(
                            icon = icon,
                            isSelected = icon == selectedIcon,
                            color = selectedColor,
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
                    items(getCategoryColors()) { color ->
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
                        val newCategory = ExpenseCategory(
                            id = category?.id ?: generateCategoryId(),
                            name = name.trim(),
                            icon = selectedIcon,
                            color = selectedColor,
                            isDefault = category?.isDefault ?: false
                        )
                        onSave(newCategory)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IconOption(
    icon: ImageVector,
    isSelected: Boolean,
    color: Color,
    onSelect: () -> Unit
) {
    Surface(
        onClick = onSelect,
        modifier = Modifier.size(40.dp),
        shape = CircleShape,
        color = if (isSelected) color.copy(alpha = 0.2f) else Color.Transparent,
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) color else PlaceholderText.copy(alpha = 0.3f)
        )
    ) {
        Box(
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = if (isSelected) color else PlaceholderText,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColorOption(
    color: Color,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Surface(
        onClick = onSelect,
        modifier = Modifier.size(32.dp),
        shape = CircleShape,
        color = color,
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

// Data class and helper functions
data class ExpenseCategory(
    val id: String,
    val name: String,
    val icon: ImageVector,
    val color: Color,
    val isDefault: Boolean = false
)

fun getDefaultCategories(): List<ExpenseCategory> {
    return listOf(
        ExpenseCategory("1", "Food & Dining", Icons.Default.Restaurant, DarkGreen, true),
        ExpenseCategory("2", "Transportation", Icons.Default.DirectionsCar, DarkBlue, true),
        ExpenseCategory("3", "Entertainment", Icons.Default.Movie, ErrorRed, true),
        ExpenseCategory("4", "Shopping", Icons.Default.ShoppingBag, WarningOrange, true),
        ExpenseCategory("5", "Bills", Icons.Default.Receipt, TextSecondary, true),
        ExpenseCategory("6", "Groceries", Icons.Default.ShoppingCart, SuccessGreen, true),
        ExpenseCategory("7", "Health", Icons.Default.LocalHospital, FairrColors.CategoryColors[3], true),
        ExpenseCategory("8", "Travel", Icons.Default.Flight, FairrColors.CategoryColors[4], true),
        ExpenseCategory("9", "Other", Icons.Default.Category, PlaceholderText, true)
    )
}

fun getCategoryIcons(): List<ImageVector> {
    return listOf(
        Icons.Default.Restaurant,
        Icons.Default.DirectionsCar,
        Icons.Default.Movie,
        Icons.Default.ShoppingBag,
        Icons.Default.Receipt,
        Icons.Default.ShoppingCart,
        Icons.Default.LocalHospital,
        Icons.Default.Flight,
        Icons.Default.Home,
        Icons.Default.School,
        Icons.Default.Work,
        Icons.Default.Sports,
        Icons.Default.Pets,
        Icons.Default.LocalGasStation,
        Icons.Default.Coffee,
        Icons.Default.Category
    )
}

fun getCategoryColors(): List<Color> {
    return FairrColors.CategoryColors
}

fun generateCategoryId(): String {
    return "custom_${System.currentTimeMillis()}"
}

@Preview(showBackground = true)
@Composable
fun CategoryManagementScreenPreview() {
    FairrTheme {
        CategoryManagementScreen(navController = rememberNavController())
    }
} 
