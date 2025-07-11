package com.example.fairr.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fairr.data.model.ExpenseSplit
import com.example.fairr.ui.theme.*
import com.example.fairr.util.CurrencyFormatter
import kotlin.math.abs

/**
 * Standardized Split Display Components
 * Provides consistent and clear display of expense splits and calculations
 */

/**
 * Enhanced split summary card showing total, split amounts, and validation status
 */
@Composable
fun SplitSummaryCard(
    totalAmount: Double,
    currency: String,
    splits: List<ExpenseSplit>,
    splitType: String,
    modifier: Modifier = Modifier
) {
    val totalSplit = splits.sumOf { it.share }
    val difference = totalAmount - totalSplit
    val isValid = abs(difference) < 0.01
    
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isValid) 
                SuccessGreen.copy(alpha = 0.1f) 
            else 
                ErrorRed.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(16.dp),
        border = if (!isValid) BorderStroke(1.dp, ErrorRed.copy(alpha = 0.3f)) else null
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Split Summary",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                
                Icon(
                    imageVector = if (isValid) Icons.Default.CheckCircle else Icons.Default.Warning,
                    contentDescription = if (isValid) "Valid split" else "Invalid split",
                    tint = if (isValid) SuccessGreen else ErrorRed,
                    modifier = Modifier.size(20.dp)
                )
            }
            
            HorizontalDivider(color = PlaceholderText.copy(alpha = 0.2f))
            
            SplitSummaryRow(
                label = "Total Amount",
                amount = totalAmount,
                currency = currency,
                emphasis = true
            )
            
            SplitSummaryRow(
                label = "Split Total",
                amount = totalSplit,
                currency = currency,
                textColor = if (isValid) TextPrimary else ErrorRed
            )
            
            if (!isValid) {
                SplitSummaryRow(
                    label = if (difference > 0) "Missing Amount" else "Over Amount",
                    amount = abs(difference),
                    currency = currency,
                    textColor = ErrorRed,
                    showIcon = true,
                    icon = Icons.Default.Warning
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Split Method: $splitType",
                    fontSize = 14.sp,
                    color = TextSecondary
                )
                
                Text(
                    text = "${splits.size} participants",
                    fontSize = 14.sp,
                    color = TextSecondary
                )
            }
        }
    }
}

@Composable
private fun SplitSummaryRow(
    label: String,
    amount: Double,
    currency: String,
    textColor: Color = TextPrimary,
    emphasis: Boolean = false,
    showIcon: Boolean = false,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (showIcon && icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = textColor,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            
            Text(
                text = label,
                fontSize = if (emphasis) 16.sp else 14.sp,
                fontWeight = if (emphasis) FontWeight.SemiBold else FontWeight.Medium,
                color = textColor
            )
        }
        
        Text(
            text = CurrencyFormatter.format(currency, amount),
            fontSize = if (emphasis) 16.sp else 14.sp,
            fontWeight = if (emphasis) FontWeight.Bold else FontWeight.Medium,
            color = textColor
        )
    }
}

/**
 * Enhanced participant split card with clear visual indicators
 */
@Composable
fun EnhancedParticipantSplitCard(
    split: ExpenseSplit,
    totalAmount: Double,
    currency: String,
    isCurrentUser: Boolean = false,
    isPayer: Boolean = false,
    splitType: String,
    onEditSplit: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val percentage = if (totalAmount > 0) (split.share / totalAmount) * 100 else 0.0
    
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isPayer -> Primary.copy(alpha = 0.08f)
                isCurrentUser -> DarkBlue.copy(alpha = 0.05f)
                else -> Surface
            }
        ),
        shape = RoundedCornerShape(12.dp),
        border = when {
            isPayer -> BorderStroke(1.dp, Primary.copy(alpha = 0.3f))
            isCurrentUser -> BorderStroke(1.dp, DarkBlue.copy(alpha = 0.2f))
            else -> null
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // User Avatar with status indicator
            Box(
                modifier = Modifier.size(48.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            when {
                                isPayer -> Primary.copy(alpha = 0.2f)
                                isCurrentUser -> DarkBlue.copy(alpha = 0.2f)
                                else -> PlaceholderText.copy(alpha = 0.2f)
                            },
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = split.userName.take(2).uppercase(),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            isPayer -> Primary
                            isCurrentUser -> DarkBlue
                            else -> PlaceholderText
                        }
                    )
                }
                
                // Status indicator (paid/payer badge)
                if (isPayer || split.isPaid) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .background(
                                if (isPayer) Primary else SuccessGreen,
                                CircleShape
                            )
                            .align(Alignment.BottomEnd),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isPayer) Icons.Default.Payment else Icons.Default.Check,
                            contentDescription = if (isPayer) "Payer" else "Paid",
                            tint = NeutralWhite,
                            modifier = Modifier.size(10.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // User details and split info
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isCurrentUser) "${split.userName} (You)" else split.userName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )
                    
                    if (isPayer) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            color = Primary.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "PAYER",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Primary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                
                // Split details based on type
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    when (splitType) {
                        "Percentage" -> {
                            Text(
                                text = "${String.format("%.1f", percentage)}%",
                                fontSize = 12.sp,
                                color = TextSecondary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        "Equal Split" -> {
                            Icon(
                                Icons.Default.Balance,
                                contentDescription = "Equal split",
                                tint = TextSecondary,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = "Equal share",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }
                        "Custom Amount" -> {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = "Custom amount",
                                tint = TextSecondary,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = "Custom amount",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }
            
            // Amount and action
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = CurrencyFormatter.format(currency, split.share),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    textAlign = TextAlign.End
                )
                
                if (splitType == "Percentage" && totalAmount > 0) {
                    Text(
                        text = "${String.format("%.1f", percentage)}%",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        textAlign = TextAlign.End
                    )
                }
            }
            
            if (onEditSplit != null) {
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = onEditSplit,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Edit split",
                        tint = Primary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
