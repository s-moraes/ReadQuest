package com.saulo.readquest.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.Quiz
import androidx.compose.ui.graphics.vector.ImageVector

enum class NavigationTab(
    val label: String,
    val unselectedIcon: ImageVector,
    val selectedIcon: ImageVector
) {
    LEARN("Read", Icons.Outlined.AutoStories, Icons.Filled.AutoStories),
    EXTRA("Extra", Icons.Outlined.Lightbulb, Icons.Filled.Lightbulb),
    QUIZ("Quiz", Icons.Outlined.Quiz, Icons.Filled.Quiz);

    val icon: ImageVector get() = unselectedIcon
}
