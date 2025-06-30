package com.example.fairr.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.fairr.data.model.FriendGroup
import com.example.fairr.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateFriendGroupDialog(
    onConfirm: (String, String, String, String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var groupName by remember { mutableStateOf("") }
    var groupDescription by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf("#4A90E2") }
    var selectedEmoji by remember { mutableStateOf("👥") }
    
    val predefinedColors = listOf(
        "#4A90E2", "#FF6B6B", "#50C878", "#FFB347", 
        "#FF69B4", "#87CEEB", "#DDA0DD", "#98FB98",
        "#F0E68C", "#FF7F50", "#40E0D0", "#9370DB"
    )
    
    val predefinedEmojis = listOf(
        "👥", "👪", "💼", "🎓", "❤️", "🤝", "🎮", "🏃",
        "🎵", "📚", "🍕", "✈️", "🏠", "🎨", "💻", "🌟"
    )

    Dialog(onDismissRequest = onDismiss) {
        ModernCard(
            modifier = modifier.fillMaxWidth(),
            backgroundColor = MaterialTheme.colorScheme.surface,
            shadowElevation = 8,
            cornerRadius = 20
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Create Friend Group",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close"
                        )
                    }
                }
                
                // Preview
                GroupPreview(
                    name = groupName.ifBlank { "Group Name" },
                    color = selectedColor,
                    emoji = selectedEmoji
                )
                
                // Group Name
                OutlinedTextField(
                    value = groupName,
                    onValueChange = { groupName = it },
                    label = { Text("Group Name") },
                    placeholder = { Text("e.g., College Friends") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                // Group Description
                OutlinedTextField(
                    value = groupDescription,
                    onValueChange = { groupDescription = it },
                    label = { Text("Description (optional)") },
                    placeholder = { Text("Describe this group") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
                
                // Color Selection
                Text(
                    text = "Choose Color",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                
                LazyVerticalGrid(
                    columns = GridCells.Fixed(6),
                    modifier = Modifier.height(80.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(predefinedColors) { color ->
                        ColorOption(
                            color = color,
                            isSelected = selectedColor == color,
                            onClick = { selectedColor = color }
                        )
                    }
                }
                
                // Emoji Selection
                Text(
                    text = "Choose Icon",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                
                LazyVerticalGrid(
                    columns = GridCells.Fixed(8),
                    modifier = Modifier.height(80.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(predefinedEmojis) { emoji ->
                        EmojiOption(
                            emoji = emoji,
                            isSelected = selectedEmoji == emoji,
                            onClick = { selectedEmoji = emoji }
                        )
                    }
                }
                
                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }
                    
                    Button(
                        onClick = { 
                            onConfirm(groupName, groupDescription, selectedColor, selectedEmoji)
                        },
                        modifier = Modifier.weight(1f),
                        enabled = groupName.isNotBlank()
                    ) {
                        Text("Create Group")
                    }
                }
            }
        }
    }
}

@Composable
fun EditFriendGroupDialog(
    group: FriendGroup,
    onConfirm: (String, String, String, String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var groupName by remember { mutableStateOf(group.name) }
    var groupDescription by remember { mutableStateOf(group.description) }
    var selectedColor by remember { mutableStateOf(group.color) }
    var selectedEmoji by remember { mutableStateOf(group.emoji) }
    
    val predefinedColors = listOf(
        "#4A90E2", "#FF6B6B", "#50C878", "#FFB347", 
        "#FF69B4", "#87CEEB", "#DDA0DD", "#98FB98",
        "#F0E68C", "#FF7F50", "#40E0D0", "#9370DB"
    )
    
    val predefinedEmojis = listOf(
        "👥", "👪", "💼", "🎓", "❤️", "🤝", "🎮", "🏃",
        "🎵", "📚", "🍕", "✈️", "🏠", "🎨", "💻", "🌟"
    )

    Dialog(onDismissRequest = onDismiss) {
        ModernCard(
            modifier = modifier.fillMaxWidth(),
            backgroundColor = MaterialTheme.colorScheme.surface,
            shadowElevation = 8,
            cornerRadius = 20
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Edit Friend Group",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close"
                        )
                    }
                }
                
                // Preview
                GroupPreview(
                    name = groupName.ifBlank { "Group Name" },
                    color = selectedColor,
                    emoji = selectedEmoji
                )
                
                // Group Name
                OutlinedTextField(
                    value = groupName,
                    onValueChange = { groupName = it },
                    label = { Text("Group Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !group.isDefault // Disable editing for default groups
                )
                
                // Group Description
                OutlinedTextField(
                    value = groupDescription,
                    onValueChange = { groupDescription = it },
                    label = { Text("Description (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
                
                // Color Selection
                Text(
                    text = "Choose Color",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                
                LazyVerticalGrid(
                    columns = GridCells.Fixed(6),
                    modifier = Modifier.height(80.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(predefinedColors) { color ->
                        ColorOption(
                            color = color,
                            isSelected = selectedColor == color,
                            onClick = { selectedColor = color }
                        )
                    }
                }
                
                // Emoji Selection
                Text(
                    text = "Choose Icon",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                
                LazyVerticalGrid(
                    columns = GridCells.Fixed(8),
                    modifier = Modifier.height(80.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(predefinedEmojis) { emoji ->
                        EmojiOption(
                            emoji = emoji,
                            isSelected = selectedEmoji == emoji,
                            onClick = { selectedEmoji = emoji }
                        )
                    }
                }
                
                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }
                    
                    Button(
                        onClick = { 
                            onConfirm(groupName, groupDescription, selectedColor, selectedEmoji)
                        },
                        modifier = Modifier.weight(1f),
                        enabled = groupName.isNotBlank()
                    ) {
                        Text("Save Changes")
                    }
                }
            }
        }
    }
}

@Composable
fun FriendGroupCard(
    group: FriendGroup,
    memberCount: Int,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onManageMembers: () -> Unit,
    modifier: Modifier = Modifier
) {
    ModernCard(
        modifier = modifier.fillMaxWidth(),
        backgroundColor = MaterialTheme.colorScheme.surface,
        shadowElevation = 2,
        cornerRadius = 16
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Group Icon
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                color = Color(android.graphics.Color.parseColor(group.color)),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = group.emoji,
                            fontSize = 20.sp
                        )
                    }
                    
                    Column {
                        Text(
                            text = group.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium
                        )
                        
                        if (group.description.isNotBlank()) {
                            Text(
                                text = group.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        Text(
                            text = "$memberCount members",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                
                // Actions Menu
                var showMenu by remember { mutableStateOf(false) }
                
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More options"
                        )
                    }
                    
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Manage Members") },
                            onClick = {
                                showMenu = false
                                onManageMembers()
                            },
                            leadingIcon = {
                                Icon(Icons.Default.People, contentDescription = null)
                            }
                        )
                        
                        DropdownMenuItem(
                            text = { Text("Edit Group") },
                            onClick = {
                                showMenu = false
                                onEdit()
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Edit, contentDescription = null)
                            }
                        )
                        
                        if (!group.isDefault) {
                            DropdownMenuItem(
                                text = { Text("Delete Group") },
                                onClick = {
                                    showMenu = false
                                    onDelete()
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Delete, 
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GroupPreview(
    name: String,
    color: String,
    emoji: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.surfaceVariant,
                RoundedCornerShape(12.dp)
            )
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(
                    color = Color(android.graphics.Color.parseColor(color)),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = emoji,
                fontSize = 18.sp
            )
        }
        
        Text(
            text = name,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun ColorOption(
    color: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(Color(android.graphics.Color.parseColor(color)))
            .border(
                width = if (isSelected) 3.dp else 0.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                shape = CircleShape
            )
            .clickable { onClick() }
    )
}

@Composable
private fun EmojiOption(
    emoji: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(32.dp)
            .background(
                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                shape = CircleShape
            )
            .border(
                width = if (isSelected) 2.dp else 0.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                shape = CircleShape
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = emoji,
            fontSize = 16.sp
        )
    }
} 