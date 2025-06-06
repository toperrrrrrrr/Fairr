package com.example.fairr.ui.model

data class UnusedPage(
    val fileName: String,
    val filePath: String,
    val description: String,
    val type: UnusedPageType,
    val recommendation: String,
    var isMarkedForRemoval: Boolean = false
)

enum class UnusedPageType {
    DUPLICATE_SCREEN,
    UNIMPLEMENTED_SCREEN,
    UNUSED_SUPPORT_FILE
} 