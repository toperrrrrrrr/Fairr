package com.example.fairr.ui.screens.expenses

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.fairr.data.model.Expense
import com.example.fairr.data.model.ExpenseSplit
import com.example.fairr.ui.theme.*
import com.example.fairr.util.CurrencyFormatter
import androidx.compose.ui.draw.shadow
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.CallSplit
import androidx.compose.material.icons.automirrored.filled.Note
import androidx.compose.material.icons.automirrored.filled.Send
import com.example.fairr.ui.screens.expenses.CommentViewModel
import com.example.fairr.data.model.Comment
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import com.example.fairr.ui.components.ErrorType
import com.example.fairr.ui.components.ErrorUtils
import com.example.fairr.ui.components.StandardErrorState
import android.util.Log

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseDetailScreen(
    navController: NavController,
    expenseId: String,
    onEditExpense: () -> Unit = {},
    onDeleteExpense: () -> Unit = {},
    viewModel: ExpenseDetailViewModel = hiltViewModel()
) {
    val uiState = viewModel.uiState
    var showMenu by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    // Handle deletion events
    LaunchedEffect(Unit) {
        // Observe deletion events from ViewModel if available
        // For now, we'll handle deletion directly in the dialog
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Expense Details",
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
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(
                                Icons.Default.MoreVert,
                                contentDescription = "Options",
                                tint = TextSecondary
                            )
                        }
                        
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Edit") },
                                leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                                onClick = {
                                    showMenu = false
                                    Log.d("ExpenseDetailScreen", "Edit button clicked for expense: $expenseId")
                                    onEditExpense()
                                }
                            )
                            // Only show delete option if user is the expense creator or admin
                            if (uiState is ExpenseDetailUiState.Success) {
                                val expense = (uiState as ExpenseDetailUiState.Success).expense
                                val currentUserId = (uiState as ExpenseDetailUiState.Success).currentUserId
                                if (expense.paidBy == currentUserId) {
                                    DropdownMenuItem(
                                        text = { Text("Delete") },
                                        leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null) },
                                        onClick = {
                                            showMenu = false
                                            Log.d("ExpenseDetailScreen", "Delete button clicked for expense: $expenseId")
                                            showDeleteDialog = true
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            )
        }
    ) { padding ->
        when (uiState) {
            is ExpenseDetailUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Loading expense...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                }
            }
            is ExpenseDetailUiState.Error -> {
                StandardErrorState(
                    errorType = ErrorUtils.getErrorType(uiState.message),
                    customMessage = ErrorUtils.getUserFriendlyMessage(uiState.message),
                    onRetry = { viewModel.refresh() },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                )
            }
            is ExpenseDetailUiState.Success -> {
                val expense = uiState.expense
                val groupName = uiState.groupName
                val currentUserId = uiState.currentUserId
                
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(LightBackground)
                        .padding(padding),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    
                    // Expense Overview Card
                    item {
                        ExpenseOverviewCard(
                            expense = expense,
                            groupName = groupName,
                            viewModel = viewModel,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                    
                    // Split Details Section
                    item {
                        Text(
                            text = "Split Details",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                    
                    items(
                        items = expense.splitBetween,
                        key = { split -> "${split.userId}_${expense.id}" } // Composite key using userId and expenseId for uniqueness
                    ) { split ->
                        ParticipantCard(
                            split = split,
                            expense = expense,
                            currentUserId = currentUserId,
                            viewModel = viewModel,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                    
                    // Comments Section
                    item {
                        CommentsSection(
                            expense = expense,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }

    // Delete confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Expense") },
            text = { Text("Are you sure you want to delete this expense? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        Log.d("ExpenseDetailScreen", "Delete confirmed for expense: $expenseId")
                        // Delete the expense and handle the result
                        viewModel.deleteExpense(expenseId)
                        // Navigate back after a short delay to allow deletion to complete
                        // The ViewModel will handle the actual deletion
                        navController.popBackStack()
                    }
                ) {
                    Text("Delete", color = ErrorRed)
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

@Composable
fun ExpenseOverviewCard(
    expense: Expense,
    groupName: String,
    viewModel: ExpenseDetailViewModel,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = NeutralWhite)
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            // Header with description and icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = expense.description,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        lineHeight = 28.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = groupName,
                        fontSize = 15.sp,
                        color = TextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                // Enhanced icon with better styling
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(
                            DarkGreen.copy(alpha = 0.15f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Receipt,
                        contentDescription = "Expense",
                        tint = DarkGreen,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Enhanced amount display
            Column(
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "Total Amount",
                    fontSize = 14.sp,
                    color = TextSecondary,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = CurrencyFormatter.format(expense.currency, expense.amount),
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    lineHeight = 42.sp
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Enhanced details grid with better spacing
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    EnhancedDetailItem(
                        label = "Paid by",
                        value = expense.paidByName,
                        icon = Icons.Default.Person
                    )
                    EnhancedDetailItem(
                        label = "Date",
                        value = viewModel.formatDate(expense.date),
                        icon = Icons.Default.CalendarToday
                    )
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    EnhancedDetailItem(
                        label = "Category",
                        value = expense.category.name.replace("_", " ").capitalize(),
                        icon = Icons.Default.Category
                    )
                    EnhancedDetailItem(
                        label = "Split Type",
                        value = expense.splitType,
                        icon = Icons.AutoMirrored.Filled.CallSplit
                    )
                }
                
                if (expense.notes.isNotEmpty()) {
                    EnhancedDetailItem(
                        label = "Notes",
                        value = expense.notes,
                        icon = Icons.AutoMirrored.Filled.Note,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
fun EnhancedDetailItem(
    label: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                fontSize = 13.sp,
                color = TextSecondary,
                fontWeight = FontWeight.Medium
            )
        }
        Text(
            text = value,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary,
            lineHeight = 20.sp
        )
    }
}

@Composable
fun DetailItem(
    label: String,
    value: String
) {
    Column {
        Text(
            text = label,
            fontSize = 12.sp,
            color = TextSecondary,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary
        )
    }
}

@Composable
fun ParticipantCard(
    split: ExpenseSplit,
    expense: Expense,
    currentUserId: String,
    viewModel: ExpenseDetailViewModel,
    modifier: Modifier = Modifier
) {
    val isPayer = split.userId == expense.paidBy
    val isCurrentUser = split.userId == currentUserId
    
    Card(
        modifier = modifier
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
            // Avatar
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        if (isPayer) DarkGreen.copy(alpha = 0.2f) 
                        else if (isCurrentUser) DarkBlue.copy(alpha = 0.2f)
                        else PlaceholderText.copy(alpha = 0.2f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = "Participant",
                    tint = if (isPayer) DarkGreen 
                          else if (isCurrentUser) DarkBlue
                          else PlaceholderText,
                    modifier = Modifier.size(20.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Name and status
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (isCurrentUser) "${split.userName} (You)" else split.userName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )
                    if (isPayer) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            color = DarkGreen.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "PAID",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = DarkGreen,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
            
            // Amount
            Text(
                text = CurrencyFormatter.format(expense.currency, split.share),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
        }
    }
}

// Extension function to capitalize first letter
fun String.capitalize(): String {
    return if (isNotEmpty()) {
        this[0].uppercase() + substring(1).lowercase()
    } else {
        this
    }
}

@Composable
fun CommentsSection(
    expense: Expense,
    modifier: Modifier = Modifier,
    commentViewModel: CommentViewModel = hiltViewModel()
) {
    val commentState by commentViewModel.state.collectAsState()
    val events by commentViewModel.events.collectAsState()
    var newCommentText by remember { mutableStateOf("") }
    var editingCommentId by remember { mutableStateOf<String?>(null) }
    var editingText by remember { mutableStateOf("") }
    
    // Load comments when expense changes
    LaunchedEffect(expense.id) {
        commentViewModel.loadComments(expense.id)
    }
    
    // Handle comment events
    LaunchedEffect(events) {
        events?.let { event ->
            when (event) {
                is CommentEvent.CommentAdded -> {
                    newCommentText = ""
                    commentViewModel.clearEvents()
                }
                is CommentEvent.CommentUpdated -> {
                    editingCommentId = null
                    editingText = ""
                    commentViewModel.clearEvents()
                }
                is CommentEvent.CommentDeleted -> {
                    commentViewModel.clearEvents()
                }
                else -> {}
            }
        }
    }
    
    Card(
        modifier = modifier.padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = NeutralWhite)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Comments Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Comments",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Text(
                    text = "${commentState.comments.size}",
                    fontSize = 14.sp,
                    color = TextSecondary,
                    fontWeight = FontWeight.Medium
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Add Comment Section
            OutlinedTextField(
                value = newCommentText,
                onValueChange = { newCommentText = it },
                placeholder = { 
                    Text(
                        "Add a comment...",
                        color = PlaceholderText
                    ) 
                },
                modifier = Modifier.fillMaxWidth(),
                minLines = 1,
                maxLines = 3,
                trailingIcon = {
                    if (newCommentText.isNotBlank()) {
                        IconButton(
                            onClick = {
                                commentViewModel.addComment(
                                    expenseId = expense.id,
                                    groupId = expense.groupId,
                                    text = newCommentText,
                                    authorName = "Current User", // This should come from user profile
                                    authorPhotoUrl = ""
                                )
                            }
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Send comment",
                                tint = Primary
                            )
                        }
                    }
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(
                    onSend = {
                        if (newCommentText.isNotBlank()) {
                            commentViewModel.addComment(
                                expenseId = expense.id,
                                groupId = expense.groupId,
                                text = newCommentText,
                                authorName = "Current User",
                                authorPhotoUrl = ""
                            )
                        }
                    }
                )
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Comments List
            when {
                commentState.isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                commentState.comments.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.ChatBubbleOutline,
                                contentDescription = null,
                                tint = PlaceholderText,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No comments yet",
                                color = PlaceholderText,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Be the first to comment!",
                                color = PlaceholderText,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
                else -> {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        commentState.comments.forEach { comment ->
                            CommentItem(
                                comment = comment,
                                isEditing = editingCommentId == comment.id,
                                editingText = editingText,
                                onEditingTextChange = { editingText = it },
                                onStartEdit = { 
                                    editingCommentId = comment.id
                                    editingText = comment.text
                                },
                                onCancelEdit = {
                                    editingCommentId = null
                                    editingText = ""
                                },
                                onSaveEdit = {
                                    commentViewModel.updateComment(
                                        expenseId = expense.id,
                                        commentId = comment.id,
                                        newText = editingText
                                    )
                                },
                                onDelete = {
                                    commentViewModel.deleteComment(
                                        expenseId = expense.id,
                                        commentId = comment.id
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CommentItem(
    comment: Comment,
    isEditing: Boolean,
    editingText: String,
    onEditingTextChange: (String) -> Unit,
    onStartEdit: () -> Unit,
    onCancelEdit: () -> Unit,
    onSaveEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Surface.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            // Comment Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Avatar
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(Primary.copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            tint = Primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Column {
                        Text(
                            text = comment.authorName,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )
                        Text(
                            text = "2 hours ago", // This should be calculated from comment.timestamp
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                }
                
                // Actions menu for comment author
                Row {
                    IconButton(
                        onClick = onStartEdit,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Edit comment",
                            tint = TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete comment",
                            tint = ErrorRed,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Comment Content
            if (isEditing) {
                OutlinedTextField(
                    value = editingText,
                    onValueChange = onEditingTextChange,
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 1,
                    maxLines = 3
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextButton(onClick = onCancelEdit) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(onClick = onSaveEdit) {
                        Text("Save")
                    }
                }
            } else {
                Text(
                    text = comment.text,
                    fontSize = 14.sp,
                    color = TextPrimary,
                    lineHeight = 20.sp
                )
                
                if (comment.isEdited) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "(edited)",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ExpenseDetailScreenPreview() {
    FairrTheme {
        ExpenseDetailScreen(
            navController = rememberNavController(),
            expenseId = "1"
        )
    }
} 
