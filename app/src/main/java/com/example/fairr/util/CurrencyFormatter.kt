package com.example.fairr.util

object CurrencyFormatter {
    private val currencySymbols = mapOf(
        "PHP" to "₱",
        "USD" to "$",
        "EUR" to "€",
        "GBP" to "£",
        "JPY" to "¥",
        "AUD" to "A$",
        "CAD" to "C$",
        "SGD" to "S$"
    )

    fun getSymbol(currencyCode: String): String {
        return currencySymbols[currencyCode] ?: currencyCode
    }

    fun format(currencyCode: String, amount: Double, showPositiveSign: Boolean = false): String {
        val symbol = getSymbol(currencyCode)
        val absAmount = kotlin.math.abs(amount)
        val formattedAmount = String.format("%.2f", absAmount)
        
        return when {
            amount > 0 && showPositiveSign -> "+$symbol$formattedAmount"
            amount < 0 -> "-$symbol$formattedAmount"
            else -> "$symbol$formattedAmount"
        }
    }
} 