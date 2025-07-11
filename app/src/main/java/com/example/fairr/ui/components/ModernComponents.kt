package com.example.fairr.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fairr.ui.theme.*
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.graphicsLayer
import com.example.fairr.data.model.Notification
import com.example.fairr.data.model.NotificationType

/**
 * Modern Card Components inspired by clean UI designs
 */
@Composable
fun ModernCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    backgroundColor: Color = CardBackground,
    shadowElevation: Int = 2,
    cornerRadius: Int = 16,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = shadowElevation.dp,
                shape = RoundedCornerShape(cornerRadius.dp),
                ambientColor = Color.Black.copy(alpha = 0.1f),
                spotColor = Color.Black.copy(alpha = 0.1f)
            )
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        shape = RoundedCornerShape(cornerRadius.dp),
        colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = androidx.compose.material3.CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            content = content
        )
    }
}

/**
 * Modern Course/Item Card (inspired by learning app designs)
 */
@Composable
fun ModernItemCard(
    title: String,
    subtitle: String? = null,
    icon: ImageVector,
    iconBackgroundColor: Color = LightGray,
    iconTint: Color = IconTint,
    badge: String? = null,
    progress: Float? = null,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    ModernCard(
        modifier = modifier,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon Container
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = iconBackgroundColor,
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            // Content
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    
                    badge?.let {
                        Text(
                            text = it,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = AccentBlue,
                            modifier = Modifier
                                .background(
                                    color = AccentBlue.copy(alpha = 0.1f),
                                    shape = RoundedCornerShape(6.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
                
                subtitle?.let {
                    Text(
                        text = it,
                        fontSize = 14.sp,
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                progress?.let { progressValue ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .background(
                                color = LightGray,
                                shape = RoundedCornerShape(2.dp)
                            )
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(progressValue)
                                .height(4.dp)
                                .background(
                                    color = AccentGreen,
                                    shape = RoundedCornerShape(2.dp)
                                )
                        )
                    }
                }
            }
        }
    }
}

/**
 * Modern Progress Bar
 */
@Composable
fun ModernProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    backgroundColor: Color = LightGray,
    progressColor: Color = Primary
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(6.dp)
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(3.dp)
            )
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(progress.coerceIn(0f, 1f))
                .background(
                    color = progressColor,
                    shape = RoundedCornerShape(3.dp)
                )
        )
    }
}

/**
 * Modern Badge Component
 */
@Composable
fun ModernBadge(
    text: String,
    backgroundColor: Color = Primary,
    textColor: Color = TextOnDark,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = textColor
        )
    }
}

/**
 * Modern Button Component
 */
@Composable
fun ModernButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = Primary,
            contentColor = TextOnDark,
            disabledContainerColor = Primary.copy(alpha = 0.5f),
            disabledContentColor = TextOnDark.copy(alpha = 0.5f)
        )
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(text)
    }
}

/**
 * Modern Stats Card (inspired by dashboard designs)
 */
@Composable
fun ModernStatsCard(
    title: String,
    value: String,
    changeValue: String? = null,
    changePositive: Boolean = true,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    ModernCard(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    color = TextSecondary,
                    fontWeight = FontWeight.Medium
                )
                
                Text(
                    text = value,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                
                changeValue?.let {
                    Text(
                        text = it,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (changePositive) AccentGreen else AccentRed
                    )
                }
            }
            
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = AccentBlue.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(10.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = AccentBlue,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

/**
 * Modern Input Field
 */
@Composable
fun ModernTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    onTrailingIconClick: (() -> Unit)? = null,
    isPassword: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
    errorMessage: String? = null,
    enabled: Boolean = true
) {
    var passwordVisible by remember { mutableStateOf(false) }
    
    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = leadingIcon?.let { 
                { Icon(imageVector = it, contentDescription = null) } 
            },
            trailingIcon = if (isPassword) {
                {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                            contentDescription = if (passwordVisible) "Hide password" else "Show password"
                        )
                    }
                }
            } else trailingIcon?.let { icon ->
                {
                    IconButton(onClick = { onTrailingIconClick?.invoke() }) {
                        Icon(imageVector = icon, contentDescription = null)
                    }
                }
            },
            visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = InputFocused,
                unfocusedBorderColor = InputBorder,
                focusedContainerColor = InputBackground,
                unfocusedContainerColor = InputBackground,
                errorBorderColor = ErrorRed,
                errorContainerColor = InputBackground
            ),
            enabled = enabled,
            isError = errorMessage != null
        )
        
        errorMessage?.let {
            Text(
                text = it,
                color = ErrorRed,
                fontSize = 12.sp,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }
    }
}

/**
 * Modern Header Component
 */
@Composable
fun ModernHeader(
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
    backgroundColor: Color = BackgroundPrimary
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .padding(20.dp)
    ) {
        Column {
            Text(
                text = title,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            
            subtitle?.let {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = it,
                    fontSize = 16.sp,
                    color = TextSecondary
                )
            }
        }
    }
}

/**
 * Modern List Item
 */
@Composable
fun ModernListItem(
    title: String,
    subtitle: String? = null,
    leadingIcon: ImageVector? = null,
    trailingContent: @Composable (() -> Unit)? = null,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            leadingIcon?.let {
                Icon(
                    imageVector = it,
                    contentDescription = null,
                    tint = IconTint,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
            }
            
            Column(modifier = Modifier.weight(1f)) {
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
                        color = TextSecondary,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
            
            trailingContent?.invoke()
        }
    }
}

/**
 * Modern Section Header
 * Clean typography with consistent spacing
 */
@Composable
fun ModernSectionHeader(
    title: String,
    subtitle: String? = null,
    action: @Composable (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            subtitle?.let {
                Text(
                    text = it,
                    fontSize = 14.sp,
                    color = TextSecondary,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
        
        action?.invoke()
    }
}

/**
 * Modern Divider
 * Subtle dividers with proper spacing
 */
@Composable
fun ModernDivider(
    modifier: Modifier = Modifier,
    thickness: Int = 1,
    color: Color = DividerColor
) {
    HorizontalDivider(
        modifier = modifier,
        thickness = thickness.dp,
        color = color
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernNotificationCard(
    notification: Notification,
    onApprove: ((String) -> Unit)? = null,
    onReject: ((String) -> Unit)? = null,
    onAcceptInvite: ((String) -> Unit)? = null,
    onDeclineInvite: ((String) -> Unit)? = null,
    onMarkAsRead: (() -> Unit)? = null,
    isProcessing: Boolean = false,
    outcome: String? = null,
    onDismiss: (() -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var offsetX by remember { mutableStateOf(0f) }
    val dismissThreshold = 150f
    val isJoinRequest = notification.type == NotificationType.GROUP_JOIN_REQUEST
    val isInvitation = notification.type == NotificationType.GROUP_INVITATION
    val requestId = notification.data["requestId"] as? String
    val inviteId = notification.data["inviteId"] as? String

    Box(
        modifier = modifier
            .pointerInput(onDismiss) {
                if (onDismiss != null) {
                    detectHorizontalDragGestures { change, dragAmount ->
                        offsetX += dragAmount
                        if (offsetX > dismissThreshold || offsetX < -dismissThreshold) {
                            onDismiss()
                            offsetX = 0f
                        }
                    }
                }
            }
    ) {
        ModernCard(
            modifier = Modifier
                .graphicsLayer { translationX = offsetX }
                .fillMaxWidth()
                .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
            backgroundColor = if (notification.isRead) CardBackground.copy(alpha = 0.7f) else CardBackground,
            shadowElevation = if (notification.isRead) 1 else 4,
            cornerRadius = 16
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Icon
                val icon = when (notification.type) {
                    NotificationType.GROUP_JOIN_REQUEST -> Icons.Default.Group
                    NotificationType.GROUP_INVITATION -> Icons.Default.Group
                    NotificationType.EXPENSE_ADDED -> Icons.Default.Receipt
                    NotificationType.SETTLEMENT_REMINDER -> Icons.Default.Payment
                    NotificationType.FRIEND_REQUEST -> Icons.Default.PersonAdd
                    else -> Icons.Default.Notifications
                }
                val iconColor = when (notification.type) {
                    NotificationType.EXPENSE_ADDED -> AccentGreen
                    NotificationType.SETTLEMENT_REMINDER -> AccentBlue
                    NotificationType.FRIEND_REQUEST -> Color(0xFF9C27B0) // Purple
                    else -> Primary
                }
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(iconColor.copy(alpha = 0.12f), shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
                // Content
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = notification.title,
                        fontWeight = if (notification.isRead) FontWeight.Normal else FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = TextPrimary,
                        maxLines = 2
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = notification.message,
                        fontSize = 14.sp,
                        color = TextSecondary,
                        maxLines = 3
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    // Action buttons
                    if (isJoinRequest && requestId != null && (onApprove != null || onReject != null)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { onApprove?.invoke(requestId) },
                                enabled = !isProcessing,
                                colors = ButtonDefaults.buttonColors(containerColor = AccentGreen)
                            ) { Text("Approve") }
                            OutlinedButton(
                                onClick = { onReject?.invoke(requestId) },
                                enabled = !isProcessing,
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentRed)
                            ) { Text("Reject") }
                        }
                    } else if (isInvitation && inviteId != null && (onAcceptInvite != null || onDeclineInvite != null)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { onAcceptInvite?.invoke(inviteId) },
                                enabled = !isProcessing,
                                colors = ButtonDefaults.buttonColors(containerColor = AccentGreen)
                            ) { Text("Accept") }
                            OutlinedButton(
                                onClick = { onDeclineInvite?.invoke(inviteId) },
                                enabled = !isProcessing,
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentRed)
                            ) { Text("Decline") }
                        }
                    } else if (onMarkAsRead != null && !notification.isRead) {
                        TextButton(onClick = onMarkAsRead) {
                            Text("Mark as read")
                        }
                    }
                    // Outcome message
                    outcome?.let {
                        Text(
                            text = it,
                            color = if (it.contains("approved", true) || it.contains("accepted", true)) AccentGreen else AccentRed,
                            fontWeight = FontWeight.Medium,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    // Remove button (visible when notification already processed or read)
                    if ((notification.isRead || outcome != null) && onDismiss != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        TextButton(onClick = onDismiss) {
                            Text("Remove", color = TextSecondary)
                        }
                    }
                }
            }
        }
    }
}

// Preview Components
@Preview(showBackground = true)
@Composable
fun ModernComponentsPreview() {
    FairrTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundSecondary)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ModernSectionHeader(
                title = "Modern Components",
                subtitle = "Preview of design system"
            )
            
            ModernStatsCard(
                title = "Total Expenses",
                value = "$1,234.56",
                changeValue = "+12.3%",
                changePositive = true,
                icon = Icons.AutoMirrored.Filled.TrendingUp
            )
            
            ModernItemCard(
                title = "Coffee with Team",
                subtitle = "Split between 4 people",
                icon = Icons.Default.Coffee,
                badge = "New"
            )
            
            ModernButton(
                text = "Continue",
                onClick = {},
                icon = Icons.AutoMirrored.Filled.ArrowForward
            )
        }
    }
} 

