package com.saulo.readquest.ui.model

import androidx.compose.runtime.Immutable

@Immutable
data class SubjectUiModel(
    val id: String,
    val title: String,
    val resumeTopicId: String,
    val resumeTopicTitle: String,
    val resumePathTitle: String,
    val resumeTopicIndex: Int,
    val resumeTotalTopics: Int,
    val isResumeTopicAccessed: Boolean = false
)
