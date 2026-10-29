package com.saulo.readquest.data.model

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.File

class SubjectManifestDtoTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun readquestGuideManifest_deserializesCorrectly() {
        val file = File("../../content/readquest-guide/subject.json")
        val manifest = json.decodeFromString<SubjectManifestDto>(file.readText())

        assertEquals("readquest-guide", manifest.id)
        assertEquals("Mastering ReadQuest", manifest.title)
        assertEquals(3, manifest.paths.size)
        assertEquals(4, manifest.extras.size)
        assertEquals(2, manifest.quizzes.size)

        val firstPath = manifest.paths.first()
        assertEquals("Foundations & Navigation", firstPath.title)
        assertEquals(2, firstPath.topics.size)

        val firstQuiz = manifest.quizzes.first()
        assertEquals("Navigation & Reading Fundamentals", firstQuiz.title)
        assertEquals(5, firstQuiz.questionCount)
    }
}
