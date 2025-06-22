package com.example.fairr.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fairr.ui.theme.*
import java.text.DecimalFormat

@Composable
fun Calculator(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var currentValue by remember { mutableStateOf(value) }
    var hasDecimal by remember { mutableStateOf(false) }
    var isCalculating by remember { mutableStateOf(false) }
    var operation by remember { mutableStateOf("") }
    var previousValue by remember { mutableStateOf("") }
    
    val df = remember { DecimalFormat("#,##0.00") }
    val keyboardController = LocalSoftwareKeyboardController.current
    
    fun hideKeyboard() {
        keyboardController?.hide()
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
        if (previousValue.isNotEmpty() && operation.isNotEmpty() && currentValue.isNotEmpty()) {
            val prev = previousValue.replace(",", "").toDouble()
            val curr = currentValue.replace(",", "").toDouble()
            val result = when (operation) {
                "+" -> prev + curr
                "-" -> prev - curr
                "×" -> prev * curr
                "÷" -> if (curr != 0.0) prev / curr else 0.0
                else -> curr
            }
            currentValue = formatNumber(result.toString())
            previousValue = ""
            operation = ""
            isCalculating = false
            onValueChange(currentValue)
        }
    }
    
    fun handleNumber(num: String) {
        hideKeyboard()
        if (!isCalculating) {
            if (currentValue == "0" || currentValue == "0.00") {
                currentValue = num
            } else {
                currentValue += num
            }
        } else {
            currentValue = num
            isCalculating = false
        }
        onValueChange(currentValue)
    }
    
    fun handleOperation(op: String) {
        hideKeyboard()
        if (currentValue.isNotEmpty()) {
            if (previousValue.isNotEmpty()) {
                calculateResult()
            }
            operation = op
            previousValue = currentValue
            isCalculating = true
        }
    }
    
    fun handleDecimal() {
        hideKeyboard()
        if (!hasDecimal) {
            currentValue = if (currentValue.isEmpty()) "0." else "$currentValue."
            hasDecimal = true
            onValueChange(currentValue)
        }
    }
    
    fun handleDelete() {
        hideKeyboard()
        if (currentValue.isNotEmpty()) {
            if (currentValue.last() == '.') hasDecimal = false
            currentValue = currentValue.dropLast(1)
            if (currentValue.isEmpty()) currentValue = "0"
            onValueChange(currentValue)
        }
    }

    fun handleClear() {
        hideKeyboard()
        currentValue = "0"
        previousValue = ""
        operation = ""
        hasDecimal = false
        isCalculating = false
        onValueChange(currentValue)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        // Display
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 24.dp)
        ) {
            Text(
                text = "$ ${formatNumber(currentValue.ifEmpty { "0" })}",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                textAlign = TextAlign.End,
                modifier = Modifier.fillMaxWidth()
            )
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
private fun CalcButton(
    text: String? = null,
    icon: ImageVector? = null,
    isOperation: Boolean = false,
    color: Color = Color.Unspecified,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(CircleShape)
            .background(
                when {
                    color != Color.Unspecified -> color.copy(alpha = 0.1f)
                    isOperation -> Primary.copy(alpha = 0.1f)
                    else -> PlaceholderText.copy(alpha = 0.1f)
                }
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color.takeIf { it != Color.Unspecified } ?: Primary,
                modifier = Modifier.size(24.dp)
            )
        } else {
            Text(
                text = text ?: "",
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                color = when {
                    color != Color.Unspecified -> color
                    isOperation -> Primary
                    else -> TextPrimary
                }
            )
        }
    }
} 