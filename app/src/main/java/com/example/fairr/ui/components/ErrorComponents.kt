package com.example.fairr.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fairr.ui.theme.*

/**
 * Standardized Error Handling Components
 * Provides consistent error messaging across the entire app
 */

/**
 * Error Types with predefined messages and styling
 */
enum class ErrorType(
    val icon: ImageVector,
    val title: String,
    val defaultMessage: String,
    val color: Color
) {
    NETWORK(
        icon = Icons.Default.WifiOff,
        title = "Connection Error",
        defaultMessage = "Please check your internet connection and try again",
        color = ErrorRed
    ),
    AUTHENTICATION(
        icon = Icons.Default.Lock,
        title = "Authentication Error",
        defaultMessage = "Please sign in again to continue",
        color = ErrorRed
    ),
    PERMISSION(
        icon = Icons.Default.Security,
        title = "Access Denied",
        defaultMessage = "You don't have permission to perform this action",
        color = ErrorRed
    ),
    NOT_FOUND(
        icon = Icons.Default.SearchOff,
        title = "Not Found",
        defaultMessage = "The requested item could not be found",
        color = ErrorRed
    ),
    VALIDATION(
        icon = Icons.Default.Warning,
        title = "Invalid Input",
        defaultMessage = "Please check your input and try again",
        color = WarningOrange
    ),
    SERVER(
        icon = Icons.Default.CloudOff,
        title = "Server Error",
        defaultMessage = "Our servers are experiencing issues. Please try again later",
        color = ErrorRed
    ),
    GENERIC(
        icon = Icons.Default.ErrorOutline,
        title = "Something Went Wrong",
        defaultMessage = "An unexpected error occurred. Please try again",
        color = ErrorRed
    )
}

/**
 * Standard full-screen error state
 */
@Composable
fun StandardErrorState(
    errorType: ErrorType = ErrorType.GENERIC,
    customMessage: String? = null,
    onRetry: (() -> Unit)? = null,
    retryText: String = "Try Again",
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = errorType.icon,
            contentDescription = "Error",
            modifier = Modifier.size(80.dp),
            tint = errorType.color
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = errorType.title,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Text(
            text = customMessage ?: errorType.defaultMessage,
            fontSize = 16.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )
        
        if (onRetry != null) {
            Spacer(modifier = Modifier.height(32.dp))
            
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Primary
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text(
                    text = retryText,
                    color = NeutralWhite,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

/**
 * Inline error banner for forms and smaller components
 */
@Composable
fun ErrorBanner(
    errorType: ErrorType = ErrorType.VALIDATION,
    message: String,
    onDismiss: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = errorType.color.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = errorType.icon,
                contentDescription = "Error",
                tint = errorType.color,
                modifier = Modifier.size(20.dp)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Text(
                text = message,
                fontSize = 14.sp,
                color = TextPrimary,
                modifier = Modifier.weight(1f),
                lineHeight = 20.sp
            )
            
            if (onDismiss != null) {
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Dismiss",
                        tint = errorType.color,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

/**
 * Success message component for consistency
 */
@Composable
fun SuccessBanner(
    message: String,
    onDismiss: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = SuccessGreen.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Success",
                tint = SuccessGreen,
                modifier = Modifier.size(20.dp)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Text(
                text = message,
                fontSize = 14.sp,
                color = TextPrimary,
                modifier = Modifier.weight(1f),
                lineHeight = 20.sp
            )
            
            if (onDismiss != null) {
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Dismiss",
                        tint = SuccessGreen,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

/**
 * Error utility functions for mapping common error types
 */
object ErrorUtils {
    
    fun getErrorType(throwable: Throwable?): ErrorType {
        return when {
            throwable?.message?.contains("PERMISSION_DENIED") == true -> ErrorType.PERMISSION
            throwable?.message?.contains("UNAUTHENTICATED") == true -> ErrorType.AUTHENTICATION
            throwable?.message?.contains("NOT_FOUND") == true -> ErrorType.NOT_FOUND
            throwable?.message?.contains("NETWORK") == true -> ErrorType.NETWORK
            throwable?.message?.contains("UNAVAILABLE") == true -> ErrorType.SERVER
            else -> ErrorType.GENERIC
        }
    }
    
    fun getErrorType(errorMessage: String): ErrorType {
        return when {
            errorMessage.contains("PERMISSION_DENIED", ignoreCase = true) -> ErrorType.PERMISSION
            errorMessage.contains("UNAUTHENTICATED", ignoreCase = true) -> ErrorType.AUTHENTICATION
            errorMessage.contains("NOT_FOUND", ignoreCase = true) -> ErrorType.NOT_FOUND
            errorMessage.contains("NETWORK", ignoreCase = true) -> ErrorType.NETWORK
            errorMessage.contains("UNAVAILABLE", ignoreCase = true) -> ErrorType.SERVER
            errorMessage.contains("connection", ignoreCase = true) -> ErrorType.NETWORK
            errorMessage.contains("permission", ignoreCase = true) -> ErrorType.PERMISSION
            errorMessage.contains("authentication", ignoreCase = true) -> ErrorType.AUTHENTICATION
            errorMessage.contains("server", ignoreCase = true) -> ErrorType.SERVER
            errorMessage.contains("validation", ignoreCase = true) -> ErrorType.VALIDATION
            else -> ErrorType.GENERIC
        }
    }
    
    fun getUserFriendlyMessage(errorMessage: String): String {
        return when {
            errorMessage.contains("PERMISSION_DENIED") -> "You don't have permission to perform this action"
            errorMessage.contains("UNAUTHENTICATED") -> "Please sign in to continue"
            errorMessage.contains("NETWORK_ERROR") -> "Please check your internet connection"
            errorMessage.contains("TIMEOUT") -> "The request timed out. Please try again"
            errorMessage.contains("INVALID_") -> "Please check your input and try again"
            else -> errorMessage.take(100) + if (errorMessage.length > 100) "..." else ""
        }
    }
}
