package com.example.fairr.util

import android.util.Patterns
import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Comprehensive input validation utility for the Fairr app
 * Provides consistent validation across all screens and components
 */
object ValidationUtils {
    
    // Constants for validation limits
    private const val MAX_EMAIL_LENGTH = 254
    private const val MIN_PASSWORD_LENGTH = 6
    private const val MAX_PASSWORD_LENGTH = 128
    private const val MAX_NAME_LENGTH = 50
    private const val MIN_NAME_LENGTH = 1
    private const val MAX_DESCRIPTION_LENGTH = 1000
    private const val MAX_GROUP_NAME_LENGTH = 50
    private const val MIN_GROUP_NAME_LENGTH = 1
    private const val MAX_EXPENSE_AMOUNT = 999999.99
    private const val MIN_EXPENSE_AMOUNT = 0.01
    private const val MAX_MESSAGE_LENGTH = 500
    
    /**
     * Validate email address format and length
     */
    fun validateEmail(email: String): ValidationResult {
        val trimmedEmail = email.trim()
        
        return when {
            trimmedEmail.isBlank() -> ValidationResult.Error("Email address cannot be empty")
            trimmedEmail.length > MAX_EMAIL_LENGTH -> ValidationResult.Error("Email address is too long (max $MAX_EMAIL_LENGTH characters)")
            !Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches() -> ValidationResult.Error("Please enter a valid email address")
            trimmedEmail.contains("..") -> ValidationResult.Error("Email address contains invalid consecutive dots")
            trimmedEmail.startsWith(".") || trimmedEmail.endsWith(".") -> ValidationResult.Error("Email address cannot start or end with a dot")
            trimmedEmail.count { it == '@' } != 1 -> ValidationResult.Error("Email address must contain exactly one @ symbol")
            else -> ValidationResult.Success
        }
    }
    
    /**
     * Validate password strength and format
     */
    fun validatePassword(password: String): ValidationResult {
        return when {
            password.isBlank() -> ValidationResult.Error("Password cannot be empty")
            password.length < MIN_PASSWORD_LENGTH -> ValidationResult.Error("Password must be at least $MIN_PASSWORD_LENGTH characters long")
            password.length > MAX_PASSWORD_LENGTH -> ValidationResult.Error("Password is too long (max $MAX_PASSWORD_LENGTH characters)")
            !password.any { it.isLetter() } -> ValidationResult.Error("Password must contain at least one letter")
            !password.any { it.isDigit() } -> ValidationResult.Error("Password must contain at least one number")
            password.contains(" ") -> ValidationResult.Error("Password cannot contain spaces")
            else -> ValidationResult.Success
        }
    }
    
    /**
     * Validate expense amount for financial accuracy
     */
    fun validateAmount(amount: String): ValidationResult {
        val trimmedAmount = amount.trim()
        
        return when {
            trimmedAmount.isBlank() -> ValidationResult.Error("Amount cannot be empty")
            else -> {
                val numericAmount = trimmedAmount.toDoubleOrNull()
                when {
                    numericAmount == null -> ValidationResult.Error("Please enter a valid number")
                    numericAmount <= 0 -> ValidationResult.Error("Amount must be greater than 0")
                    numericAmount < MIN_EXPENSE_AMOUNT -> ValidationResult.Error("Amount must be at least $MIN_EXPENSE_AMOUNT")
                    numericAmount > MAX_EXPENSE_AMOUNT -> ValidationResult.Error("Amount is too large (max $MAX_EXPENSE_AMOUNT)")
                    hasMoreThanTwoDecimalPlaces(numericAmount) -> ValidationResult.Error("Amount can have at most 2 decimal places")
                    else -> ValidationResult.Success
                }
            }
        }
    }
    
    /**
     * Validate user display name
     */
    fun validateDisplayName(name: String): ValidationResult {
        val trimmedName = name.trim()
        
        return when {
            trimmedName.isBlank() -> ValidationResult.Error("Name cannot be empty")
            trimmedName.length < MIN_NAME_LENGTH -> ValidationResult.Error("Name is too short")
            trimmedName.length > MAX_NAME_LENGTH -> ValidationResult.Error("Name is too long (max $MAX_NAME_LENGTH characters)")
            !trimmedName.all { it.isLetter() || it.isWhitespace() || it == '\'' || it == '-' } -> 
                ValidationResult.Error("Name can only contain letters, spaces, apostrophes, and hyphens")
            trimmedName.startsWith(" ") || trimmedName.endsWith(" ") -> ValidationResult.Error("Name cannot start or end with spaces")
            else -> ValidationResult.Success
        }
    }
    
    /**
     * Validate group name
     */
    fun validateGroupName(name: String): ValidationResult {
        val trimmedName = name.trim()
        
        return when {
            trimmedName.isBlank() -> ValidationResult.Error("Group name cannot be empty")
            trimmedName.length < MIN_GROUP_NAME_LENGTH -> ValidationResult.Error("Group name is too short")
            trimmedName.length > MAX_GROUP_NAME_LENGTH -> ValidationResult.Error("Group name is too long (max $MAX_GROUP_NAME_LENGTH characters)")
            containsInvalidCharacters(trimmedName) -> ValidationResult.Error("Group name contains invalid characters")
            else -> ValidationResult.Success
        }
    }
    
    /**
     * Validate expense description
     */
    fun validateExpenseDescription(description: String): ValidationResult {
        val trimmedDescription = description.trim()
        
        return when {
            trimmedDescription.isBlank() -> ValidationResult.Error("Description cannot be empty")
            trimmedDescription.length > MAX_DESCRIPTION_LENGTH -> ValidationResult.Error("Description is too long (max $MAX_DESCRIPTION_LENGTH characters)")
            containsInvalidCharacters(trimmedDescription) -> ValidationResult.Error("Description contains invalid characters")
            else -> ValidationResult.Success
        }
    }
    
    /**
     * Validate optional message text
     */
    fun validateMessage(message: String): ValidationResult {
        val trimmedMessage = message.trim()
        
        return when {
            trimmedMessage.length > MAX_MESSAGE_LENGTH -> ValidationResult.Error("Message is too long (max $MAX_MESSAGE_LENGTH characters)")
            containsInvalidCharacters(trimmedMessage) -> ValidationResult.Error("Message contains invalid characters")
            else -> ValidationResult.Success
        }
    }
    
    /**
     * Validate verification code format
     */
    fun validateVerificationCode(code: String): ValidationResult {
        val trimmedCode = code.trim()
        
        return when {
            trimmedCode.isBlank() -> ValidationResult.Error("Verification code cannot be empty")
            trimmedCode.length != 6 -> ValidationResult.Error("Verification code must be 6 digits")
            !trimmedCode.all { it.isDigit() } -> ValidationResult.Error("Verification code can only contain numbers")
            else -> ValidationResult.Success
        }
    }
    
    /**
     * Validate phone number format (optional for future use)
     */
    fun validatePhoneNumber(phone: String): ValidationResult {
        val trimmedPhone = phone.trim()
        
        return when {
            trimmedPhone.isBlank() -> ValidationResult.Error("Phone number cannot be empty")
            trimmedPhone.length < 10 -> ValidationResult.Error("Phone number is too short")
            trimmedPhone.length > 15 -> ValidationResult.Error("Phone number is too long")
            !trimmedPhone.all { it.isDigit() || it == '+' || it == '-' || it == '(' || it == ')' || it == ' ' } -> 
                ValidationResult.Error("Phone number contains invalid characters")
            else -> ValidationResult.Success
        }
    }
    
    /**
     * Sanitize text input to prevent injection attacks
     */
    fun sanitizeText(input: String): String {
        return input.trim()
            .replace(Regex("[<>\"'&]"), "") // Remove potentially dangerous characters
            .replace(Regex("\\s+"), " ") // Replace multiple spaces with single space
            .take(MAX_DESCRIPTION_LENGTH) // Limit length
    }
    
    /**
     * Sanitize and validate currency amount
     */
    fun sanitizeAndValidateAmount(amount: String): Pair<ValidationResult, Double?> {
        val sanitized = amount.trim().replace(Regex("[^0-9.]"), "")
        val validation = validateAmount(sanitized)
        val numericValue = if (validation is ValidationResult.Success) sanitized.toDoubleOrNull() else null
        return Pair(validation, numericValue)
    }
    
    // Helper methods
    
    private fun hasMoreThanTwoDecimalPlaces(amount: Double): Boolean {
        val bd = BigDecimal.valueOf(amount)
        val scale = bd.setScale(2, RoundingMode.HALF_UP).scale()
        return bd.scale() > 2 && bd.compareTo(bd.setScale(2, RoundingMode.HALF_UP)) != 0
    }
    
    private fun containsInvalidCharacters(text: String): Boolean {
        // Check for potential script injection or other malicious content
        val dangerousPatterns = listOf(
            "<script", "</script>", "javascript:", "data:", "vbscript:",
            "onload=", "onerror=", "onclick=", "onmouseover="
        )
        
        val lowerText = text.lowercase()
        return dangerousPatterns.any { pattern -> lowerText.contains(pattern) }
    }
}

/**
 * Sealed class representing validation results
 */
sealed class ValidationResult {
    object Success : ValidationResult()
    data class Error(val message: String) : ValidationResult()
    
    fun isSuccess(): Boolean = this is Success
    fun isError(): Boolean = this is Error
    
    fun getErrorMessage(): String? = when (this) {
        is Error -> message
        is Success -> null
    }
} 