package com.example.fairr.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.fairr.ui.theme.*

/**
 * A reusable image loading component with loading and error states
 */
@Composable
fun FairrImage(
    model: Any?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(8.dp),
    contentScale: ContentScale = ContentScale.Crop,
    placeholder: @Composable () -> Unit = { DefaultImagePlaceholder() },
    error: @Composable () -> Unit = { DefaultImageError() }
) {
    SubcomposeAsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(model)
            .crossfade(true)
            .build(),
        contentDescription = contentDescription,
        modifier = modifier.clip(shape),
        contentScale = contentScale,
        loading = {
            placeholder()
        },
        error = {
            error()
        }
    )
}

/**
 * Enhanced ProfileImage component with advanced features
 * Supports loading states, error handling, custom fallbacks, and optional editing
 */
@Composable
fun ProfileImage(
    photoUrl: String? = null,
    imageUrl: String? = null, // Alternative parameter name for backward compatibility
    displayName: String? = null,
    size: Dp = 40.dp,
    modifier: Modifier = Modifier,
    placeholderText: String? = null,
    showBorder: Boolean = false,
    borderColor: Color = Primary,
    borderWidth: Dp = 2.dp,
    isEditable: Boolean = false,
    onEditClick: () -> Unit = {},
    backgroundColor: Color = Primary.copy(alpha = 0.1f),
    textColor: Color = Primary
) {
    val actualImageUrl = photoUrl ?: imageUrl
    val fallbackText = placeholderText ?: displayName?.let { 
        it.split(" ").take(2).joinToString("") { word -> word.take(1).uppercase() }
    } ?: "?"
    
    Box(modifier = modifier) {
        if (actualImageUrl != null) {
            SubcomposeAsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(actualImageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = "Profile picture",
                modifier = Modifier
                    .size(size)
                    .clip(CircleShape)
                    .let { mod ->
                        if (showBorder) {
                            mod.border(borderWidth, borderColor, CircleShape)
                        } else mod
                    },
                contentScale = ContentScale.Crop,
                loading = {
                    EnhancedProfileImagePlaceholder(
                        text = fallbackText,
                        size = size,
                        backgroundColor = backgroundColor,
                        textColor = textColor
                    )
                },
                error = {
                    EnhancedProfileImagePlaceholder(
                        text = fallbackText,
                        size = size,
                        backgroundColor = backgroundColor,
                        textColor = textColor
                    )
                }
            )
        } else {
            EnhancedProfileImagePlaceholder(
                text = fallbackText,
                size = size,
                backgroundColor = backgroundColor,
                textColor = textColor,
                modifier = Modifier
                    .let { mod ->
                        if (showBorder) {
                            mod.border(borderWidth, borderColor, CircleShape)
                        } else mod
                    }
            )
        }
        
        // Edit button overlay
        if (isEditable) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(size * 0.3f)
                    .clip(CircleShape)
                    .background(Primary)
                    .clickable { onEditClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.CameraAlt,
                    contentDescription = "Edit photo",
                    tint = NeutralWhite,
                    modifier = Modifier.size(size * 0.15f)
                )
            }
        }
    }
}

/**
 * Backward compatibility overload for ProfileImage with old API
 */
@Composable
fun ProfileImage(
    imageUrl: String?,
    modifier: Modifier = Modifier,
    size: Int = 40,
    placeholderText: String? = null
) {
    ProfileImage(
        photoUrl = imageUrl,
        size = size.dp,
        modifier = modifier,
        placeholderText = placeholderText
    )
}

/**
 * A receipt/expense image component with retry functionality
 */
@Composable
fun ReceiptImage(
    imageUrl: String,
    modifier: Modifier = Modifier,
    showLoadingIndicator: Boolean = true,
    onRetryClick: () -> Unit = {}
) {
    var shouldRetry by remember { mutableStateOf(false) }
    
    SubcomposeAsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(imageUrl)
            .crossfade(true)
            .build(),
        contentDescription = "Receipt image",
        modifier = modifier.clip(RoundedCornerShape(8.dp)),
        contentScale = ContentScale.Crop,
        loading = {
            if (showLoadingIndicator) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(LightBackground),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = Primary,
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                }
            } else {
                DefaultImagePlaceholder()
            }
        },
        error = {
            DefaultImageError(
                onRetryClick = {
                    shouldRetry = !shouldRetry
                    onRetryClick()
                }
            )
        }
    )
}

/**
 * Group avatar with emoji or image support
 */
@Composable
fun GroupAvatar(
    avatar: String,
    groupName: String,
    size: Dp = 48.dp,
    modifier: Modifier = Modifier,
    avatarType: String = "EMOJI", // "EMOJI" or "IMAGE"
    backgroundColor: Color = Primary.copy(alpha = 0.1f),
    textColor: Color = Primary
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        when (avatarType) {
            "IMAGE" -> {
                if (avatar.isNotEmpty()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(avatar)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Group Avatar",
                        modifier = Modifier
                            .size(size)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    GroupAvatarFallback(groupName, size, textColor)
                }
            }
            else -> { // EMOJI
                if (avatar.isNotEmpty()) {
                    Text(
                        text = avatar,
                        fontSize = (size.value * 0.4f).sp
                    )
                } else {
                    GroupAvatarFallback(groupName, size, textColor)
                }
            }
        }
    }
}

@Composable
private fun GroupAvatarFallback(
    groupName: String,
    size: Dp,
    textColor: Color
) {
    Text(
        text = groupName.firstOrNull()?.uppercase() ?: "G",
        fontSize = (size.value * 0.3f).sp,
        fontWeight = FontWeight.Bold,
        color = textColor
    )
}

@Composable
private fun DefaultImagePlaceholder() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LightBackground),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            Icons.Default.Image,
            contentDescription = null,
            tint = PlaceholderText,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
private fun DefaultImageError(
    onRetryClick: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LightBackground)
            .clickable { onRetryClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                Icons.Default.BrokenImage,
                contentDescription = null,
                tint = ErrorRed,
                modifier = Modifier.size(24.dp)
            )
            if (onRetryClick != {}) {
                Spacer(modifier = Modifier.height(4.dp))
                Icon(
                    Icons.Default.Refresh,
                    contentDescription = "Retry",
                    tint = TextSecondary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun EnhancedProfileImagePlaceholder(
    text: String,
    size: Dp,
    backgroundColor: Color = Primary.copy(alpha = 0.1f),
    textColor: Color = Primary,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        if (text.isNotBlank() && text != "?") {
            Text(
                text = text,
                color = textColor,
                fontSize = when {
                    size < 30.dp -> 10.sp
                    size < 50.dp -> 14.sp
                    size < 80.dp -> 18.sp
                    else -> 24.sp
                },
                fontWeight = FontWeight.Bold
            )
        } else {
            Icon(
                Icons.Default.Person,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(size * 0.5f)
            )
        }
    }
} 