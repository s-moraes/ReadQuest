package com.saulo.readquest.data.model

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class CatalogDto(
    val version: Int = 1,
    @SerialName("subjects")
    @JsonNames("subjects", "courses")
    val subjects: List<CatalogSubjectEntryDto> = emptyList()
)

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class CatalogSubjectEntryDto(
    val id: String,
    val title: String,
    val description: String,
    val version: Int = 1,
    @SerialName("path")
    @JsonNames("path", "courseJsonPath")
    val path: String
)
