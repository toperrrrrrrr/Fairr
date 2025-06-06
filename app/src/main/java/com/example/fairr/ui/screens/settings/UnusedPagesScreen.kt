package com.example.fairr.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.fairr.ui.model.UnusedPage
import com.example.fairr.ui.model.UnusedPageType
import com.example.fairr.ui.theme.*
import com.example.fairr.ui.viewmodels.UnusedPagesViewModel
import androidx.compose.foundation.clickable
import com.example.fairr.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnusedPagesScreen(
    navController: NavController,
    viewModel: UnusedPagesViewModel = hiltViewModel()
) {
    // Effect to automatically mark certain files for removal
    LaunchedEffect(Unit) {
        viewModel.unusedPages.forEach { page ->
            when (page.fileName) {
                "analytics/" -> viewModel.togglePageRemoval(page.filePath)
                "camera/" -> viewModel.togglePageRemoval(page.filePath)
                "budget/" -> viewModel.togglePageRemoval(page.filePath)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Unused Pages") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    val markedCount = viewModel.getMarkedForRemoval().size
                    if (markedCount > 0) {
                        Text(
                            text = "$markedCount selected",
                            modifier = Modifier.padding(end = 16.dp),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            items(viewModel.unusedPages) { page ->
                UnusedPageItem(
                    page = page,
                    onToggleRemoval = { viewModel.togglePageRemoval(page.filePath) },
                    onNavigateToPage = { 
                        when (page.fileName) {
                            "ModernHomeScreen.kt" -> navController.navigate(Screen.Main.createRoute(0)) {
                                popUpTo(Screen.Splash.route)
                                launchSingleTop = true
                            }
                            "export/" -> navController.navigate(Screen.Main.createRoute(3)) {
                                popUpTo(Screen.Splash.route)
                                launchSingleTop = true
                            }
                        }
                    },
                    showPreviewButton = page.fileName in listOf("ModernHomeScreen.kt", "export/")
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UnusedPageItem(
    page: UnusedPage,
    onToggleRemoval: () -> Unit,
    onNavigateToPage: () -> Unit,
    showPreviewButton: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable(enabled = showPreviewButton) { onNavigateToPage() },
        colors = CardDefaults.cardColors(
            containerColor = if (page.isMarkedForRemoval) {
                MaterialTheme.colorScheme.errorContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = page.fileName,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = page.filePath,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Switch(
                    checked = page.isMarkedForRemoval,
                    onCheckedChange = { onToggleRemoval() },
                    thumbContent = if (page.isMarkedForRemoval) {
                        {
                            Icon(
                                imageVector = Icons.Filled.Delete,
                                contentDescription = null,
                                modifier = Modifier.size(SwitchDefaults.IconSize)
                            )
                        }
                    } else null
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = page.description,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = when (page.type) {
                        UnusedPageType.DUPLICATE_SCREEN -> Icons.Default.FileCopy
                        UnusedPageType.UNIMPLEMENTED_SCREEN -> Icons.Default.Construction
                        UnusedPageType.UNUSED_SUPPORT_FILE -> Icons.Default.InsertDriveFile
                    },
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = page.type.name.replace("_", " "),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Recommendation: ${page.recommendation}",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Preview button - only show for ModernHomeScreen and export
            if (showPreviewButton) {
                Button(
                    onClick = onNavigateToPage,
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(top = 8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Preview,
                        contentDescription = "Preview",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Preview")
                }
            }
        }
    }
} 