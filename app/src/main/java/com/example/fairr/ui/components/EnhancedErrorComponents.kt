package com.example.fairr.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fairr.ui.theme.*

/**
 * Enhanced error handling with actionable guidance and user-friendly messages
 */

data class ErrorAction(
    val label: String,
    val icon: ImageVector? = null,
    val action: () -> Unit
)

data class ErrorContext(
    val type: ErrorType,
    val message: String,
    val primaryAction: ErrorAction? = null,
    val secondaryAction: ErrorAction? = null,
    val helpLink: String? = null
)

/**
 * Enhanced error messages with context and solutions
 */
object EnhancedErrorMessages {
    fun getNetworkError(retryAction: () -> Unit) = ErrorContext(
        type = ErrorType.NETWORK,
        message = "Unable to connect to the internet. This could be because:\n" +
                "• Your device is in airplane mode\n" +
                "• You're not connected to Wi-Fi or mobile data\n" +
                "• Your connection is weak or unstable",
        primaryAction = ErrorAction("Check Connection") {
            // This would typically open system settings
        },
        secondaryAction = ErrorAction("Try Again") { retryAction() },
        helpLink = "https://support.fairr.app/network-issues"
    )

    fun getAuthenticationError(signInAction: () -> Unit) = ErrorContext(
        type = ErrorType.AUTHENTICATION,
        message = "Your session has expired or you've been signed out. This helps keep your account secure.",
        primaryAction = ErrorAction("Sign In Again") { signInAction() },
        helpLink = "https://support.fairr.app/authentication"
    )

    fun getPermissionError(requestPermission: () -> Unit) = ErrorContext(
        type = ErrorType.PERMISSION,
        message = "You need additional permissions to perform this action. This helps protect sensitive data.",
        primaryAction = ErrorAction("Grant Permission") { requestPermission() },
        helpLink = "https://support.fairr.app/permissions"
    )

    fun getValidationError(
        field: String,
        value: String,
        requirement: String
    ) = ErrorContext(
        type = ErrorType.VALIDATION,
        message = "The $field you entered ($value) doesn't meet the requirements: $requirement",
        primaryAction = ErrorAction("Edit $field") { /* Navigate to edit */ }
    )

    fun getServerError(retryAction: () -> Unit) = ErrorContext(
        type = ErrorType.SERVER,
        message = "We're experiencing technical difficulties. Our team has been notified and is working on it.",
        primaryAction = ErrorAction("Try Again") { retryAction() },
        secondaryAction = ErrorAction("Contact Support") { /* Open support */ },
        helpLink = "https://status.fairr.app"
    )
}

/**
 * Enhanced error state with actionable guidance
 */
@Composable
fun EnhancedErrorState(
    errorContext: ErrorContext,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Error icon with animation
        Icon(
            imageVector = errorContext.type.icon,
            contentDescription = "Error",
            modifier = Modifier.size(80.dp),
            tint = errorContext.type.color
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Error title
        Text(
            text = errorContext.type.title,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Detailed error message
        Text(
            text = errorContext.message,
            fontSize = 16.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )
        
        // Primary action button
        errorContext.primaryAction?.let { action ->
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(
                onClick = action.action,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Primary
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                action.icon?.let { icon ->
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = action.label,
                    color = NeutralWhite,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        
        // Secondary action button
        errorContext.secondaryAction?.let { action ->
            Spacer(modifier = Modifier.height(12.dp))
            
            OutlinedButton(
                onClick = action.action,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                action.icon?.let { icon ->
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = action.label,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        
        // Help link
        errorContext.helpLink?.let { link ->
            Spacer(modifier = Modifier.height(16.dp))
            
            TextButton(
                onClick = { /* Open help link */ }
            ) {
                Icon(
                    imageVector = Icons.Default.Help,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Learn More",
                    fontSize = 14.sp,
                    color = Primary
                )
            }
        }
    }
}

/**
 * Enhanced inline error banner with guidance
 */
@Composable
fun EnhancedErrorBanner(
    errorContext: ErrorContext,
    onDismiss: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = errorContext.type.color.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header row
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = errorContext.type.icon,
                    contentDescription = "Error",
                    tint = errorContext.type.color,
                    modifier = Modifier.size(20.dp)
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Text(
                    text = errorContext.type.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary,
                    modifier = Modifier.weight(1f)
                )
                
                if (onDismiss != null) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Dismiss",
                            tint = errorContext.type.color,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
            
            // Error message
            Text(
                text = errorContext.message,
                fontSize = 14.sp,
                color = TextSecondary,
                lineHeight = 20.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
            
            // Action buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                errorContext.primaryAction?.let { action ->
                    TextButton(
                        onClick = action.action,
                        modifier = Modifier.weight(1f)
                    ) {
                        action.icon?.let { icon ->
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                        }
                        Text(action.label)
                    }
                }
                
                errorContext.secondaryAction?.let { action ->
                    TextButton(
                        onClick = action.action,
                        modifier = Modifier.weight(1f)
                    ) {
                        action.icon?.let { icon ->
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                        }
                        Text(action.label)
                    }
                }
            }
        }
    }
}

/**
 * Enhanced validation error for form fields
 */
@Composable
fun ValidationError(
    message: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Error,
            contentDescription = "Validation Error",
            tint = ErrorRed,
            modifier = Modifier.size(16.dp)
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Text(
            text = message,
            fontSize = 12.sp,
            color = ErrorRed
        )
    }
}

/**
 * Enhanced error utils with more context
 */
object EnhancedErrorUtils {
    fun getValidationMessage(field: String, value: String?, requirements: List<String>): String {
        val failedRequirements = requirements.filter { requirement ->
            when (requirement) {
                "required" -> value.isNullOrBlank()
                "email" -> !value.isNullOrBlank() && !android.util.Patterns.EMAIL_ADDRESS.matcher(value).matches()
                "min6" -> !value.isNullOrBlank() && value.length < 6
                "max50" -> !value.isNullOrBlank() && value.length > 50
                "numeric" -> !value.isNullOrBlank() && !value.all { it.isDigit() }
                else -> false
            }
        }

        return when {
            failedRequirements.contains("required") -> "$field is required"
            failedRequirements.contains("email") -> "Please enter a valid email address"
            failedRequirements.contains("min6") -> "$field must be at least 6 characters"
            failedRequirements.contains("max50") -> "$field must be less than 50 characters"
            failedRequirements.contains("numeric") -> "$field must contain only numbers"
            else -> "Please check $field and try again"
        }
    }

    fun getNetworkErrorContext(error: Throwable?, retryAction: () -> Unit): ErrorContext {
        return when {
            error?.message?.contains("timeout", ignoreCase = true) == true -> ErrorContext(
                type = ErrorType.NETWORK,
                message = "The request is taking longer than expected. This might be due to a slow internet connection.",
                primaryAction = ErrorAction("Try Again") { retryAction() }
            )
            error?.message?.contains("offline", ignoreCase = true) == true -> ErrorContext(
                type = ErrorType.NETWORK,
                message = "You appear to be offline. Please check your internet connection.",
                primaryAction = ErrorAction("Check Connection") { /* Open settings */ }
            )
            else -> EnhancedErrorMessages.getNetworkError(retryAction)
        }
    }

    fun getAuthErrorContext(error: Throwable?, signInAction: () -> Unit): ErrorContext {
        return when {
            error?.message?.contains("expired", ignoreCase = true) == true -> ErrorContext(
                type = ErrorType.AUTHENTICATION,
                message = "Your session has expired for your security. Please sign in again.",
                primaryAction = ErrorAction("Sign In") { signInAction() }
            )
            error?.message?.contains("invalid", ignoreCase = true) == true -> ErrorContext(
                type = ErrorType.AUTHENTICATION,
                message = "Your credentials appear to be invalid. Please check and try again.",
                primaryAction = ErrorAction("Try Again") { signInAction() }
            )
            else -> EnhancedErrorMessages.getAuthenticationError(signInAction)
        }
    }
} 