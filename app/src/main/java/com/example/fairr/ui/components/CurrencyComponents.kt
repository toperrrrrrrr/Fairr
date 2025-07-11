package com.example.fairr.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fairr.data.currency.CurrencyService
import com.example.fairr.ui.theme.*

@Composable
fun CurrencyConversionCard(
    amount: Double,
    fromCurrency: String,
    toCurrency: String,
    currencyService: CurrencyService,
    modifier: Modifier = Modifier
) {
    if (fromCurrency == toCurrency) return

    val conversion = currencyService.convertAmount(amount, fromCurrency, toCurrency)
    val fromCurrencyInfo = currencyService.getCurrency(fromCurrency)
    val toCurrencyInfo = currencyService.getCurrency(toCurrency)

    Card(
        modifier = modifier,
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = InfoBlue.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = fromCurrencyInfo?.flag ?: "",
                        fontSize = 16.sp
                    )
                    Text(
                        text = currencyService.formatAmount(amount, fromCurrency),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                Text(
                    text = "1 \$fromCurrency = \${String.format(\"%.4f\", conversion.rate)} \$toCurrency",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
            
            Icon(
                Icons.Default.SwapHoriz,
                contentDescription = "Convert",
                tint = InfoBlue,
                modifier = Modifier.size(20.dp)
            )
            
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = currencyService.formatAmount(conversion.convertedAmount, toCurrency),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = InfoBlue
                    )
                    Text(
                        text = toCurrencyInfo?.flag ?: "",
                        fontSize = 16.sp
                    )
                }
                
                Text(
                    text = toCurrency,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }
    }
}

@Composable
fun MultiCurrencyAmountDisplay(
    amount: Double,
    currency: String,
    userCurrency: String,
    currencyService: CurrencyService,
    modifier: Modifier = Modifier,
    showConversion: Boolean = true
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Primary amount
        Text(
            text = currencyService.formatAmount(amount, currency),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        
        // Conversion if different currencies
        if (showConversion && currency != userCurrency) {
            val conversion = currencyService.convertAmount(amount, currency, userCurrency)
            Text(
                text = " \${currencyService.formatAmount(conversion.convertedAmount, userCurrency)}",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        }
    }
}
