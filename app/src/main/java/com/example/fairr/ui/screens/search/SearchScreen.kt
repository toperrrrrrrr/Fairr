package com.example.fairr.ui.screens.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
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
import com.example.fairr.ui.components.FairrEmptyState
import com.example.fairr.ui.components.FairrLoadingCard
import com.example.fairr.util.CurrencyFormatter
import com.example.fairr.ui.components.CategoryIcon
import com.example.fairr.data.model.Group
import com.example.fairr.data.model.Expense
import java.util.Date
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import com.example.fairr.ui.components.ErrorType
import com.example.fairr.ui.components.ErrorBanner
import com.example.fairr.ui.components.StandardErrorState

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

    // Search when query or filters change with debouncing
    LaunchedEffect(searchQuery, selectedFilter, selectedCategory, selectedDateRange, sortBy) {
        if (searchQuery.isNotBlank()) {
            viewModel.search(searchQuery, selectedFilter, selectedCategory, selectedDateRange, sortBy)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { 
                            Text(
                                "Search expenses, groups...",
                                color = TextSecondary.copy(alpha = 0.7f)
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester)
                            .semantics {
                                contentDescription = "Search field for expenses and groups"
                            },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(
                            onSearch = { 
                                keyboardController?.hide()
                                if (searchQuery.isNotBlank()) {
                                    viewModel.search(searchQuery, selectedFilter, selectedCategory, selectedDateRange, sortBy)
                                }
                            }
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Primary,
                            focusedLabelColor = Primary,
                            cursorColor = Primary
                        ),
                        leadingIcon = {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = "Search",
                                tint = TextSecondary.copy(alpha = 0.7f),
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        trailingIcon = {
                            Row {
                                if (uiState.isLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier
                                            .size(20.dp)
                                            .semantics {
                                                contentDescription = "Searching for results"
                                            },
                                        strokeWidth = 2.dp,
                                        color = Primary
                                    )
                                }
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(
                                        onClick = { 
                                            searchQuery = ""
                                            viewModel.clearSearch()
                                        },
                                        modifier = Modifier.semantics {
                                            contentDescription = "Clear search"
                                        }
                                    ) {
                                        Icon(
                                            Icons.Default.Clear, 
                                            contentDescription = "Clear",
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
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
                    IconButton(
                        onClick = { showFilters = !showFilters },
                        modifier = Modifier.semantics {
                            contentDescription = if (showFilters) "Hide search filters" else "Show search filters"
                        }
                    ) {
                        Icon(
                            Icons.Default.FilterList,
                            contentDescription = if (showFilters) "Hide Filters" else "Show Filters",
                            tint = if (showFilters) Primary else TextSecondary
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
            // Filter Section with accessibility
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
                    dateRanges = dateRanges,
                    modifier = Modifier.semantics {
                        contentDescription = "Search filters: Filter type, category, date range, and sort options"
                    }
                )
            }
            
            // Results Section
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Loading state with accessibility
                if (uiState.isLoading) {
                    // Show skeleton loaders for search results
                    items(4) {
                        com.example.fairr.ui.components.SkeletonCard()
                    }
                    
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            com.example.fairr.ui.components.EnhancedLoadingState(
                                message = "Searching your data...",
                                subtitle = "Looking through expenses, groups, and transactions"
                            )
                        }
                    }
                }
                // Error state with accessibility
                else if (uiState.error != null) {
                    item {
                        ErrorBanner(
                            errorType = ErrorType.GENERIC,
                            message = "Search failed: ${uiState.error}",
                            modifier = Modifier
                                .fillMaxWidth()
                                .semantics {
                                    contentDescription = "Search error: ${uiState.error}"
                                }
                        )
                    }
                }
                // Empty state
                else if (searchQuery.isNotBlank() && uiState.searchResults.isEmpty()) {
                    item {
                        FairrEmptyState(
                            title = "No Results Found",
                            message = "Try adjusting your search terms or filters to find what you're looking for.",
                            actionText = "Clear Filters",
                            onActionClick = { 
                                selectedFilter = SearchFilter.ALL
                                selectedCategory = "All Categories"
                                selectedDateRange = "All Time"
                                sortBy = SortOption.DATE_DESC
                            },
                            icon = Icons.Default.SearchOff,
                            modifier = Modifier
                                .fillMaxWidth()
                                .semantics {
                                    contentDescription = "No search results found for '$searchQuery'. Try adjusting your search terms or clearing filters."
                                }
                        )
                    }
                }
                // Success state with results
                else if (searchQuery.isNotBlank() && uiState.searchResults.isNotEmpty()) {
                    // Summary header
                    item {
                        val totalResults = uiState.searchResults.size
                        Text(
                            text = "Found $totalResults results for \"$searchQuery\"",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary,
                            modifier = Modifier
                                .padding(vertical = 8.dp)
                                .semantics {
                                    contentDescription = "Search results: $totalResults items found for $searchQuery"
                                    heading()
                                }
                        )
                    }

                    // Group results
                    val groupResults = uiState.searchResults.filterIsInstance<SearchResult.GroupResult>()
                    if (groupResults.isNotEmpty()) {
                        item {
                            Text(
                                text = "Groups (${groupResults.size})",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = TextSecondary,
                                modifier = Modifier
                                    .padding(top = 16.dp, bottom = 8.dp)
                                    .semantics {
                                        heading()
                                        contentDescription = "Groups section, ${groupResults.size} groups found"
                                    }
                            )
                        }
                        
                        items(groupResults) { groupResult ->
                            GroupSearchCard(
                                group = groupResult,
                                searchQuery = searchQuery,
                                onClick = { onNavigateToGroup(groupResult.id) }
                            )
                        }
                    }

                    // Expense results
                    val expenseResults = uiState.searchResults.filterIsInstance<SearchResult.ExpenseResult>()
                    if (expenseResults.isNotEmpty()) {
                        item {
                            Text(
                                text = "Expenses (${expenseResults.size})",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = TextSecondary,
                                modifier = Modifier
                                    .padding(top = 16.dp, bottom = 8.dp)
                                    .semantics {
                                        heading()
                                        contentDescription = "Expenses section, ${expenseResults.size} expenses found"
                                    }
                            )
                        }
                        
                        items(expenseResults) { expenseResult ->
                            ExpenseSearchCard(
                                expense = expenseResult,
                                searchQuery = searchQuery,
                                onClick = { onNavigateToExpense(expenseResult.id) }
                            )
                        }
                    }
                }
                // Initial state with search suggestions
                else if (searchQuery.isEmpty()) {
                    item {
                        SearchSuggestionsSection(
                            onSuggestionClick = { suggestion ->
                                searchQuery = suggestion
                                viewModel.search(suggestion, selectedFilter, selectedCategory, selectedDateRange, sortBy)
                            },
                            modifier = Modifier.semantics {
                                contentDescription = "Search suggestions and recent searches"
                            }
                        )
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
    dateRanges: List<String>,
    modifier: Modifier
) {
    Card(
        modifier = modifier
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
    searchQuery: String = "",
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = NeutralWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(Primary.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Receipt,
                    contentDescription = "Expense",
                    tint = Primary,
                    modifier = Modifier.size(22.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(14.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = highlightSearchTerm(expense.description, searchQuery),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = expense.groupName,
                        fontSize = 13.sp,
                        color = TextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                    Box(
                        modifier = Modifier
                            .size(3.dp)
                            .background(TextSecondary.copy(alpha = 0.3f), CircleShape)
                    )
                    Text(
                        text = expense.date,
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                }
                
                Spacer(modifier = Modifier.height(6.dp))
                
                Surface(
                    color = Primary.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.wrapContentSize()
                ) {
                    Text(
                        text = expense.category,
                        fontSize = 11.sp,
                        color = Primary,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "₱${String.format("%.2f", expense.amount)}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = "View Details",
                    tint = TextSecondary.copy(alpha = 0.5f),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupSearchCard(
    group: SearchResult.GroupResult,
    searchQuery: String = "",
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = NeutralWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(SuccessGreen.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Group,
                    contentDescription = "Group",
                    tint = SuccessGreen,
                    modifier = Modifier.size(22.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(14.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = highlightSearchTerm(group.name, searchQuery),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "${group.memberCount} member${if (group.memberCount != 1) "s" else ""}",
                        fontSize = 13.sp,
                        color = TextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                    Box(
                        modifier = Modifier
                            .size(3.dp)
                            .background(TextSecondary.copy(alpha = 0.3f), CircleShape)
                    )
                    Text(
                        text = "${group.expenseCount} expense${if (group.expenseCount != 1) "s" else ""}",
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = if (group.balance >= 0) "you get" else "you owe",
                    fontSize = 11.sp,
                    color = TextSecondary,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "₱${String.format("%.2f", kotlin.math.abs(group.balance))}",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (group.balance >= 0) SuccessGreen else ErrorRed
                )
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = "View Group",
                    tint = TextSecondary.copy(alpha = 0.5f),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun EnhancedSearchSuggestionsState(
    onSuggestionClick: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Quick Search Section
        Text(
            text = "Quick Search",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        // Popular searches
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Primary.copy(alpha = 0.05f))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Popular Searches",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Primary
                )
                
                val popularSuggestions = listOf(
                    "Food & Dining" to Icons.Default.Restaurant,
                    "Transportation" to Icons.Default.DirectionsCar,
                    "Entertainment" to Icons.Default.MovieCreation,
                    "Shopping" to Icons.Default.ShoppingCart,
                    "Bills & Utilities" to Icons.Default.Receipt
                )
                
                popularSuggestions.forEach { (suggestion, icon) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSuggestionClick(suggestion) }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            icon,
                            contentDescription = null,
                            tint = Primary,
                            modifier = Modifier.size(16.dp)
                        )
                        
                        Spacer(modifier = Modifier.width(12.dp))
                        
                        Text(
                            text = suggestion,
                            fontSize = 14.sp,
                            color = TextPrimary
                        )
                        
                        Spacer(modifier = Modifier.weight(1f))
                        
                        Icon(
                            Icons.Default.TrendingUp,
                            contentDescription = "Popular",
                            tint = TextSecondary.copy(alpha = 0.5f),
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Time-based suggestions
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = AccentBlue.copy(alpha = 0.05f))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Time Periods",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AccentBlue
                )
                
                val timeSuggestions = listOf(
                    "This week" to Icons.Default.Today,
                    "This month" to Icons.Default.CalendarMonth,
                    "Last 7 days" to Icons.Default.DateRange,
                    "Last 30 days" to Icons.Default.CalendarViewMonth
                )
                
                timeSuggestions.forEach { (suggestion, icon) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSuggestionClick(suggestion) }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            icon,
                            contentDescription = null,
                            tint = AccentBlue,
                            modifier = Modifier.size(16.dp)
                        )
                        
                        Spacer(modifier = Modifier.width(12.dp))
                        
                        Text(
                            text = suggestion,
                            fontSize = 14.sp,
                            color = TextPrimary
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Search tips
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = WarningOrange.copy(alpha = 0.05f))
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    Icons.Default.Lightbulb,
                    contentDescription = "Tip",
                    tint = WarningOrange,
                    modifier = Modifier.size(20.dp)
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column {
                    Text(
                        text = "Search Tips",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = WarningOrange
                    )
                    Text(
                        text = "Try searching for expense descriptions, amounts, group names, or category types. Use filters for more precise results.",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        lineHeight = 16.sp
                    )
                }
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

@Composable
private fun SearchSuggestionsSection(
    onSuggestionClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val recentSearches = listOf("Groceries", "Restaurant", "Gas", "Coffee", "Lunch")
    val popularSearches = listOf("Food & Dining", "Transportation", "Entertainment", "Bills")
    
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Recent Searches
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = NeutralWhite)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 12.dp)
                ) {
                    Icon(
                        Icons.Default.History,
                        contentDescription = "Recent",
                        tint = TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Recent Searches",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )
                }
                
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(recentSearches) { search ->
                        SuggestionChip(
                            text = search,
                            onClick = { onSuggestionClick(search) }
                        )
                    }
                }
            }
        }
        
        // Popular Searches
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = NeutralWhite)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 12.dp)
                ) {
                    Icon(
                        Icons.Default.TrendingUp,
                        contentDescription = "Popular",
                        tint = TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Popular Categories",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )
                }
                
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(popularSearches) { search ->
                        SuggestionChip(
                            text = search,
                            onClick = { onSuggestionClick(search) }
                        )
                    }
                }
            }
        }
        
        // Search Tips
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = InfoBlue.copy(alpha = 0.1f))
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Lightbulb,
                    contentDescription = "Tip",
                    tint = InfoBlue,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Search Tips",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = InfoBlue
                    )
                    Text(
                        text = "Try searching by description, category, amount, or who paid",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun SuggestionChip(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FilterChip(
        selected = false,
        onClick = onClick,
        label = { Text(text) },
        modifier = modifier,
        colors = FilterChipDefaults.filterChipColors(
            containerColor = LightBackground,
            labelColor = TextPrimary
        )
    )
}

private fun highlightSearchTerm(text: String, searchQuery: String): AnnotatedString {
    if (searchQuery.isBlank()) return AnnotatedString(text)
    
    return buildAnnotatedString {
        val lowerText = text.lowercase()
        val lowerQuery = searchQuery.lowercase()
        var lastIndex = 0
        
        while (lastIndex < text.length) {
            val index = lowerText.indexOf(lowerQuery, lastIndex)
            if (index == -1) {
                append(text.substring(lastIndex))
                break
            }
            
            // Add text before the match
            append(text.substring(lastIndex, index))
            
            // Add highlighted match
            withStyle(SpanStyle(color = Primary, fontWeight = FontWeight.Bold)) {
                append(text.substring(index, index + searchQuery.length))
            }
            
            lastIndex = index + searchQuery.length
        }
    }
}

private fun formatDate(date: Date): String {
    val formatter = java.text.SimpleDateFormat("MMM dd", java.util.Locale.getDefault())
    return formatter.format(date)
} 
