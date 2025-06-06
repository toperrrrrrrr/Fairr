package com.example.fairr.ui.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.fairr.ui.model.UnusedPage
import com.example.fairr.ui.model.UnusedPageType
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class UnusedPagesViewModel @Inject constructor() : ViewModel() {
    
    var unusedPages by mutableStateOf<List<UnusedPage>>(emptyList())
        private set

    init {
        // Initialize with data from codebase analysis
        unusedPages = listOf(
            UnusedPage(
                fileName = "ModernHomeScreen.kt",
                filePath = "app/src/main/java/com/example/fairr/ui/screens/ModernHomeScreen.kt",
                description = "Duplicate of HomeScreen.kt, not referenced in navigation",
                type = UnusedPageType.DUPLICATE_SCREEN,
                recommendation = "Remove or merge features into HomeScreen.kt"
            ),
            UnusedPage(
                fileName = "analytics/",
                filePath = "app/src/main/java/com/example/fairr/ui/screens/analytics/",
                description = "Referenced in navigation but not fully implemented",
                type = UnusedPageType.UNIMPLEMENTED_SCREEN,
                recommendation = "Either implement or remove navigation reference"
            ),
            UnusedPage(
                fileName = "camera/",
                filePath = "app/src/main/java/com/example/fairr/ui/screens/camera/",
                description = "Directory exists but not referenced in navigation",
                type = UnusedPageType.UNIMPLEMENTED_SCREEN,
                recommendation = "Remove if not planned for immediate implementation"
            ),
            UnusedPage(
                fileName = "budget/",
                filePath = "app/src/main/java/com/example/fairr/ui/screens/budget/",
                description = "Referenced in navigation but implementation incomplete",
                type = UnusedPageType.UNIMPLEMENTED_SCREEN,
                recommendation = "Complete implementation or remove"
            ),
            UnusedPage(
                fileName = "export/",
                filePath = "app/src/main/java/com/example/fairr/ui/screens/export/",
                description = "Referenced in GroupDetailScreen menu but not implemented",
                type = UnusedPageType.UNIMPLEMENTED_SCREEN,
                recommendation = "Implement or remove menu option"
            ),
            UnusedPage(
                fileName = "FairrDestinations.kt",
                filePath = "app/src/main/java/com/example/fairr/navigation/FairrDestinations.kt",
                description = "Redundant with Screen sealed class in FairrNavGraph.kt",
                type = UnusedPageType.UNUSED_SUPPORT_FILE,
                recommendation = "Remove and consolidate navigation constants"
            )
        )
    }

    fun togglePageRemoval(filePath: String) {
        unusedPages = unusedPages.map { page ->
            if (page.filePath == filePath) {
                page.copy(isMarkedForRemoval = !page.isMarkedForRemoval)
            } else {
                page
            }
        }
    }

    fun getMarkedForRemoval(): List<UnusedPage> {
        return unusedPages.filter { it.isMarkedForRemoval }
    }
} 