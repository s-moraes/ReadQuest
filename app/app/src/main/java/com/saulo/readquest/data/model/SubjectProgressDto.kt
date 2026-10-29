package com.saulo.readquest.data.model

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class SubjectProgressDto(
    @SerialName("subjectId")
    @JsonNames("subjectId", "courseId")
    val subjectId: String = "",
    val updatedAt: Long = 0L,
    val resumeTopicId: String? = null,
    val accessedTopicIds: Set<String> = emptySet(),
    val completedTopicIds: Set<String> = emptySet(),
    val completedExtraIds: Set<String> = emptySet(),
    val quizzes: Map<String, QuizProgressDto> = emptyMap()
)

@Serializable
data class QuizProgressDto(
    val bestScorePercent: Int = 0,
    val passed: Boolean = false,
    val attempts: Int = 0,
    val lastAttemptAt: Long = 0L
)
