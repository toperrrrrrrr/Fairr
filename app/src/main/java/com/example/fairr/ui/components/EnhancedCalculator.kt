package com.example.fairr.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fairr.ui.theme.*
import java.text.DecimalFormat
import kotlin.math.round

/**
 * Enhanced calculator with smart suggestions and improved UX
 */

data class QuickAmount(
    val label: String,
    val amount: Double,
    val icon: ImageVector? = null
)

data class CalculatorState(
    val currentValue: String = "0",
    val previousValue: String = "",
    val operation: String = "",
    val hasDecimal: Boolean = false,
    val isCalculating: Boolean = false,
    val history: List<String> = emptyList()
)

@Composable
fun EnhancedCalculator(
    value: String,
    onValueChange: (String) -> Unit,
    quickAmounts: List<QuickAmount> = emptyList(),
    showSuggestions: Boolean = true,
    modifier: Modifier = Modifier
) {
    var state by remember { mutableStateOf(CalculatorState(currentValue = value)) }
    val haptics = LocalHapticFeedback.current
    val keyboardController = LocalSoftwareKeyboardController.current
    
    val df = remember { DecimalFormat("#,##0.00") }
    
    // Common amounts based on current value
    val suggestedAmounts = remember(state.currentValue) {
        if (state.currentValue.isNotEmpty() && state.currentValue != "0") {
            try {
                val current = state.currentValue.replace(",", "").toDouble()
                listOf(
                    QuickAmount("Half", current / 2, Icons.Default.CallSplit),
                    QuickAmount("Double", current * 2, Icons.Default.Add),
                    QuickAmount("Round Up", ceil5(current), Icons.Default.TrendingUp),
                    QuickAmount("Round Down", floor5(current), Icons.Default.TrendingDown)
                )
            } catch (e: Exception) {
                emptyList()
            }
        } else emptyList()
    }
    
    fun formatNumber(number: String): String {
        return try {
            val numberValue = number.toDouble()
            df.format(numberValue)
        } catch (e: Exception) {
            number
        }
    }
    
    fun calculateResult() {
        if (state.previousValue.isNotEmpty() && state.operation.isNotEmpty() && state.currentValue.isNotEmpty()) {
            val prev = state.previousValue.replace(",", "").toDouble()
            val curr = state.currentValue.replace(",", "").toDouble()
            val result = when (state.operation) {
                "+" -> prev + curr
                "-" -> prev - curr
                "×" -> prev * curr
                "÷" -> if (curr != 0.0) prev / curr else 0.0
                else -> curr
            }
            
            // Update state
            state = state.copy(
                currentValue = formatNumber(result.toString()),
                previousValue = "",
                operation = "",
                isCalculating = false,
                history = state.history + "${formatNumber(prev.toString())} ${state.operation} ${formatNumber(curr.toString())} = ${formatNumber(result.toString())}"
            )
            
            onValueChange(state.currentValue)
            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }
    
    fun handleNumber(num: String) {
        keyboardController?.hide()
        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        
        state = if (!state.isCalculating) {
            if (state.currentValue == "0" || state.currentValue == "0.00") {
                state.copy(currentValue = num)
            } else {
                state.copy(currentValue = state.currentValue + num)
            }
        } else {
            state.copy(
                currentValue = num,
                isCalculating = false
            )
        }
        
        onValueChange(state.currentValue)
    }
    
    fun handleOperation(op: String) {
        keyboardController?.hide()
        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
        
        if (state.currentValue.isNotEmpty()) {
            if (state.previousValue.isNotEmpty()) {
                calculateResult()
            }
            state = state.copy(
                operation = op,
                previousValue = state.currentValue,
                isCalculating = true
            )
        }
    }
    
    fun handleDecimal() {
        keyboardController?.hide()
        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        
        if (!state.hasDecimal) {
            state = state.copy(
                currentValue = if (state.currentValue.isEmpty()) "0." else "${state.currentValue}.",
                hasDecimal = true
            )
            onValueChange(state.currentValue)
        }
    }
    
    fun handleDelete() {
        keyboardController?.hide()
        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
        
        if (state.currentValue.isNotEmpty()) {
            state = state.copy(
                currentValue = state.currentValue.dropLast(1).ifEmpty { "0" },
                hasDecimal = if (state.currentValue.last() == '.') false else state.hasDecimal
            )
            onValueChange(state.currentValue)
        }
    }
    
    fun handleClear() {
        keyboardController?.hide()
        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
        
        state = CalculatorState()
        onValueChange(state.currentValue)
    }
    
    fun handleQuickAmount(amount: Double) {
        keyboardController?.hide()
        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        
        state = state.copy(
            currentValue = formatNumber(amount.toString()),
            hasDecimal = amount % 1 != 0.0
        )
        onValueChange(state.currentValue)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        // Quick amounts
        if (quickAmounts.isNotEmpty()) {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(quickAmounts) { quickAmount ->
                    QuickAmountChip(
                        amount = quickAmount,
                        onClick = { handleQuickAmount(quickAmount.amount) }
                    )
                }
            }
        }
        
        // Display
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 24.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.End
            ) {
                // Operation display
                if (state.previousValue.isNotEmpty() && state.operation.isNotEmpty()) {
                    Text(
                        text = "${formatNumber(state.previousValue)} ${state.operation}",
                        fontSize = 16.sp,
                        color = TextSecondary,
                        textAlign = TextAlign.End
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                
                // Current value
                Text(
                    text = "$ ${formatNumber(state.currentValue.ifEmpty { "0" })}",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    textAlign = TextAlign.End
                )
            }
        }
        
        // Smart suggestions
        if (showSuggestions && suggestedAmounts.isNotEmpty()) {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(suggestedAmounts) { suggestion ->
                    QuickAmountChip(
                        amount = suggestion,
                        onClick = { handleQuickAmount(suggestion.amount) }
                    )
                }
            }
        }

        // Calculator Grid
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // First Row - Operations
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CalcButton(
                    text = "C",
                    isOperation = true,
                    color = ErrorRed,
                    modifier = Modifier.weight(1f)
                ) { handleClear() }
                CalcButton(
                    text = "÷",
                    isOperation = true,
                    modifier = Modifier.weight(1f)
                ) { handleOperation("÷") }
                CalcButton(
                    text = "×",
                    isOperation = true,
                    modifier = Modifier.weight(1f)
                ) { handleOperation("×") }
                CalcButton(
                    icon = Icons.AutoMirrored.Filled.Backspace,
                    isOperation = true,
                    color = DarkBlue,
                    modifier = Modifier.weight(1f)
                ) { handleDelete() }
            }

            // Number Rows
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CalcButton(
                    text = "7",
                    modifier = Modifier.weight(1f)
                ) { handleNumber("7") }
                CalcButton(
                    text = "8",
                    modifier = Modifier.weight(1f)
                ) { handleNumber("8") }
                CalcButton(
                    text = "9",
                    modifier = Modifier.weight(1f)
                ) { handleNumber("9") }
                CalcButton(
                    text = "-",
                    isOperation = true,
                    modifier = Modifier.weight(1f)
                ) { handleOperation("-") }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CalcButton(
                    text = "4",
                    modifier = Modifier.weight(1f)
                ) { handleNumber("4") }
                CalcButton(
                    text = "5",
                    modifier = Modifier.weight(1f)
                ) { handleNumber("5") }
                CalcButton(
                    text = "6",
                    modifier = Modifier.weight(1f)
                ) { handleNumber("6") }
                CalcButton(
                    text = "+",
                    isOperation = true,
                    modifier = Modifier.weight(1f)
                ) { handleOperation("+") }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CalcButton(
                    text = "1",
                    modifier = Modifier.weight(1f)
                ) { handleNumber("1") }
                CalcButton(
                    text = "2",
                    modifier = Modifier.weight(1f)
                ) { handleNumber("2") }
                CalcButton(
                    text = "3",
                    modifier = Modifier.weight(1f)
                ) { handleNumber("3") }
                CalcButton(
                    text = "=",
                    isOperation = true,
                    color = DarkGreen,
                    modifier = Modifier.weight(1f)
                ) { calculateResult() }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CalcButton(
                    text = "0",
                    modifier = Modifier.weight(2f)
                ) { handleNumber("0") }
                CalcButton(
                    text = ".",
                    modifier = Modifier.weight(1f)
                ) { handleDecimal() }
                CalcButton(
                    text = "00",
                    modifier = Modifier.weight(1f)
                ) { handleNumber("00") }
            }
        }
    }
}

@Composable
private fun QuickAmountChip(
    amount: QuickAmount,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        color = Primary.copy(alpha = 0.1f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            amount.icon?.let { icon ->
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Primary,
                    modifier = Modifier.size(16.dp)
                )
            }
            Text(
                text = amount.label,
                color = Primary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun CalcButton(
    modifier: Modifier = Modifier,
    text: String? = null,
    icon: ImageVector? = null,
    isOperation: Boolean = false,
    color: Color = if (isOperation) Primary else TextPrimary,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .aspectRatio(1f)
            .clip(CircleShape)
            .clickable(onClick = onClick),
        color = color.copy(alpha = 0.1f)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (text != null) {
                Text(
                    text = text,
                    color = color,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Medium
                )
            } else if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

private fun ceil5(value: Double): Double {
    return round(value / 5.0) * 5.0
}

private fun floor5(value: Double): Double {
    return (value / 5.0).toInt() * 5.0
} 