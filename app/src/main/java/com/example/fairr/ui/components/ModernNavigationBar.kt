package com.example.fairr.ui.components

import androidx.compose.animation.core.*
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.fairr.ui.theme.*

@Composable
fun ModernNavigationBar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val navItems = listOf(
        NavigationItem("Home", Icons.Filled.Home, Icons.Outlined.Home),
        NavigationItem("Groups", Icons.Filled.Groups, Icons.Outlined.Groups),
        NavigationItem("Friends", Icons.Filled.People, Icons.Outlined.People),
        NavigationItem("Notifications", Icons.Filled.Notifications, Icons.Outlined.Notifications),
        NavigationItem("Settings", Icons.Filled.Settings, Icons.Outlined.Settings)
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                navItems.forEachIndexed { index, item ->
                    ModernNavigationBarItem(
                        selected = selectedTab == index,
                        onClick = { onTabSelected(index) },
                        item = item,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun ModernNavigationBarItem(
    selected: Boolean,
    onClick: () -> Unit,
    item: NavigationItem,
    modifier: Modifier = Modifier
) {
    val animatedElevation by animateDpAsState(
        targetValue = if (selected) 8.dp else 0.dp,
        animationSpec = tween(durationMillis = 200),
        label = "elevation"
    )

    val animatedScale by animateFloatAsState(
        targetValue = if (selected) 1.1f else 1.0f,
        animationSpec = tween(durationMillis = 200),
        label = "scale"
    )

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(
            visible = selected,
            enter = scaleIn(
                animationSpec = tween(durationMillis = 200)
            ) + fadeIn(
                animationSpec = tween(durationMillis = 200)
            ),
            exit = scaleOut(
                animationSpec = tween(durationMillis = 200)
            ) + fadeOut(
                animationSpec = tween(durationMillis = 200)
            )
        ) {
            Card(
                modifier = Modifier
                    .width(48.dp)
                    .height(40.dp)
                    .shadow(
                        elevation = animatedElevation,
                        shape = RoundedCornerShape(20.dp),
                        spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                    ),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = item.selectedIcon,
                        contentDescription = item.title,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Color.Transparent),
            contentAlignment = Alignment.Center
        ) {
            if (!selected) {
                Icon(
                    imageVector = item.unselectedIcon,
                    contentDescription = item.title,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Surface(
            modifier = Modifier
                .size(48.dp)
                .graphicsLayer {
                    scaleX = animatedScale
                    scaleY = animatedScale
                },
            shape = CircleShape,
            color = Color.Transparent,
            onClick = onClick
        ) {
        }
    }
}

private data class NavigationItem(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) 