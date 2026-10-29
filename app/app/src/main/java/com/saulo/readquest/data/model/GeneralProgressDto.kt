package com.saulo.readquest.data.model

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class GeneralProgressDto(
    val version: Int = 1,
    val updatedAt: Long = 0L,
    @SerialName("activeSubjectId")
    @JsonNames("activeSubjectId", "activeCourseId")
    val activeSubjectId: String = "",
    @SerialName("subjectSyncIndex")
    @JsonNames("subjectSyncIndex", "courseSyncIndex")
    val subjectSyncIndex: Map<String, Long> = emptyMap()
)
