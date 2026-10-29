package com.saulo.readquest.ui.model

import androidx.compose.runtime.Immutable

@Immutable
data class QuizTopicUiModel(
    val id: String,
    val subjectId: String,
    val title: String,
    val questionCount: Int,
    val timeLimitMinutes: Int,
    val passPercentage: Int,
    val isPassed: Boolean,
    val bestScorePercent: Int = 0,
    val attempts: Int = 0
)
