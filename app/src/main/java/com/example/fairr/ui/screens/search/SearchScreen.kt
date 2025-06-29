package com.example.fairr.ui.screens.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.fairr.ui.theme.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.collectAsState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    navController: NavController,
    onNavigateToExpense: (String) -> Unit = {},
    onNavigateToGroup: (String) -> Unit = {},
    viewModel: SearchViewModel = hiltViewModel()
) {
    var searchQuery by remember { mutableStateOf("") }
    var showFilters by remember { mutableStateOf(false) }
    var selectedFilter by remember { mutableStateOf(SearchFilter.ALL) }
    var selectedCategory by remember { mutableStateOf("All Categories") }
    var selectedDateRange by remember { mutableStateOf("All Time") }
    var sortBy by remember { mutableStateOf(SortOption.DATE_DESC) }
    
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    
    val uiState = viewModel.uiState
    
    val categories = listOf("All Categories", "Food & Dining", "Transportation", "Entertainment", "Shopping", "Bills", "Other")
    val dateRanges = listOf("All Time", "Last 7 Days", "Last 30 Days", "Last 3 Months", "This Year")

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    // Search when query or filters change
    LaunchedEffect(searchQuery, selectedFilter, selectedCategory, selectedDateRange, sortBy) {
        viewModel.search(searchQuery, selectedFilter, selectedCategory, selectedDateRange, sortBy)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search expenses, groups...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(
                            onSearch = { keyboardController?.hide() }
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = DarkGreen,
                            focusedLabelColor = DarkGreen
                        ),
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                                }
                            }
                        }
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
                    IconButton(onClick = { showFilters = !showFilters }) {
                        Icon(
                            Icons.Default.FilterList,
                            contentDescription = "Filters",
                            tint = if (showFilters) DarkGreen else TextSecondary
                        )
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
        ) {
            // Filter Section
            if (showFilters) {
                FilterSection(
                    selectedFilter = selectedFilter,
                    onFilterChange = { selectedFilter = it },
                    selectedCategory = selectedCategory,
                    onCategoryChange = { selectedCategory = it },
                    selectedDateRange = selectedDateRange,
                    onDateRangeChange = { selectedDateRange = it },
                    sortBy = sortBy,
                    onSortChange = { sortBy = it },
                    categories = categories,
                    dateRanges = dateRanges
                )
            }
            
            // Results Section
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (searchQuery.isNotEmpty()) {
                    item {
                        Text(
                            text = "${uiState.searchResults.size} results for \"$searchQuery\"",
                            fontSize = 14.sp,
                            color = TextSecondary,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                }
                
                if (uiState.searchResults.isEmpty() && searchQuery.isNotEmpty()) {
                    item {
                        EmptySearchState(query = searchQuery)
                    }
                } else if (searchQuery.isEmpty()) {
                    item {
                        SearchSuggestionsState()
                    }
                } else {
                    items(uiState.searchResults) { result: SearchResult ->
                        when (result) {
                            is SearchResult.ExpenseResult -> {
                                ExpenseSearchCard(
                                    expense = result,
                                    onClick = { onNavigateToExpense(result.id) }
                                )
                            }
                            is SearchResult.GroupResult -> {
                                GroupSearchCard(
                                    group = result,
                                    onClick = { onNavigateToGroup(result.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterSection(
    selectedFilter: SearchFilter,
    onFilterChange: (SearchFilter) -> Unit,
    selectedCategory: String,
    onCategoryChange: (String) -> Unit,
    selectedDateRange: String,
    onDateRangeChange: (String) -> Unit,
    sortBy: SortOption,
    onSortChange: (SortOption) -> Unit,
    categories: List<String>,
    dateRanges: List<String>
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .shadow(1.dp, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = NeutralWhite)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Filter Type Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SearchFilter.values().forEach { filter ->
                    FilterChip(
                        onClick = { onFilterChange(filter) },
                        label = { Text(filter.displayName) },
                        selected = selectedFilter == filter,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = DarkGreen,
                            selectedLabelColor = NeutralWhite
                        )
                    )
                }
            }
            
            // Category Dropdown
            var categoryExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = categoryExpanded,
                onExpandedChange = { categoryExpanded = !categoryExpanded }
            ) {
                OutlinedTextField(
                    value = selectedCategory,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Category") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DarkGreen,
                        focusedLabelColor = DarkGreen
                    )
                )
                
                ExposedDropdownMenu(
                    expanded = categoryExpanded,
                    onDismissRequest = { categoryExpanded = false }
                ) {
                    categories.forEach { category ->
                        DropdownMenuItem(
                            text = { Text(category) },
                            onClick = {
                                onCategoryChange(category)
                                categoryExpanded = false
                            }
                        )
                    }
                }
            }
            
            // Date Range and Sort Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Date Range Dropdown
                var dateExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = dateExpanded,
                    onExpandedChange = { dateExpanded = !dateExpanded },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = selectedDateRange,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Date Range") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dateExpanded) },
                        modifier = Modifier.menuAnchor(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = DarkGreen,
                            focusedLabelColor = DarkGreen
                        )
                    )
                    
                    ExposedDropdownMenu(
                        expanded = dateExpanded,
                        onDismissRequest = { dateExpanded = false }
                    ) {
                        dateRanges.forEach { range ->
                            DropdownMenuItem(
                                text = { Text(range) },
                                onClick = {
                                    onDateRangeChange(range)
                                    dateExpanded = false
                                }
                            )
                        }
                    }
                }
                
                // Sort Dropdown
                var sortExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = sortExpanded,
                    onExpandedChange = { sortExpanded = !sortExpanded },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = sortBy.displayName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Sort By") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = sortExpanded) },
                        modifier = Modifier.menuAnchor(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = DarkGreen,
                            focusedLabelColor = DarkGreen
                        )
                    )
                    
                    ExposedDropdownMenu(
                        expanded = sortExpanded,
                        onDismissRequest = { sortExpanded = false }
                    ) {
                        SortOption.values().forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option.displayName) },
                                onClick = {
                                    onSortChange(option)
                                    sortExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseSearchCard(
    expense: SearchResult.ExpenseResult,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
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
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(DarkBlue.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Receipt,
                    contentDescription = "Expense",
                    tint = DarkBlue,
                    modifier = Modifier.size(20.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = expense.description,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${expense.groupName} • ${expense.date}",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
                Text(
                    text = expense.category,
                    fontSize = 11.sp,
                    color = DarkGreen,
                    modifier = Modifier
                        .background(
                            DarkGreen.copy(alpha = 0.1f),
                            RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
            
            Text(
                text = "$${String.format("%.2f", expense.amount)}",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupSearchCard(
    group: SearchResult.GroupResult,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
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
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(DarkGreen.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Group,
                    contentDescription = "Group",
                    tint = DarkGreen,
                    modifier = Modifier.size(20.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = group.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${group.memberCount} members • ${group.expenseCount} expenses",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = if (group.balance >= 0) "you get" else "you owe",
                    fontSize = 10.sp,
                    color = TextSecondary
                )
                Text(
                    text = "$${String.format("%.2f", kotlin.math.abs(group.balance))}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (group.balance >= 0) SuccessGreen else ErrorRed
                )
            }
        }
    }
}

@Composable
fun EmptySearchState(query: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Default.SearchOff,
            contentDescription = "No results",
            modifier = Modifier.size(64.dp),
            tint = PlaceholderText
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "No results for \"$query\"",
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = TextPrimary
        )
        
        Text(
            text = "Try adjusting your search or filters",
            fontSize = 14.sp,
            color = TextSecondary,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
fun SearchSuggestionsState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "Quick Search",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        val suggestions = listOf(
            "Recent expenses",
            "Food & Dining",
            "This month",
            "Transportation",
            "Bills"
        )
        
        suggestions.forEach { suggestion ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { /* TODO: Apply suggestion */ }
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Search,
                    contentDescription = null,
                    tint = PlaceholderText,
                    modifier = Modifier.size(16.dp)
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Text(
                    text = suggestion,
                    fontSize = 14.sp,
                    color = TextSecondary
                )
            }
        }
    }
}

// Data classes and enums
sealed class SearchResult {
    data class ExpenseResult(
        val id: String,
        val description: String,
        val amount: Double,
        val date: String,
        val category: String,
        val groupName: String
    ) : SearchResult()
    
    data class GroupResult(
        val id: String,
        val name: String,
        val memberCount: Int,
        val expenseCount: Int,
        val balance: Double
    ) : SearchResult()
}

enum class SearchFilter(val displayName: String) {
    ALL("All"),
    EXPENSES("Expenses"),
    GROUPS("Groups")
}

enum class SortOption(val displayName: String) {
    DATE_DESC("Newest First"),
    DATE_ASC("Oldest First"),
    AMOUNT_DESC("Highest Amount"),
    AMOUNT_ASC("Lowest Amount"),
    NAME_ASC("A to Z"),
    NAME_DESC("Z to A")
}

@Preview(showBackground = true)
@Composable
fun SearchScreenPreview() {
    FairrTheme {
        SearchScreen(navController = rememberNavController())
    }
} 
