package com.example.fairr.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fairr.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmojiPickerDialog(
    onDismiss: () -> Unit,
    onEmojiSelected: (String) -> Unit
) {
    val emojis = listOf(
        "🏠", "🏢", "🏫", "🏪", "🏨", "🏰", "🏯", "🏛️",
        "🎪", "🎭", "🎨", "🎬", "🎤", "🎧", "🎼", "🎹",
        "⚽", "🏀", "🏈", "⚾", "🎾", "🏐", "🏉", "🎱",
        "🚗", "🚕", "🚙", "🚌", "🚎", "🏎️", "🚓", "🚑",
        "🚒", "🚐", "🚚", "🚛", "🚜", "🛴", "🚲", "🛵",
        "🏍️", "🚨", "🚔", "🚍", "🚘", "🚖", "🚡", "🚠",
        "🚟", "🚃", "🚋", "🚞", "🚝", "🚄", "🚅", "🚈",
        "🚂", "🚆", "🚇", "🚊", "🚉", "✈️", "🛫", "🛬",
        "🛩️", "💺", "🛰️", "🚀", "🛸", "🚁", "🛶", "⛵",
        "🚤", "🛥️", "🛳️", "⛴️", "🚢", "⚓", "🚧", "⛽",
        "🚏", "🚦", "🚥", "🗺️", "🗿", "🗽", "🗼", "🏰",
        "🏯", "🏟️", "🎡", "🎢", "🎠", "⛲", "⛱️", "🏖️",
        "🏝️", "🏔️", "🗻", "⛰️", "🌋", "🗾", "🏕️", "⛺",
        "🏞️", "🛣️", "🛤️", "🌅", "🌄", "🌠", "🎇", "🎆",
        "🌇", "🌆", "🏙️", "🌃", "🌌", "🌉", "🌁", "🌂",
        "☂️", "🌂", "☔", "⛱️", "⚡", "❄️", "🔥", "💧",
        "🌊", "💎", "💍", "💎", "💎", "💎", "💎", "💎"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Choose Group Avatar",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
        },
        text = {
            LazyVerticalGrid(
                columns = GridCells.Fixed(8),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.height(300.dp)
            ) {
                items(emojis) { emoji ->
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Primary.copy(alpha = 0.1f))
                            .clickable {
                                onEmojiSelected(emoji)
                                onDismiss()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = emoji,
                            fontSize = 20.sp
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun GroupAvatar(
    avatar: String,
    avatarType: com.example.fairr.data.model.AvatarType,
    groupName: String,
    modifier: Modifier = Modifier,
    size: Int = 48
) {
    Box(
        modifier = modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(Primary.copy(alpha = 0.1f)),
        contentAlignment = Alignment.Center
    ) {
        when (avatarType) {
            com.example.fairr.data.model.AvatarType.EMOJI -> {
                if (avatar.isNotEmpty()) {
                    Text(
                        text = avatar,
                        fontSize = (size * 0.4).sp
                    )
                } else {
                    // Fallback to first letter of group name
                    Text(
                        text = groupName.firstOrNull()?.uppercase() ?: "G",
                        fontSize = (size * 0.3).sp,
                        fontWeight = FontWeight.Bold,
                        color = Primary
                    )
                }
            }
            com.example.fairr.data.model.AvatarType.IMAGE -> {
                // TODO: Implement image loading with Coil or similar
                Text(
                    text = groupName.firstOrNull()?.uppercase() ?: "G",
                    fontSize = (size * 0.3).sp,
                    fontWeight = FontWeight.Bold,
                    color = Primary
                )
            }
        }
    }
}

@Composable
fun AvatarPickerButton(
    currentAvatar: String,
    currentAvatarType: com.example.fairr.data.model.AvatarType,
    groupName: String,
    onAvatarClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.clickable { onAvatarClick() }
    ) {
        GroupAvatar(
            avatar = currentAvatar,
            avatarType = currentAvatarType,
            groupName = groupName,
            size = 80
        )
        
        // Edit indicator
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(24.dp)
                .clip(CircleShape)
                .background(Primary),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "✏️",
                fontSize = 12.sp
            )
        }
    }
} 