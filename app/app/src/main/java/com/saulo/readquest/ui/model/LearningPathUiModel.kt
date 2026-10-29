package com.saulo.readquest.ui.model

import androidx.compose.runtime.Immutable

@Immutable
data class LearningPathUiModel(
    val id: String,
    val subjectId: String,
    val title: String,
    val description: String,
    val completedCount: Int,
    val totalCount: Int
)
