package com.example.fairr.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

/**
 * Standardized toolbar with back navigation
 * Eliminates code duplication across multiple screens
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FairrTopAppBar(
    title: String,
    navController: NavController? = null,
    onNavigateBack: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    showBackButton: Boolean = true,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        navigationIcon = {
            if (showBackButton) {
                IconButton(
                    onClick = {
                        onNavigateBack?.invoke() ?: navController?.popBackStack()
                    }
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Navigate back"
                    )
                }
            }
        },
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
            actionIconContentColor = MaterialTheme.colorScheme.onSurface
        ),
        modifier = modifier
    )
}

/**
 * Toolbar with search functionality
 * Standardizes search UI patterns across screens
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FairrSearchTopAppBar(
    title: String,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onSearchActiveChange: (Boolean) -> Unit,
    isSearchActive: Boolean = false,
    navController: NavController? = null,
    onNavigateBack: (() -> Unit)? = null,
    placeholder: String = "Search...",
    modifier: Modifier = Modifier
) {
    if (isSearchActive) {
        SearchBar(
            query = searchQuery,
            onQueryChange = onSearchQueryChange,
            onSearch = { onSearchActiveChange(false) },
            active = isSearchActive,
            onActiveChange = onSearchActiveChange,
            placeholder = { 
                Text(
                    text = placeholder,
                    style = MaterialTheme.typography.bodyLarge
                ) 
            },
            leadingIcon = {
                IconButton(
                    onClick = {
                        if (searchQuery.isEmpty()) {
                            onSearchActiveChange(false)
                        } else {
                            onSearchQueryChange("")
                        }
                    }
                ) {
                    Icon(
                        imageVector = if (searchQuery.isEmpty()) Icons.AutoMirrored.Filled.ArrowBack else Icons.Default.Clear,
                        contentDescription = if (searchQuery.isEmpty()) "Back" else "Clear search"
                    )
                }
            },
            modifier = modifier.fillMaxWidth()
        ) {}
    } else {
        FairrTopAppBar(
            title = title,
            navController = navController,
            onNavigateBack = onNavigateBack,
            actions = {
                IconButton(onClick = { onSearchActiveChange(true) }) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search"
                    )
                }
            },
            modifier = modifier
        )
    }
}

/**
 * Toolbar with menu actions
 * Standardizes action menu patterns
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FairrMenuTopAppBar(
    title: String,
    navController: NavController? = null,
    onNavigateBack: (() -> Unit)? = null,
    menuItems: List<ToolbarMenuItem>,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }
    
    FairrTopAppBar(
        title = title,
        navController = navController,
        onNavigateBack = onNavigateBack,
        actions = {
            IconButton(onClick = { showMenu = true }) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "More options"
                )
            }
            
            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false }
            ) {
                menuItems.forEach { item ->
                    DropdownMenuItem(
                        text = { 
                            Text(
                                text = item.text,
                                style = MaterialTheme.typography.bodyLarge
                            ) 
                        },
                        onClick = {
                            showMenu = false
                            item.onClick()
                        },
                        leadingIcon = item.icon?.let { icon ->
                            {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = item.iconTint ?: MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    )
                }
            }
        },
        modifier = modifier
    )
}

/**
 * Toolbar with single action button
 * For screens with one primary action
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FairrActionTopAppBar(
    title: String,
    navController: NavController? = null,
    onNavigateBack: (() -> Unit)? = null,
    actionIcon: ImageVector,
    actionContentDescription: String,
    onActionClick: () -> Unit,
    actionEnabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    FairrTopAppBar(
        title = title,
        navController = navController,
        onNavigateBack = onNavigateBack,
        actions = {
            IconButton(
                onClick = onActionClick,
                enabled = actionEnabled
            ) {
                Icon(
                    imageVector = actionIcon,
                    contentDescription = actionContentDescription,
                    tint = if (actionEnabled) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                    }
                )
            }
        },
        modifier = modifier
    )
}

/**
 * Centered toolbar for modal screens
 * No back navigation, centered title
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FairrCenteredTopAppBar(
    title: String,
    actions: @Composable RowScope.() -> Unit = {},
    modifier: Modifier = Modifier
) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        actions = actions,
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            actionIconContentColor = MaterialTheme.colorScheme.onSurface
        ),
        modifier = modifier
    )
}

/**
 * Data class for toolbar menu items
 */
data class ToolbarMenuItem(
    val text: String,
    val onClick: () -> Unit,
    val icon: ImageVector? = null,
    val iconTint: androidx.compose.ui.graphics.Color? = null
)

/**
 * Reusable standard dialog cancel/confirm buttons
 * Eliminates button layout duplication
 */
@Composable
fun FairrDialogButtons(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    dismissText: String = "Cancel",
    confirmText: String = "OK",
    confirmEnabled: Boolean = true,
    isDestructive: Boolean = false,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
    ) {
        TextButton(onClick = onDismiss) {
            Text(
                text = dismissText,
                style = MaterialTheme.typography.labelLarge
            )
        }
        
        TextButton(
            onClick = onConfirm,
            enabled = confirmEnabled,
            colors = if (isDestructive) {
                ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            } else {
                ButtonDefaults.textButtonColors()
            }
        ) {
            Text(
                text = confirmText,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

/**
 * Standardized empty state component
 * Reduces duplication of empty state layouts
 */
@Composable
fun FairrEmptyState(
    icon: ImageVector,
    title: String,
    description: String,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Text(
            text = description,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        
        if (actionText != null && onActionClick != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = onActionClick,
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Text(
                    text = actionText,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

/**
 * Standardized error state component
 * Reduces duplication of error state layouts
 */
@Composable
fun FairrErrorState(
    title: String = "Something went wrong",
    description: String,
    onRetry: (() -> Unit)? = null,
    retryText: String = "Try again",
    modifier: Modifier = Modifier
) {
    FairrEmptyState(
        icon = Icons.Default.ErrorOutline,
        title = title,
        description = description,
        actionText = if (onRetry != null) retryText else null,
        onActionClick = onRetry,
        modifier = modifier
    )
}

/**
 * Standardized loading state component
 * Reduces duplication of loading layouts
 */
@Composable
fun FairrLoadingState(
    message: String = "Loading...",
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(32.dp)
        )
        
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}