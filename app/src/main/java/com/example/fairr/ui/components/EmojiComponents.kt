package com.example.fairr.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Enhanced emoji text component that handles proper rendering
 * across different Android versions and devices
 */
@Composable
fun EmojiText(
    emoji: String,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 16.sp,
    color: Color = Color.Unspecified,
    textAlign: TextAlign? = null,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip
) {
    Text(
        text = emoji,
        modifier = modifier,
        style = TextStyle(
            fontSize = fontSize,
            fontFamily = FontFamily.SansSerif, // Better emoji support than Default
            fontWeight = FontWeight.Normal,
            color = color
        ),
        textAlign = textAlign,
        maxLines = maxLines,
        overflow = overflow
    )
}

/**
 * Emoji avatar component with proper sizing and centering
 */
@Composable
fun EmojiAvatar(
    emoji: String,
    size: Dp = 48.dp,
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.primaryContainer,
    containerColor: Color = Color.Transparent
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(containerColor)
            .then(
                if (backgroundColor != Color.Transparent) {
                    Modifier.background(backgroundColor, CircleShape)
                } else {
                    Modifier
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        EmojiText(
            emoji = emoji,
            fontSize = (size.value * 0.45f).sp, // Optimal ratio for readability
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Enhanced group avatar that handles both emoji and fallback properly
 */
@Composable
fun GroupEmojiAvatar(
    avatar: String,
    groupName: String,
    size: Dp = 48.dp,
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.primaryContainer,
    textColor: Color = MaterialTheme.colorScheme.onPrimaryContainer
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        if (avatar.isNotEmpty() && isValidEmoji(avatar)) {
            EmojiText(
                emoji = avatar,
                fontSize = (size.value * 0.45f).sp,
                textAlign = TextAlign.Center
            )
        } else {
            // Fallback to text initial
            Text(
                text = groupName.firstOrNull()?.uppercase() ?: "G",
                style = TextStyle(
                    fontSize = (size.value * 0.35f).sp,
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    textAlign = TextAlign.Center
                )
            )
        }
    }
}

/**
 * User avatar with initials that handles emoji characters in names
 */
@Composable
fun UserEmojiAvatar(
    name: String,
    size: Dp = 40.dp,
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
    textColor: Color = MaterialTheme.colorScheme.primary
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        val initials = getCleanInitials(name)
        Text(
            text = initials,
            style = TextStyle(
                fontSize = (size.value * 0.35f).sp,
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Bold,
                color = textColor,
                textAlign = TextAlign.Center
            )
        )
    }
}

/**
 * Emoji picker item with proper touch targets and visual feedback
 */
@Composable
fun EmojiPickerItem(
    emoji: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    size: Dp = 40.dp,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(
                color = if (isSelected) 
                    MaterialTheme.colorScheme.primaryContainer 
                else 
                    Color.Transparent
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        EmojiText(
            emoji = emoji,
            fontSize = (size.value * 0.45f).sp,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Utility functions for emoji handling
 */
private fun isValidEmoji(text: String): Boolean {
    if (text.isBlank()) return false
    
    // More permissive emoji validation - check if contains emoji unicode ranges
    return text.any { char ->
        val codePoint = char.code
        // Extended emoji unicode ranges
        codePoint in 0x1F600..0x1F64F || // Emoticons
        codePoint in 0x1F300..0x1F5FF || // Misc Symbols and Pictographs
        codePoint in 0x1F680..0x1F6FF || // Transport and Map
        codePoint in 0x1F1E0..0x1F1FF || // Regional indicators
        codePoint in 0x2600..0x26FF ||   // Misc symbols
        codePoint in 0x2700..0x27BF ||   // Dingbats
        codePoint == 0x200D ||           // Zero width joiner
        codePoint in 0xFE0F..0xFE0F ||   // Variation selector
        codePoint in 0x1F900..0x1F9FF || // Supplemental Symbols and Pictographs
        codePoint in 0x1FA70..0x1FAFF || // Symbols and Pictographs Extended-A
        codePoint in 0x1FAB0..0x1FABF || // Symbols and Pictographs Extended-B
        codePoint in 0x1FAC0..0x1FAFF || // Symbols and Pictographs Extended-C
        codePoint in 0x1FAD0..0x1FAFF    // Symbols and Pictographs Extended-D
    }
}

private fun getCleanInitials(name: String): String {
    // Remove emoji and special characters, then get initials
    val cleanName = name.filter { char ->
        char.isLetter() || char.isWhitespace()
    }.trim()
    
    return if (cleanName.isNotEmpty()) {
        cleanName.split(" ")
            .take(2)
            .joinToString("") { word -> 
                word.firstOrNull()?.uppercase() ?: ""
            }
            .ifEmpty { cleanName.first().uppercase() }
    } else {
        "U" // Default for "User"
    }
}

/**
 * Predefined emoji collections for consistent usage
 */
object EmojiCollections {
    val groupEmojis = listOf(
        "👥", "👪", "💼", "🎓", "❤️", "🤝", "🎮", "🏃",
        "🎵", "📚", "🍕", "✈️", "🏠", "🎨", "💻", "🌟",
        "🎉", "💰", "☕", "🎬", "🏖️", "🎸", "💡", "🎯",
        "🔥", "🌈", "⚽", "🏀", "🎳", "🎪", "🎭", "🏆"
    )
    
    val activityEmojis = listOf(
        "🎯", "🎮", "🎵", "🎨", "📚", "🏃", "🍕", "☕",
        "🎬", "✈️", "🏖️", "💻", "📱", "🚗", "🎪", "🌟"
    )
    
    val categoryEmojis = mapOf(
        "Food & Dining" to "🍕",
        "Transportation" to "🚗",
        "Entertainment" to "🎬",
        "Shopping" to "🛍️",
        "Bills" to "💡",
        "Other" to "📋"
    )
} 