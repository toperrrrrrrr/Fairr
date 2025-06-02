package com.example.fairr.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
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
 * A circular profile image component
 */
@Composable
fun ProfileImage(
    imageUrl: String?,
    modifier: Modifier = Modifier,
    size: Int = 40,
    placeholderText: String? = null
) {
    if (imageUrl != null) {
        FairrImage(
            model = imageUrl,
            contentDescription = "Profile picture",
            modifier = modifier.size(size.dp),
            shape = CircleShape,
            placeholder = {
                ProfileImagePlaceholder(
                    text = placeholderText,
                    modifier = Modifier.size(size.dp)
                )
            }
        )
    } else {
        ProfileImagePlaceholder(
            text = placeholderText,
            modifier = modifier.size(size.dp)
        )
    }
}

/**
 * A receipt/expense image component
 */
@Composable
fun ReceiptImage(
    imageUrl: String,
    modifier: Modifier = Modifier,
    showLoadingIndicator: Boolean = true
) {
    FairrImage(
        model = imageUrl,
        contentDescription = "Receipt image",
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        placeholder = {
            if (showLoadingIndicator) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(LightBackground),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = DarkGreen,
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                }
            } else {
                DefaultImagePlaceholder()
            }
        }
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
private fun DefaultImageError() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LightBackground),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            Icons.Default.BrokenImage,
            contentDescription = null,
            tint = ErrorRed,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
private fun ProfileImagePlaceholder(
    text: String?,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(DarkGreen.copy(alpha = 0.1f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        if (!text.isNullOrBlank()) {
            Text(
                text = text.take(2).uppercase(),
                color = DarkGreen,
                style = MaterialTheme.typography.titleMedium
            )
        } else {
            Icon(
                Icons.Default.Image,
                contentDescription = null,
                tint = DarkGreen,
                modifier = Modifier.size(24.dp)
            )
        }
    }
} 