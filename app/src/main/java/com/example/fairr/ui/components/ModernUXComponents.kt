package com.example.fairr.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fairr.ui.theme.*

/**
 * Enhanced Bottom Navigation with Centered FAB
 * Implements the design shown in the reference images with FAB positioned in the center
 */
@Composable
fun EnhancedBottomNavigation(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    onFabClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val navItems = listOf(
        BottomNavItem("Home", Icons.Filled.Home, Icons.Outlined.Home),
        BottomNavItem("Groups", Icons.Filled.Group, Icons.Outlined.Group),
        BottomNavItem("Analytics", Icons.Filled.Analytics, Icons.Outlined.Analytics),
        BottomNavItem("Profile", Icons.Filled.Person, Icons.Outlined.Person)
    )
    
    Box(
        modifier = modifier.fillMaxWidth()
    ) {
        // Bottom Navigation Bar
        NavigationBar(
            containerColor = NavBackground,
            tonalElevation = 0.dp,
            windowInsets = WindowInsets(0.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            navItems.forEachIndexed { index, item ->
                // Add spacing for FAB in the middle (between index 1 and 2)
                if (index == 2) {
                    Spacer(modifier = Modifier.weight(1f))
                }
                
                NavigationBarItem(
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == index) item.selectedIcon else item.unselectedIcon,
                            contentDescription = item.title,
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = {
                        Text(
                            text = item.title,
                            fontSize = 12.sp,
                            fontWeight = if (selectedTab == index) FontWeight.SemiBold else FontWeight.Medium
                        )
                    },
                    selected = selectedTab == index,
                    onClick = { onTabSelected(index) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = NavSelected,
                        selectedTextColor = NavSelected,
                        unselectedIconColor = NavUnselected,
                        unselectedTextColor = NavUnselected,
                        indicatorColor = NavSelected.copy(alpha = 0.1f)
                    ),
                    modifier = Modifier.weight(1f)
                )
            }
        }
        
        // Centered FAB
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-28).dp), // Raise the FAB above the nav bar
            contentAlignment = Alignment.Center
        ) {
            FloatingActionButton(
                onClick = onFabClick,
                modifier = Modifier.size(56.dp),
                containerColor = Primary,
                contentColor = TextOnDark,
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 8.dp,
                    pressedElevation = 12.dp
                ),
                shape = CircleShape
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Expense",
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

/**
 * Modern Bottom Navigation Bar
 * Implements the clean design system with proper spacing and modern styling
 */
@Composable
fun ModernBottomNavigation(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val navItems = listOf(
        BottomNavItem("Home", Icons.Filled.Home, Icons.Outlined.Home),
        BottomNavItem("Groups", Icons.Filled.Group, Icons.Outlined.Group),
        BottomNavItem("Analytics", Icons.Filled.Analytics, Icons.Outlined.Analytics),
        BottomNavItem("Profile", Icons.Filled.Person, Icons.Outlined.Person)
    )
    
    NavigationBar(
        modifier = modifier,
        containerColor = NavBackground,
        tonalElevation = 0.dp,
        windowInsets = WindowInsets(0.dp)
    ) {
        navItems.forEachIndexed { index, item ->
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = if (selectedTab == index) item.selectedIcon else item.unselectedIcon,
                        contentDescription = item.title,
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = {
                    Text(
                        text = item.title,
                        fontSize = 12.sp,
                        fontWeight = if (selectedTab == index) FontWeight.SemiBold else FontWeight.Medium
                    )
                },
                selected = selectedTab == index,
                onClick = { onTabSelected(index) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = NavSelected,
                    selectedTextColor = NavSelected,
                    unselectedIconColor = NavUnselected,
                    unselectedTextColor = NavUnselected,
                    indicatorColor = NavSelected.copy(alpha = 0.1f)
                )
            )
        }
    }
}

data class BottomNavItem(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

/**
 * Modern Floating Action Button
 * Follows the design system with proper shadows and modern styling
 */
@Composable
fun ModernFAB(
    onClick: () -> Unit,
    icon: ImageVector = Icons.Default.Add,
    contentDescription: String = "Add",
    modifier: Modifier = Modifier,
    backgroundColor: Color = Primary,
    contentColor: Color = TextOnDark
) {
    FloatingActionButton(
        onClick = onClick,
        modifier = modifier.size(56.dp),
        containerColor = backgroundColor,
        contentColor = contentColor,
        elevation = FloatingActionButtonDefaults.elevation(
            defaultElevation = 8.dp,
            pressedElevation = 12.dp
        ),
        shape = CircleShape
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier.size(24.dp)
        )
    }
}

/**
 * Modern Loading States
 * Beautiful loading indicators that match the design system
 */
@Composable
fun ModernLoadingIndicator(
    modifier: Modifier = Modifier,
    size: Int = 48,
    strokeWidth: Int = 4
) {
    CircularProgressIndicator(
        modifier = modifier.size(size.dp),
        strokeWidth = strokeWidth.dp,
        color = Primary,
        trackColor = LightGray
    )
}

@Composable
fun ModernLoadingCard(
    title: String = "Loading...",
    subtitle: String? = null,
    modifier: Modifier = Modifier
) {
    ModernCard(modifier = modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ModernLoadingIndicator(size = 32, strokeWidth = 3)
            
            Column {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary
                )
                subtitle?.let {
                    Text(
                        text = it,
                        fontSize = 14.sp,
                        color = TextSecondary
                    )
                }
            }
        }
    }
}

@Composable
fun ModernSkeletonLoader(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "skeleton")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(60.dp)
            .background(
                color = LightGray.copy(alpha = alpha),
                shape = RoundedCornerShape(8.dp)
            )
    )
}

/**
 * Modern Empty States
 * Clean empty state components for better UX
 */
@Composable
fun ModernEmptyState(
    title: String,
    subtitle: String,
    icon: ImageVector,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Icon Container
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(
                    color = LightGray,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = IconTint
            )
        }
        
        // Text Content
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                textAlign = TextAlign.Center
            )
            
            Text(
                text = subtitle,
                fontSize = 16.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                lineHeight = 24.sp
            )
        }
        
        // Action Button
        if (actionText != null && onActionClick != null) {
            Spacer(modifier = Modifier.height(8.dp))
            ModernButton(
                text = actionText,
                onClick = onActionClick,
                variant = ModernButtonVariant.Outline
            )
        }
    }
}

/**
 * Modern Error States
 * Error handling components with retry functionality
 */
@Composable
fun ModernErrorState(
    title: String = "Something went wrong",
    subtitle: String = "We encountered an error while loading your data.",
    onRetryClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    ModernCard(
        modifier = modifier,
        backgroundColor = ErrorRed.copy(alpha = 0.05f)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Error Icon
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(
                        color = ErrorRed.copy(alpha = 0.1f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ErrorOutline,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = ErrorRed
                )
            }
            
            // Error Text
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                    textAlign = TextAlign.Center
                )
                
                Text(
                    text = subtitle,
                    fontSize = 14.sp,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )
            }
            
            // Retry Button
            onRetryClick?.let { retry ->
                ModernButton(
                    text = "Try Again",
                    onClick = retry,
                    icon = Icons.Default.Refresh,
                    variant = ModernButtonVariant.Secondary
                )
            }
        }
    }
}

/**
 * Modern Success/Info Banners
 * For showing success messages and important information
 */
@Composable
fun ModernSuccessBanner(
    message: String,
    onDismiss: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    ModernCard(
        modifier = modifier,
        backgroundColor = AccentGreen.copy(alpha = 0.1f)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = AccentGreen,
                modifier = Modifier.size(24.dp)
            )
            
            Text(
                text = message,
                fontSize = 14.sp,
                color = TextPrimary,
                modifier = Modifier.weight(1f)
            )
            
            onDismiss?.let { dismiss ->
                IconButton(
                    onClick = dismiss,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Dismiss",
                        tint = TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ModernInfoBanner(
    message: String,
    onDismiss: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    ModernCard(
        modifier = modifier,
        backgroundColor = AccentBlue.copy(alpha = 0.1f)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = AccentBlue,
                modifier = Modifier.size(24.dp)
            )
            
            Text(
                text = message,
                fontSize = 14.sp,
                color = TextPrimary,
                modifier = Modifier.weight(1f)
            )
            
            onDismiss?.let { dismiss ->
                IconButton(
                    onClick = dismiss,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Dismiss",
                        tint = TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

/**
 * Modern Search Bar
 * Clean search input with modern styling
 */
@Composable
fun ModernSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String = "Search...",
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = { Text(placeholder, color = PlaceholderText) },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = IconTint
            )
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Clear",
                        tint = IconTint
                    )
                }
            }
        },
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = InputFocused,
            unfocusedBorderColor = InputBorder,
            focusedContainerColor = InputBackground,
            unfocusedContainerColor = InputBackground
        ),
        enabled = enabled,
        singleLine = true
    )
}

/**
 * Modern App Bar with consistent styling
 */
@Composable
fun ModernTopAppBar(
    title: String,
    navigationIcon: ImageVector? = null,
    onNavigationClick: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    modifier: Modifier = Modifier
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
        },
        navigationIcon = {
            if (navigationIcon != null && onNavigationClick != null) {
                IconButton(onClick = onNavigationClick) {
                    Icon(
                        imageVector = navigationIcon,
                        contentDescription = "Navigation",
                        tint = TextPrimary
                    )
                }
            }
        },
        actions = actions,
        modifier = modifier,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = BackgroundPrimary
        )
    )
}

/**
 * Modern Pull-to-Refresh Implementation
 */
@Composable
fun ModernPullToRefresh(
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(modifier = modifier) {
        content()
        
        if (isRefreshing) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                ModernLoadingIndicator(size = 32, strokeWidth = 3)
            }
        }
    }
}

/**
 * Preview Components
 */
@Preview(showBackground = true)
@Composable
fun ModernUXComponentsPreview() {
    FairrTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundSecondary)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Loading State
            ModernLoadingCard(
                title = "Loading your data",
                subtitle = "Please wait..."
            )
            
            // Success Banner
            ModernSuccessBanner(
                message = "Your expense has been added successfully!",
                onDismiss = {}
            )
            
            // Empty State
            ModernEmptyState(
                title = "No expenses yet",
                subtitle = "Start by adding your first expense to track your spending.",
                icon = Icons.Default.Receipt,
                actionText = "Add Expense",
                onActionClick = {}
            )
            
            // Search Bar
            var searchQuery by remember { mutableStateOf("") }
            ModernSearchBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                placeholder = "Search expenses..."
            )
        }
    }
} 