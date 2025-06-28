package com.example.fairr.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.fairr.ui.theme.*
import kotlinx.coroutines.delay
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items

/**
 * Custom Chip Components
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FairrFilterChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    enabled: Boolean = true
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
        modifier = modifier,
        enabled = enabled,
        leadingIcon = leadingIcon?.let {
            {
                Icon(
                    imageVector = it,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }
        },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = DarkGreen,
            selectedLabelColor = NeutralWhite,
            selectedLeadingIconColor = NeutralWhite,
            containerColor = NeutralWhite,
            labelColor = TextPrimary,
            iconColor = TextSecondary
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FairrActionChip(
    onClick: () -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    enabled: Boolean = true
) {
    AssistChip(
        onClick = onClick,
        label = { Text(label) },
        modifier = modifier,
        enabled = enabled,
        leadingIcon = leadingIcon?.let {
            {
                Icon(
                    imageVector = it,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }
        },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = NeutralWhite,
            labelColor = TextPrimary,
            leadingIconContentColor = TextSecondary
        )
    )
}

/**
 * Custom Dialog Components
 */
@Composable
fun FairrConfirmationDialog(
    title: String,
    message: String,
    confirmText: String = "Confirm",
    dismissText: String = "Cancel",
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    isDestructive: Boolean = false,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
        },
        text = {
            Text(
                text = message,
                color = TextSecondary
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isDestructive) ErrorRed else DarkGreen
                )
            ) {
                Text(confirmText, color = NeutralWhite)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(dismissText, color = TextSecondary)
            }
        },
        modifier = modifier,
        containerColor = NeutralWhite,
        titleContentColor = TextPrimary,
        textContentColor = TextSecondary
    )
}

@Composable
fun FairrLoadingDialog(
    isVisible: Boolean,
    message: String = "Loading...",
    onDismiss: () -> Unit = {}
) {
    if (isVisible) {
        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(
                dismissOnBackPress = false,
                dismissOnClickOutside = false
            )
        ) {
            Card(
                modifier = Modifier
                    .wrapContentSize()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = NeutralWhite)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(48.dp),
                        color = DarkGreen,
                        strokeWidth = 4.dp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = message,
                        fontSize = 16.sp,
                        color = TextPrimary,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

/**
 * Loading States
 */
@Composable
fun FairrLoadingCard(
    modifier: Modifier = Modifier,
    message: String = "Loading..."
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = NeutralWhite)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(
                color = DarkGreen,
                strokeWidth = 3.dp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = message,
                fontSize = 14.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun FairrSkeletonLoader(
    modifier: Modifier = Modifier,
    height: Int = 16,
    cornerRadius: Int = 4
) {
    val shimmerColors = listOf(
        PlaceholderText.copy(alpha = 0.2f),
        PlaceholderText.copy(alpha = 0.4f),
        PlaceholderText.copy(alpha = 0.2f)
    )

    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1000,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer"
    )

    Box(
        modifier = modifier
            .height(height.dp)
            .background(
                brush = Brush.linearGradient(
                    colors = shimmerColors,
                    start = Offset(translateAnim - 1000f, 0f),
                    end = Offset(translateAnim, 0f)
                ),
                shape = RoundedCornerShape(cornerRadius.dp)
            )
    )
}

/**
 * Error States
 */
@Composable
fun FairrErrorState(
    title: String = "Something went wrong",
    message: String = "Please try again later",
    actionText: String = "Retry",
    onActionClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Default.ErrorOutline
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = "Error",
            modifier = Modifier.size(64.dp),
            tint = ErrorRed
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = message,
            fontSize = 14.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = onActionClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = DarkGreen
            )
        ) {
            Text(actionText, color = NeutralWhite)
        }
    }
}

@Composable
fun FairrNetworkErrorState(
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FairrErrorState(
        title = "No Internet Connection",
        message = "Please check your internet connection and try again",
        actionText = "Retry",
        onActionClick = onRetryClick,
        modifier = modifier,
        icon = Icons.Default.WifiOff
    )
}

/**
 * Empty States
 */
@Composable
fun FairrEmptyState(
    title: String,
    message: String,
    actionText: String? = null,
    onActionClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Default.Inbox
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(
                    PlaceholderText.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(16.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = "Empty",
                modifier = Modifier.size(40.dp),
                tint = PlaceholderText
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = title,
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = message,
            fontSize = 14.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )
        
        if (actionText != null) {
            Spacer(modifier = Modifier.height(24.dp))
            
            OutlinedButton(
                onClick = onActionClick,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = DarkGreen
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkGreen)
            ) {
                Text(actionText)
            }
        }
    }
}

/**
 * Snackbar Components
 */
@Composable
fun FairrSnackbarHost(
    hostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
    SnackbarHost(
        hostState = hostState,
        modifier = modifier
    ) { data ->
        Snackbar(
            snackbarData = data,
            containerColor = DarkBackground,
            contentColor = NeutralWhite,
            actionColor = DarkGreen,
            shape = RoundedCornerShape(8.dp)
        )
    }
}

/**
 * Success/Info Messages
 */
@Composable
fun FairrSuccessMessage(
    message: String,
    isVisible: Boolean,
    onDismiss: () -> Unit = {},
    duration: Long = 3000,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(isVisible) {
        if (isVisible) {
            delay(duration)
            onDismiss()
        }
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(
            initialOffsetY = { -it },
            animationSpec = tween(300)
        ) + fadeIn(),
        exit = slideOutVertically(
            targetOffsetY = { -it },
            animationSpec = tween(300)
        ) + fadeOut()
    ) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = SuccessGreen
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = "Success",
                    tint = NeutralWhite,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = message,
                    color = NeutralWhite,
                    fontSize = 14.sp,
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Dismiss",
                        tint = NeutralWhite,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

/**
 * Animated Components
 */
@Composable
fun FairrAnimatedCounter(
    count: Int,
    modifier: Modifier = Modifier,
    textColor: Color = TextPrimary,
    fontSize: Int = 24
) {
    var oldCount by remember { mutableStateOf(count) }
    val animatedCount by animateIntAsState(
        targetValue = count,
        animationSpec = tween(durationMillis = 500, easing = EaseOutCubic),
        label = "counter_animation"
    )

    LaunchedEffect(count) {
        oldCount = count
    }

    Text(
        text = animatedCount.toString(),
        color = textColor,
        fontSize = fontSize.sp,
        fontWeight = FontWeight.Bold,
        modifier = modifier
    )
}

@Composable
fun FairrPulsingIcon(
    icon: ImageVector,
    modifier: Modifier = Modifier,
    tint: Color = DarkGreen,
    size: Int = 24
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ), label = "pulse_scale"
    )

    Icon(
        imageVector = icon,
        contentDescription = null,
        tint = tint,
        modifier = modifier
            .size(size.dp)
            .scale(scale)
    )
}

/**
 * A Box that dismisses the keyboard when tapped outside of text input fields
 * Use this to wrap content that contains text input fields
 */
@Composable
fun KeyboardDismissibleBox(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .fillMaxSize()
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) {
                focusManager.clearFocus()
                keyboardController?.hide()
            }
    ) {
        content()
    }
}

/**
 * Animation Components
 */
@Composable
fun FairrAnimatedVisibility(
    visible: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(
            initialOffsetY = { it / 2 },
            animationSpec = tween(300, easing = EaseOutCubic)
        ) + fadeIn(
            animationSpec = tween(300)
        ),
        exit = slideOutVertically(
            targetOffsetY = { it / 2 },
            animationSpec = tween(300, easing = EaseInCubic)
        ) + fadeOut(
            animationSpec = tween(300)
        ),
        modifier = modifier
    ) {
        content()
    }
}

@Composable
fun FairrAnimatedListItem(
    visible: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(
            initialOffsetY = { 50 },
            animationSpec = tween(400, easing = EaseOutCubic)
        ) + fadeIn(
            animationSpec = tween(400)
        ),
        modifier = modifier
    ) {
        content()
    }
}

@Composable
fun FairrScaleInAnimation(
    visible: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = scaleIn(
            initialScale = 0.8f,
            animationSpec = tween(300, easing = EaseOutBack)
        ) + fadeIn(
            animationSpec = tween(300)
        ),
        exit = scaleOut(
            targetScale = 0.8f,
            animationSpec = tween(300, easing = EaseInBack)
        ) + fadeOut(
            animationSpec = tween(300)
        ),
        modifier = modifier
    ) {
        content()
    }
}

/**
 * Loading Animation Components
 */
@Composable
fun FairrPulsingDot(
    modifier: Modifier = Modifier,
    color: Color = DarkGreen
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Box(
        modifier = modifier
            .size(8.dp)
            .scale(scale)
            .background(color, CircleShape)
    )
}

@Composable
fun FairrLoadingDots(
    modifier: Modifier = Modifier,
    color: Color = DarkGreen
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        repeat(3) { index ->
            val delay = index * 200
            val infiniteTransition = rememberInfiniteTransition(label = "dot_$index")
            val scale by infiniteTransition.animateFloat(
                initialValue = 0.8f,
                targetValue = 1.2f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1000, delayMillis = delay, easing = EaseInOutCubic),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "dot_scale_$index"
            )

            Box(
                modifier = Modifier
                    .size(8.dp)
                    .scale(scale)
                    .background(color, CircleShape)
            )
        }
    }
}

/**
 * Interactive Components with Haptic Feedback
 */
@Composable
fun FairrInteractiveButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit
) {
    val hapticFeedback = LocalHapticFeedback.current
    
    Button(
        onClick = {
            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
            onClick()
        },
        modifier = modifier,
        enabled = enabled,
        content = content
    )
}

@Composable
fun FairrInteractiveCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable () -> Unit
) {
    val hapticFeedback = LocalHapticFeedback.current
    
    Card(
        modifier = modifier.clickable(
            enabled = enabled,
            onClick = {
                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                onClick()
            }
        )
    ) {
        content()
    }
}

@Composable
fun FairrInteractiveChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    enabled: Boolean = true
) {
    val hapticFeedback = LocalHapticFeedback.current
    
    FilterChip(
        selected = selected,
        onClick = {
            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
            onClick()
        },
        label = { Text(label) },
        modifier = modifier,
        enabled = enabled,
        leadingIcon = leadingIcon?.let {
            {
                Icon(
                    imageVector = it,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }
        },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = DarkGreen,
            selectedLabelColor = NeutralWhite,
            selectedLeadingIconColor = NeutralWhite,
            containerColor = NeutralWhite,
            labelColor = TextPrimary,
            iconColor = TextSecondary
        )
    )
}

/**
 * Progress Indicators
 */
@Composable
fun FairrProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    color: Color = DarkGreen
) {
    LinearProgressIndicator(
        progress = progress,
        modifier = modifier,
        color = color,
        trackColor = color.copy(alpha = 0.2f)
    )
}

@Composable
fun FairrCircularProgress(
    progress: Float,
    modifier: Modifier = Modifier,
    color: Color = DarkGreen,
    strokeWidth: Dp = 4.dp
) {
    CircularProgressIndicator(
        progress = progress,
        modifier = modifier,
        color = color,
        strokeWidth = strokeWidth,
        trackColor = color.copy(alpha = 0.2f)
    )
}

/**
 * Category Selection Components
 */
@Composable
fun CategoryChip(
    category: com.example.fairr.data.model.ExpenseCategory,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (selected) {
        Color(android.graphics.Color.parseColor(category.color))
    } else {
        MaterialTheme.colorScheme.surface
    }
    
    val textColor = if (selected) {
        Color.White
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Card(
        modifier = modifier
            .clickable { onClick() }
            .padding(4.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        border = if (!selected) {
            androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        } else null
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = category.icon,
                fontSize = 16.sp
            )
            Text(
                text = category.displayName,
                color = textColor,
                fontSize = 12.sp,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
            )
        }
    }
}

@Composable
fun CategorySelectionGrid(
    selectedCategory: com.example.fairr.data.model.ExpenseCategory,
    onCategorySelected: (com.example.fairr.data.model.ExpenseCategory) -> Unit,
    modifier: Modifier = Modifier
) {
    val categories = com.example.fairr.data.model.ExpenseCategory.values()
    
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(categories) { category ->
            CategoryChip(
                category = category,
                selected = selectedCategory == category,
                onClick = { onCategorySelected(category) },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun CategoryIcon(
    category: com.example.fairr.data.model.ExpenseCategory,
    modifier: Modifier = Modifier,
    size: Dp = 24.dp
) {
    Box(
        modifier = modifier
            .size(size)
            .background(
                Color(android.graphics.Color.parseColor(category.color)),
                CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = category.icon,
            fontSize = (size.value * 0.6).sp
        )
    }
}

// Preview Components
@Preview(showBackground = true)
@Composable
fun ComponentsPreview() {
    FairrTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            FairrFilterChip(
                selected = true,
                onClick = {},
                label = "Selected",
                leadingIcon = Icons.Default.Star
            )
            
            FairrActionChip(
                onClick = {},
                label = "Action",
                leadingIcon = Icons.Default.Add
            )
            
            FairrLoadingCard(message = "Loading your data...")
            
            FairrEmptyState(
                title = "No Data",
                message = "There's nothing here yet",
                actionText = "Add Something",
                icon = Icons.Default.Inbox
            )
            
            FairrLoadingDots()
            
            FairrProgressBar(progress = 0.7f)
            
            FairrCircularProgress(progress = 0.5f)
        }
    }
} 
