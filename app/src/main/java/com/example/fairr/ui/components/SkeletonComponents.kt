package com.example.fairr.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import com.example.fairr.ui.theme.*

/**
 * Base shimmer effect that can be reused across different skeleton components
 */
@Composable
private fun ShimmerBrush(): Brush {
    val shimmerColors = listOf(
        LightGray.copy(alpha = 0.2f),
        LightGray.copy(alpha = 0.4f),
        LightGray.copy(alpha = 0.2f)
    )

    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1200,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer"
    )

    return Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(translateAnim - 1000f, 0f),
        end = Offset(translateAnim, 0f)
    )
}

/**
 * Expense item skeleton with amount and category
 */
@Composable
fun ExpenseSkeletonItem(
    modifier: Modifier = Modifier
) {
    val brush = ShimmerBrush()
    
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = NeutralWhite),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Category icon placeholder
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(brush, CircleShape)
                )
                
                // Description and date
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .width(120.dp)
                            .height(16.dp)
                            .background(brush, RoundedCornerShape(4.dp))
                    )
                    Box(
                        modifier = Modifier
                            .width(80.dp)
                            .height(12.dp)
                            .background(brush, RoundedCornerShape(4.dp))
                    )
                }
            }
            
            // Amount
            Box(
                modifier = Modifier
                    .width(80.dp)
                    .height(20.dp)
                    .background(brush, RoundedCornerShape(4.dp))
            )
        }
    }
}

/**
 * Friend item skeleton with avatar and name
 */
@Composable
fun FriendSkeletonItem(
    modifier: Modifier = Modifier
) {
    val brush = ShimmerBrush()
    
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar placeholder
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(brush, CircleShape)
        )
        
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .width(120.dp)
                    .height(16.dp)
                    .background(brush, RoundedCornerShape(4.dp))
            )
            Box(
                modifier = Modifier
                    .width(80.dp)
                    .height(12.dp)
                    .background(brush, RoundedCornerShape(4.dp))
            )
        }
    }
}

/**
 * Group item skeleton with group avatar and details
 */
@Composable
fun GroupSkeletonItem(
    modifier: Modifier = Modifier
) {
    val brush = ShimmerBrush()
    
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = NeutralWhite),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Group avatar
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(brush, RoundedCornerShape(12.dp))
            )
            
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Group name
                Box(
                    modifier = Modifier
                        .width(160.dp)
                        .height(20.dp)
                        .background(brush, RoundedCornerShape(4.dp))
                )
                
                // Member count
                Box(
                    modifier = Modifier
                        .width(100.dp)
                        .height(14.dp)
                        .background(brush, RoundedCornerShape(4.dp))
                )
                
                // Balance
                Box(
                    modifier = Modifier
                        .width(120.dp)
                        .height(16.dp)
                        .background(brush, RoundedCornerShape(4.dp))
                )
            }
        }
    }
}

/**
 * Settlement skeleton item
 */
@Composable
fun SettlementSkeletonItem(
    modifier: Modifier = Modifier
) {
    val brush = ShimmerBrush()
    
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = NeutralWhite),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Users
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(brush, CircleShape)
                    )
                    Box(
                        modifier = Modifier
                            .width(100.dp)
                            .height(16.dp)
                            .background(brush, RoundedCornerShape(4.dp))
                    )
                }
                
                // Amount
                Box(
                    modifier = Modifier
                        .width(80.dp)
                        .height(20.dp)
                        .background(brush, RoundedCornerShape(4.dp))
                )
            }
            
            // Progress bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .background(brush, RoundedCornerShape(4.dp))
            )
        }
    }
}

/**
 * Extension function to add skeleton loading items to a LazyListScope
 */
fun LazyListScope.skeletonItems(
    count: Int,
    content: @Composable () -> Unit
) {
    items(count) {
        content()
        Spacer(modifier = Modifier.height(8.dp))
    }
}

/**
 * Full screen skeleton loading state
 */
@Composable
fun SkeletonLoadingScreen(
    type: SkeletonType,
    itemCount: Int = 5,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        skeletonItems(itemCount) {
            when (type) {
                SkeletonType.EXPENSE -> ExpenseSkeletonItem()
                SkeletonType.FRIEND -> FriendSkeletonItem()
                SkeletonType.GROUP -> GroupSkeletonItem()
                SkeletonType.SETTLEMENT -> SettlementSkeletonItem()
            }
        }
    }
}

enum class SkeletonType {
    EXPENSE,
    FRIEND,
    GROUP,
    SETTLEMENT
} 