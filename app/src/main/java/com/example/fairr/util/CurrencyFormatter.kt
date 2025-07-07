package com.example.fairr.util

/**
 * Utility object for formatting currency values and symbols throughout the Fairr app.
 *
 * Provides methods to get currency symbols, format amounts with or without signs,
 * and display currency names for user-friendly UI.
 */
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

    /**
     * Returns the symbol for a given currency code (e.g., "USD" → "$", "PHP" → "₱").
     * Falls back to the code itself if unknown.
     */
    fun getSymbol(currencyCode: String): String {
        return currencySymbols[currencyCode.uppercase()] ?: currencyCode
    }

    /**
     * Formats a currency amount with symbol and optional positive sign.
     *
     * @param currencyCode The ISO code (e.g., "USD", "PHP")
     * @param amount The amount to format
     * @param showPositiveSign If true, adds a "+" for positive values
     * @return Formatted string (e.g., "+$100.00", "-₱50.00", "$0.00")
     */
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

    /**
     * Formats an amount using the default currency (PHP).
     */
    fun format(amount: Double, showPositiveSign: Boolean = false): String {
        return format("PHP", amount, showPositiveSign)
    }

    /**
     * Formats an amount with symbol and a space (e.g., "$ 100.00").
     */
    fun formatWithSpacing(currencyCode: String, amount: Double): String {
        val symbol = getSymbol(currencyCode)
        val absAmount = kotlin.math.abs(amount)
        val formattedAmount = String.format("%.2f", absAmount)
        
        return when {
            amount < 0 -> "-$symbol $formattedAmount"
            else -> "$symbol $formattedAmount"
        }
    }

    /**
     * Returns the display name for a currency code (e.g., "USD" → "US Dollar").
     */
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