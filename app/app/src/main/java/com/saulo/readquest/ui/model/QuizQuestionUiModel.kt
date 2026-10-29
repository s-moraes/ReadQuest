package com.saulo.readquest.ui.model

import androidx.compose.runtime.Immutable

@Immutable
data class QuizQuestionUiModel(
    val id: String,
    val quizId: String,
    val questionNumber: Int,
    val prompt: String,
    val options: List<String>,
    val correctOptionIndex: Int,
    val explanation: String
)
