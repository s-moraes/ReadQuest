package com.saulo.readquest.ui.model

import androidx.compose.runtime.Immutable

@Immutable
data class TopicItemUiModel(
    val id: String,
    val pathId: String,
    val topicIndex: Int,
    val title: String,
    val isCompleted: Boolean,
    val isCurrentResume: Boolean,
    val markdownContent: String,
    val relatedQuizId: String? = null,
    val relatedExtraIds: List<String> = emptyList()
)
