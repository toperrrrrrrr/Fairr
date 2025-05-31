package com.example.fairr.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fairr.ui.theme.*

/**
 * Modern Card Components inspired by clean UI designs
 */
@Composable
fun ModernCard(
    modifier: Modifier = Modifier,
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
            ),
        shape = RoundedCornerShape(cornerRadius.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
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
    progress: Float? = null,
    badge: String? = null,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    ModernCard(
        modifier = modifier.clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon Container
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = LightGray,
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = IconTint,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Content
            Column(modifier = Modifier.weight(1f)) {
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
                        ModernBadge(text = it)
                    }
                }
                
                subtitle?.let {
                    Text(
                        text = it,
                        fontSize = 14.sp,
                        color = TextSecondary,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                
                progress?.let {
                    Spacer(modifier = Modifier.height(8.dp))
                    ModernProgressBar(progress = it)
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
    enabled: Boolean = true,
    backgroundColor: Color = ButtonBackground,
    textColor: Color = ButtonText,
    icon: ImageVector? = null
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(52.dp),
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor,
            contentColor = textColor,
            disabledContainerColor = MediumGray,
            disabledContentColor = TextSecondary
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            icon?.let {
                Icon(
                    imageVector = it,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

/**
 * Modern Stats Card (inspired by dashboard designs)
 */
@Composable
fun ModernStatsCard(
    title: String,
    value: String,
    icon: ImageVector,
    change: String? = null,
    isPositive: Boolean = true,
    modifier: Modifier = Modifier
) {
    ModernCard(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    color = TextSecondary,
                    fontWeight = FontWeight.Medium
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = value,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                
                change?.let {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = it,
                        fontSize = 12.sp,
                        color = if (isPositive) SuccessGreen else ErrorRed,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = Primary.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(10.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = Primary,
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
    onTrailingIconClick: () -> Unit = {},
    isError: Boolean = false,
    errorMessage: String? = null
) {
    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = leadingIcon?.let {
                {
                    Icon(
                        imageVector = it,
                        contentDescription = null,
                        tint = IconTint
                    )
                }
            },
            trailingIcon = trailingIcon?.let {
                {
                    IconButton(onClick = onTrailingIconClick) {
                        Icon(
                            imageVector = it,
                            contentDescription = null,
                            tint = IconTint
                        )
                    }
                }
            },
            isError = isError,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Primary,
                focusedLabelColor = Primary,
                cursorColor = Primary,
                errorBorderColor = ErrorRed,
                errorLabelColor = ErrorRed
            ),
            shape = RoundedCornerShape(12.dp)
        )
        
        if (isError && errorMessage != null) {
            Text(
                text = errorMessage,
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
            ModernHeader(
                title = "Modern Design",
                subtitle = "Clean and minimal interface"
            )
            
            ModernItemCard(
                title = "UI/UX Design Course",
                subtitle = "12 lessons • 4.5 ⭐",
                icon = Icons.Default.School,
                progress = 0.65f,
                badge = "$70"
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ModernStatsCard(
                    title = "Total Expenses",
                    value = "$1,234",
                    icon = Icons.Default.Receipt,
                    change = "+12%",
                    modifier = Modifier.weight(1f)
                )
                
                ModernStatsCard(
                    title = "Groups",
                    value = "8",
                    icon = Icons.Default.Group,
                    change = "+2",
                    modifier = Modifier.weight(1f)
                )
            }
            
            ModernButton(
                text = "Get Started",
                onClick = {},
                icon = Icons.AutoMirrored.Filled.ArrowForward
            )
        }
    }
} 