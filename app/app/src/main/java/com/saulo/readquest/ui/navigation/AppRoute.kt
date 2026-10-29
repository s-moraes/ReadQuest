package com.saulo.readquest.ui.navigation

sealed interface AppRoute {
    data object HomeTabs : AppRoute
    data object Settings : AppRoute
    data class PathTopics(val pathId: String) : AppRoute
    data class ContentReader(val itemId: String, val isExtra: Boolean) : AppRoute
    data class QuizRunner(val quizId: String) : AppRoute
    data class QuizResultReview(val quizId: String, val selectedAnswers: Map<Int, Int>) : AppRoute
}
