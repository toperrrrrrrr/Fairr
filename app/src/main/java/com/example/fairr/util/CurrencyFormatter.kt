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
        return currencySymbols[currencyCode.uppercase()] ?: currencyCode
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

    // Convenience method for formatting with default currency (PHP)
    fun format(amount: Double, showPositiveSign: Boolean = false): String {
        return format("PHP", amount, showPositiveSign)
    }

    // Method to format amount with proper spacing and symbol placement
    fun formatWithSpacing(currencyCode: String, amount: Double): String {
        val symbol = getSymbol(currencyCode)
        val absAmount = kotlin.math.abs(amount)
        val formattedAmount = String.format("%.2f", absAmount)
        
        return when {
            amount < 0 -> "-$symbol $formattedAmount"
            else -> "$symbol $formattedAmount"
        }
    }

    // Method to get currency name for display
    fun getCurrencyName(currencyCode: String): String {
        return when (currencyCode.uppercase()) {
            "PHP" -> "Philippine Peso"
            "USD" -> "US Dollar"
            "EUR" -> "Euro"
            "GBP" -> "British Pound"
            "JPY" -> "Japanese Yen"
            "AUD" -> "Australian Dollar"
            "CAD" -> "Canadian Dollar"
            "SGD" -> "Singapore Dollar"
            else -> currencyCode
        }
    }
} 