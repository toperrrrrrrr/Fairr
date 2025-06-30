package com.example.fairr.data.currency

import javax.inject.Inject
import javax.inject.Singleton

data class Currency(
    val code: String,
    val name: String,
    val symbol: String,
    val flag: String
)

data class CurrencyConversion(
    val fromCurrency: String,
    val toCurrency: String,
    val rate: Double,
    val convertedAmount: Double
)

@Singleton
class CurrencyService @Inject constructor() {
    
    // Static exchange rates (in a real app, these would come from an API)
    private val exchangeRates = mapOf(
        "USD" to 1.0,      // Base currency
        "EUR" to 0.85,
        "GBP" to 0.73,
        "JPY" to 110.0,
        "CAD" to 1.25,
        "AUD" to 1.35,
        "CHF" to 0.92,
        "CNY" to 6.45,
        "SGD" to 1.35,
        "PHP" to 56.0,     // Philippine Peso
        "INR" to 74.0,     // Indian Rupee
        "KRW" to 1180.0,   // Korean Won
        "THB" to 33.0,     // Thai Baht
        "VND" to 23000.0,  // Vietnamese Dong
        "MYR" to 4.15,     // Malaysian Ringgit
        "IDR" to 14300.0   // Indonesian Rupiah
    )
    
    private val supportedCurrencies = listOf(
        Currency("USD", "US Dollar", "$", "🇺🇸"),
        Currency("EUR", "Euro", "€", "🇪🇺"),
        Currency("GBP", "British Pound", "£", "🇬🇧"),
        Currency("JPY", "Japanese Yen", "¥", "🇯🇵"),
        Currency("CAD", "Canadian Dollar", "C$", "🇨🇦"),
        Currency("AUD", "Australian Dollar", "A$", "🇦🇺"),
        Currency("CHF", "Swiss Franc", "CHF", "🇨🇭"),
        Currency("CNY", "Chinese Yuan", "¥", "🇨🇳"),
        Currency("SGD", "Singapore Dollar", "S$", "🇸🇬"),
        Currency("PHP", "Philippine Peso", "₱", "🇵🇭"),
        Currency("INR", "Indian Rupee", "₹", "🇮🇳"),
        Currency("KRW", "Korean Won", "₩", "🇰🇷"),
        Currency("THB", "Thai Baht", "฿", "🇹🇭"),
        Currency("VND", "Vietnamese Dong", "₫", "🇻🇳"),
        Currency("MYR", "Malaysian Ringgit", "RM", "🇲🇾"),
        Currency("IDR", "Indonesian Rupiah", "Rp", "🇮🇩")
    )
    
    fun getSupportedCurrencies(): List<Currency> = supportedCurrencies
    
    fun getCurrency(code: String): Currency? {
        return supportedCurrencies.find { it.code == code }
    }
    
    fun convertAmount(
        amount: Double,
        fromCurrency: String,
        toCurrency: String
    ): CurrencyConversion {
        if (fromCurrency == toCurrency) {
            return CurrencyConversion(fromCurrency, toCurrency, 1.0, amount)
        }
        
        val fromRate = exchangeRates[fromCurrency] ?: 1.0
        val toRate = exchangeRates[toCurrency] ?: 1.0
        
        // Convert to USD first, then to target currency
        val usdAmount = amount / fromRate
        val convertedAmount = usdAmount * toRate
        val rate = toRate / fromRate
        
        return CurrencyConversion(fromCurrency, toCurrency, rate, convertedAmount)
    }
    
    fun formatAmount(amount: Double, currency: String): String {
        val currencyInfo = getCurrency(currency)
        val symbol = currencyInfo?.symbol ?: currency
        
        return when (currency) {
            "JPY", "KRW", "VND", "IDR" -> {
                // Currencies without decimal places
                "${symbol}${String.format("%,.0f", amount)}"
            }
            else -> {
                "${symbol}${String.format("%,.2f", amount)}"
            }
        }
    }
    
    fun getExchangeRate(fromCurrency: String, toCurrency: String): Double {
        if (fromCurrency == toCurrency) return 1.0
        
        val fromRate = exchangeRates[fromCurrency] ?: 1.0
        val toRate = exchangeRates[toCurrency] ?: 1.0
        
        return toRate / fromRate
    }
} 