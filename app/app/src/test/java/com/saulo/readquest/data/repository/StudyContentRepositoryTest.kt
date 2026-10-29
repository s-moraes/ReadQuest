package com.saulo.readquest.data.repository

import com.saulo.readquest.data.local.FileContentManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class StudyContentRepositoryTest {

    private val contentDir = File("../../content")
    private val manager = FileContentManager(contentDir)
    private val repository = StudyContentRepository(manager)

    @Test
    fun repository_loadsSubjectsFromAssetsFolder() {
        val subjects = repository.getSubjects()
        assertTrue(subjects.isNotEmpty())

        val guide = repository.getSubject("readquest-guide")
        assertNotNull(guide)
        assertEquals("Mastering ReadQuest", guide?.title)
    }

    @Test
    fun repository_loadsLearningPathsAndTopics() {
        val paths = repository.getLearningPaths("readquest-guide")
        assertEquals(3, paths.size)

        val firstPath = paths.first()
        assertEquals("getting-started", firstPath.id)
        val topics = repository.getTopicsForPath(firstPath.id)
        assertEquals(2, topics.size)

        val topic1 = topics.first()
        assertEquals("guide-01-app-overview", topic1.id)
        assertEquals("App Anatomy & Core Navigation", topic1.title)
        assertTrue(topic1.markdownContent?.contains("App Anatomy") == true)
    }

    @Test
    fun repository_loadsExtrasAndQuizzes() {
        val extras = repository.getExtraItems("readquest-guide")
        assertEquals(4, extras.size)

        val quizzes = repository.getQuizTopics("readquest-guide")
        assertEquals(2, quizzes.size)

        for (quiz in quizzes) {
            val questions = repository.getQuizQuestions(quiz.id)
            assertEquals(5, questions.size)
            for (q in questions) {
                assertEquals(4, q.options.size)
                assertTrue(q.correctOptionIndex in 0..3)
                assertTrue(q.explanation.isNotEmpty())
            }
        }
    }

    @Test
    fun repository_completeTopic_updatesState() {
        val topic = repository.completeTopic("guide-01-app-overview")
        assertNotNull(topic)
        assertTrue(topic!!.isCompleted)

        val path = repository.getPath("getting-started")
        assertTrue((path?.completedCount ?: 0) >= 1)
    }

    @Test
    fun repository_recordQuizResult_marksQuizPassed() {
        val quizId = "quiz-navigation"
        repository.recordQuizResult(quizId, isPassed = true)
        val quiz = repository.getQuiz(quizId)
        assertTrue(quiz!!.isPassed)
    }
}
