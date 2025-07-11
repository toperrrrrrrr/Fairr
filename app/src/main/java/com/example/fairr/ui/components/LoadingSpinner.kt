package com.example.fairr.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fairr.ui.theme.*
import kotlin.math.PI
import kotlin.math.sin

/**
 * Enhanced loading system with modern UX patterns
 */

@Composable
fun LoadingSpinner(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(48.dp),
            color = MaterialTheme.colorScheme.primary,
            strokeWidth = 4.dp
        )
    }
}

/**
 * Enhanced contextual loading component with modern animations
 */
@Composable
fun EnhancedLoadingState(
    message: String = "Loading...",
    subtitle: String? = null,
    showProgress: Boolean = false,
    progress: Float = 0f,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterVertically)
    ) {
        // Animated loading indicator
        AnimatedLoadingDots()
        
        // Main loading message
        Text(
            text = message,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Medium,
            color = TextPrimary,
            textAlign = TextAlign.Center
        )
        
        // Subtitle if provided
        subtitle?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )
        }
        
        // Progress bar if enabled
        if (showProgress) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .height(6.dp),
                    color = Primary,
                    trackColor = LightGray
                )
                Text(
                    text = "${(progress * 100).toInt()}%",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
        }
    }
}

/**
 * Modern skeleton loader for list items
 */
@Composable
fun SkeletonLoader(
    modifier: Modifier = Modifier,
    lines: Int = 3,
    showAvatar: Boolean = true
) {
    val shimmerColors = listOf(
        LightGray.copy(alpha = 0.3f),
        LightGray.copy(alpha = 0.6f),
        LightGray.copy(alpha = 0.3f)
    )

    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1500,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_translation"
    )

    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = androidx.compose.ui.geometry.Offset(translateAnim - 1000f, 0f),
        end = androidx.compose.ui.geometry.Offset(translateAnim, 0f)
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Avatar placeholder
        if (showAvatar) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(brush, CircleShape)
            )
        }
        
        // Content placeholders
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            repeat(lines) { index ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth(if (index == lines - 1) 0.7f else 1f)
                        .height(16.dp)
                        .background(brush, RoundedCornerShape(4.dp))
                )
            }
        }
    }
}

/**
 * Card-based skeleton loader
 */
@Composable
fun SkeletonCard(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = NeutralWhite),
        shape = RoundedCornerShape(12.dp)
    ) {
        SkeletonLoader(
            modifier = Modifier.padding(16.dp),
            lines = 2,
            showAvatar = true
        )
    }
}

/**
 * Animated loading dots with wave effect - OPTIMIZED VERSION
 */
@Composable
fun AnimatedLoadingDots(
    modifier: Modifier = Modifier,
    dotCount: Int = 3,
    color: Color = Primary
) {
    // Use single transition instead of multiple transitions for better performance
    val transition = rememberInfiniteTransition(label = "loading_dots")
    
    val animationProgress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1200,
                easing = EaseInOutCubic
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "dots_progress"
    )

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(dotCount) { index ->
            // Calculate individual dot scale based on progress and index
            val dotProgress = ((animationProgress * dotCount) - index).coerceIn(0f, 1f)
            val scale = 0.8f + (0.4f * sin(dotProgress * PI).toFloat())
            
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .scale(scale)
                    .background(color, CircleShape)
            )
        }
    }
}

/**
 * Loading overlay for full-screen operations
 */
@Composable
fun LoadingOverlay(
    isVisible: Boolean,
    message: String = "Processing...",
    onDismiss: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    if (isVisible) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            Card(
                colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = NeutralWhite),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.padding(32.dp)
            ) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(48.dp),
                        color = Primary,
                        strokeWidth = 4.dp
                    )
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextPrimary,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

/**
 * Progressive loading container that shows different states based on loading progress
 */
@Composable
fun ProgressiveLoadingContainer(
    isLoading: Boolean,
    progress: Float = 0f,
    loadingMessage: String = "Loading...",
    skeletonType: SkeletonType? = null,
    itemCount: Int = 5,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    when {
        isLoading && progress > 0f -> {
            EnhancedLoadingState(
                message = loadingMessage,
                showProgress = true,
                progress = progress,
                modifier = modifier
            )
        }
        isLoading && skeletonType != null -> {
            SkeletonLoadingScreen(
                type = skeletonType,
                itemCount = itemCount,
                modifier = modifier
            )
        }
        isLoading -> {
            LoadingSpinner(modifier = modifier)
        }
        else -> {
            content()
        }
    }
}

/**
 * Progressive list loading container with load more functionality
 */
@Composable
fun ProgressiveListContainer(
    isLoading: Boolean,
    isLoadingMore: Boolean,
    canLoadMore: Boolean,
    onLoadMore: () -> Unit,
    skeletonType: SkeletonType,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        when {
            isLoading -> {
                SkeletonLoadingScreen(
                    type = skeletonType,
                    itemCount = 5,
                    modifier = Modifier.weight(1f)
                )
            }
            else -> {
                Box(modifier = Modifier.weight(1f)) {
                    content()
                }
                
                if (isLoadingMore) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(32.dp),
                            color = Primary,
                            strokeWidth = 3.dp
                        )
                    }
                } else if (canLoadMore) {
                    TextButton(
                        onClick = onLoadMore,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text("Load More")
                    }
                }
            }
        }
    }
}

/**
 * Contextual loading messages for different operations
 */
object LoadingMessages {
    const val AUTHENTICATION = "Signing you in..."
    const val LOADING_PROFILE = "Loading your profile..."
    const val LOADING_GROUPS = "Fetching your groups..."
    const val LOADING_EXPENSES = "Loading expenses..."
    const val LOADING_SETTLEMENTS = "Calculating settlements..."
    const val LOADING_FRIENDS = "Loading your friends..."
    const val LOADING_NOTIFICATIONS = "Checking notifications..."
    const val PROCESSING_PAYMENT = "Processing payment..."
    const val SAVING_EXPENSE = "Saving expense..."
    const val CREATING_GROUP = "Creating group..."
    const val UPLOADING_IMAGE = "Uploading image..."
    const val SYNCING_DATA = "Syncing data..."
    const val LOADING_ANALYTICS = "Analyzing your data..."
    const val EXPORTING_DATA = "Preparing export..."
    
    fun getContextualMessage(operation: String): String = when (operation.lowercase()) {
        "auth", "authentication", "login", "signin" -> AUTHENTICATION
        "profile" -> LOADING_PROFILE
        "groups", "group" -> LOADING_GROUPS
        "expenses", "expense" -> LOADING_EXPENSES
        "settlements", "settlement" -> LOADING_SETTLEMENTS
        "friends", "friend" -> LOADING_FRIENDS
        "notifications", "notification" -> LOADING_NOTIFICATIONS
        "payment", "pay" -> PROCESSING_PAYMENT
        "save", "saving" -> SAVING_EXPENSE
        "create", "creating" -> CREATING_GROUP
        "upload", "uploading" -> UPLOADING_IMAGE
        "sync", "syncing" -> SYNCING_DATA
        "analytics", "analyze" -> LOADING_ANALYTICS
        "export", "exporting" -> EXPORTING_DATA
        else -> "Loading..."
    }
} 