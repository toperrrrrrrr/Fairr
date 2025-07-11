package com.example.fairr.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fairr.data.model.Group

// Standard card values for consistency
object FairrCardDefaults {
    val ELEVATION = 1.dp
    val CORNER_RADIUS = 12.dp
    val PADDING = 16.dp
    val SPACING = 12.dp
    val AVATAR_SIZE = 48.dp
    val ICON_SIZE = 20.dp
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StandardCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val cardModifier = modifier.fillMaxWidth()
    
    if (onClick != null) {
        ElevatedCard(
            onClick = onClick,
            modifier = cardModifier,
            shape = RoundedCornerShape(FairrCardDefaults.CORNER_RADIUS),
            colors = androidx.compose.material3.CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
            elevation = androidx.compose.material3.CardDefaults.elevatedCardElevation(
                defaultElevation = FairrCardDefaults.ELEVATION
            )
        ) {
            Box(modifier = Modifier.padding(FairrCardDefaults.PADDING)) {
                content()
            }
        }
    } else {
        ElevatedCard(
            modifier = cardModifier,
            shape = RoundedCornerShape(FairrCardDefaults.CORNER_RADIUS),
            colors = androidx.compose.material3.CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
            elevation = androidx.compose.material3.CardDefaults.elevatedCardElevation(
                defaultElevation = FairrCardDefaults.ELEVATION
            )
        ) {
            Box(modifier = Modifier.padding(FairrCardDefaults.PADDING)) {
                content()
            }
        }
    }
}

@Composable
fun OverviewCard(
    totalBalance: Double,
    currency: String,
    onNavigateToBudgets: () -> Unit,
    modifier: Modifier = Modifier
) {
    StandardCard(modifier = modifier) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Total Balance",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(FairrCardDefaults.SPACING))
            
            Text(
                text = "$currency${String.format("%.2f", totalBalance)}",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(FairrCardDefaults.SPACING))
            
            OutlinedButton(
                onClick = onNavigateToBudgets,
                modifier = Modifier.align(Alignment.End)
            ) {
                Icon(
                    Icons.Default.Analytics,
                    contentDescription = "View Budgets",
                    modifier = Modifier.size(FairrCardDefaults.ICON_SIZE)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("View Budgets")
            }
        }
    }
}

@Composable
fun GroupCard(
    group: Group,
    balance: Double = 0.0,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    StandardCard(
        modifier = modifier,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Group Avatar
                Box(
                    modifier = Modifier
                        .size(FairrCardDefaults.AVATAR_SIZE)
                        .background(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (group.avatar.isNotEmpty()) {
                        Text(
                            text = group.avatar,
                            fontSize = 24.sp
                        )
                    } else {
                        Icon(
                            Icons.Default.Group,
                            contentDescription = "Group Icon",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(FairrCardDefaults.ICON_SIZE)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(FairrCardDefaults.SPACING))
                
                Column {
                    Text(
                        text = group.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Text(
                        text = "${group.members.size} members",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${group.currency}${String.format("%.2f", balance)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (balance >= 0) 
                        MaterialTheme.colorScheme.primary 
                    else 
                        MaterialTheme.colorScheme.error
                )
                
                Text(
                    text = if (balance >= 0) "you'll receive" else "you owe",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun ListItemCard(
    title: String,
    subtitle: String? = null,
    leading: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    StandardCard(
        modifier = modifier,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (leading != null) {
                    Box(modifier = Modifier.size(FairrCardDefaults.AVATAR_SIZE)) {
                        leading()
                    }
                    Spacer(modifier = Modifier.width(FairrCardDefaults.SPACING))
                }
                
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (subtitle != null) {
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            if (trailing != null) {
                Box(modifier = Modifier.padding(start = FairrCardDefaults.SPACING)) {
                    trailing()
                }
            }
        }
    }
} 