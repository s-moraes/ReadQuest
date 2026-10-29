package com.saulo.readquest.ui.model

import androidx.compose.runtime.Immutable

enum class ExtraCategory(val displayName: String) {
    ALL("All"),
    USE_CASE("Use Cases"),
    CURIOSITY("Curiosities"),
    HISTORY("History"),
    EXTERNAL_LINK("Links")
}

@Immutable
data class ExtraItemUiModel(
    val id: String,
    val subjectId: String,
    val title: String,
    val summary: String,
    val category: ExtraCategory,
    val tags: List<String>,
    val isLocked: Boolean,
    val tierBadge: String? = null,
    val externalUrl: String? = null,
    val isCompleted: Boolean = false,
    val unlockRequirementTitle: String? = null
)
