package com.saulo.readquest.data.repository

import com.saulo.readquest.data.model.GeneralProgressDto
import com.saulo.readquest.data.model.SubjectProgressDto
import kotlinx.coroutines.flow.StateFlow

interface UserProgressRepository {
    val generalProgressFlow: StateFlow<GeneralProgressDto>
    val activeSubjectProgressFlow: StateFlow<SubjectProgressDto>

    suspend fun switchActiveSubject(subjectId: String)
    suspend fun markTopicAccessed(subjectId: String, topicId: String)
    suspend fun markTopicCompleted(subjectId: String, topicId: String, nextTopicId: String? = null)
    suspend fun markExtraCompleted(subjectId: String, extraId: String)
    suspend fun recordQuizAttempt(
        subjectId: String,
        quizId: String,
        scorePercent: Int,
        passThreshold: Int
    )
    suspend fun resetSubjectProgress(subjectId: String)
    fun getSubjectProgress(subjectId: String): SubjectProgressDto
}
