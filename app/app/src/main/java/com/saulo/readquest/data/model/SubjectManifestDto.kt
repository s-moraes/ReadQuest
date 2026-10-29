package com.saulo.readquest.data.model

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class SubjectManifestDto(
    val id: String,
    val title: String,
    val version: Int = 1,
    val assets: List<String> = emptyList(),
    @SerialName("path")
    @JsonNames("path", "paths", "learningPaths")
    val paths: List<LearningPathDto> = emptyList(),
    val extras: List<ExtraItemDto> = emptyList(),
    val quizzes: List<QuizIndexDto> = emptyList()
)

@Serializable
data class LearningPathDto(
    val id: String,
    val title: String,
    val description: String,
    val icon: String = "default",
    val topics: List<TopicIndexDto> = emptyList()
)

@Serializable
data class TopicIndexDto(
    val id: String,
    val title: String,
    val file: String,
    val relatedQuizId: String? = null,
    val relatedExtraIds: List<String> = emptyList()
)

@Serializable
data class ExtraItemDto(
    val id: String,
    val title: String,
    val summary: String,
    val type: String,
    val tags: List<String> = emptyList(),
    val tier: String = "FREE",
    val unlockAfterPathId: String? = null,
    val externalUrl: String? = null,
    val file: String? = null
)

@Serializable
data class QuizIndexDto(
    val id: String,
    val title: String,
    val relatedPathId: String? = null,
    val questionCount: Int,
    val timeLimitMinutes: Int,
    val passPercentage: Int,
    val file: String
)
