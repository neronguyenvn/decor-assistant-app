package com.example.decorassistant.model

import androidx.compose.ui.graphics.vector.ImageVector

data class BottomSheetItem(
    val title: String = "",
    val icon: ImageVector,
    val onClick: () -> Unit
)