package com.saulo.readquest.ui.viewmodel

import com.saulo.readquest.data.model.CatalogDto
import com.saulo.readquest.data.model.CatalogSubjectEntryDto
import com.saulo.readquest.data.model.ExtraItemDto
import com.saulo.readquest.data.model.GeneralProgressDto
import com.saulo.readquest.data.model.LearningPathDto
import com.saulo.readquest.data.model.LocalSettingsDto
import com.saulo.readquest.data.model.QuizIndexDto
import com.saulo.readquest.data.model.QuizProgressDto
import com.saulo.readquest.data.model.SubjectManifestDto
import com.saulo.readquest.data.model.SubjectProgressDto
import com.saulo.readquest.data.model.TopicIndexDto
import com.saulo.readquest.ui.model.ExtraCategory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class StudyUiStateMapperTest {

    @Test
    fun mapToUiState_withNullManifest_returnsEmptyCollections() {
        val (state, fallbackId) = StudyUiStateMapper.mapToUiState(
            settings = LocalSettingsDto(),
            generalProgress = GeneralProgressDto(activeSubjectId = ""),
            manifest = null,
            subjectProgress = SubjectProgressDto(),
            navState = NavigationState(),
            catalog = null,
            getSubjectProgress = { SubjectProgressDto() }
        )

        assertTrue(state.learningPaths.isEmpty())
        assertTrue(state.topics.isEmpty())
        assertTrue(state.extraItems.isEmpty())
        assertTrue(state.quizTopics.isEmpty())
        assertNull(state.activeSubject)
        assertNull(fallbackId)
    }

    @Test
    fun mapToUiState_withManifest_mapsLearningPathsAndCalculatesProgress() {
        val manifest = SubjectManifestDto(
            id = "sub-1",
            title = "Subject 1",
            version = 1,
            paths = listOf(
                LearningPathDto(
                    id = "p-1",
                    title = "Path 1",
                    description = "Description 1",
                    icon = "code",
                    topics = listOf(
                        TopicIndexDto(id = "t-1", title = "Topic 1", file = "01.md"),
                        TopicIndexDto(id = "t-2", title = "Topic 2", file = "02.md")
                    )
                )
            )
        )
        val progress = SubjectProgressDto(
            subjectId = "sub-1",
            completedTopicIds = setOf("t-1")
        )

        val (state, _) = StudyUiStateMapper.mapToUiState(
            settings = LocalSettingsDto(),
            generalProgress = GeneralProgressDto(activeSubjectId = "sub-1"),
            manifest = manifest,
            subjectProgress = progress,
            navState = NavigationState(),
            catalog = null,
            getSubjectProgress = { progress }
        )

        assertEquals(1, state.learningPaths.size)
        val path = state.learningPaths.first()
        assertEquals("p-1", path.id)
        assertEquals(1, path.completedCount)
        assertEquals(2, path.totalCount)
    }

    @Test
    fun mapToUiState_withExtras_calculatesUnlockStateAndCategories() {
        val manifest = SubjectManifestDto(
            id = "sub-1",
            title = "Subject 1",
            version = 1,
            paths = listOf(
                LearningPathDto(
                    id = "p-1",
                    title = "Path 1",
                    description = "Description 1",
                    icon = "code",
                    topics = listOf(
                        TopicIndexDto(id = "t-1", title = "Topic 1", file = "01.md")
                    )
                )
            ),
            extras = listOf(
                ExtraItemDto(
                    id = "e-unlocked",
                    title = "Unlocked Extra",
                    summary = "Summary",
                    type = "USE_CASE"
                ),
                ExtraItemDto(
                    id = "e-locked",
                    title = "Locked Extra",
                    summary = "Summary",
                    type = "CURIOSITY",
                    unlockAfterPathId = "p-1"
                )
            )
        )

        // Case 1: Topic 1 not completed -> e-locked is locked
        val (state1, _) = StudyUiStateMapper.mapToUiState(
            settings = LocalSettingsDto(),
            generalProgress = GeneralProgressDto(activeSubjectId = "sub-1"),
            manifest = manifest,
            subjectProgress = SubjectProgressDto(subjectId = "sub-1"),
            navState = NavigationState(),
            catalog = null,
            getSubjectProgress = { SubjectProgressDto() }
        )

        val unl1 = state1.extraItems.first { it.id == "e-unlocked" }
        assertFalse(unl1.isLocked)
        assertEquals(ExtraCategory.USE_CASE, unl1.category)

        val lock1 = state1.extraItems.first { it.id == "e-locked" }
        assertTrue(lock1.isLocked)
        assertEquals("Path 1", lock1.unlockRequirementTitle)
        assertEquals(ExtraCategory.CURIOSITY, lock1.category)

        // Case 2: Topic 1 completed -> e-locked becomes unlocked
        val (state2, _) = StudyUiStateMapper.mapToUiState(
            settings = LocalSettingsDto(),
            generalProgress = GeneralProgressDto(activeSubjectId = "sub-1"),
            manifest = manifest,
            subjectProgress = SubjectProgressDto(subjectId = "sub-1", completedTopicIds = setOf("t-1")),
            navState = NavigationState(),
            catalog = null,
            getSubjectProgress = { SubjectProgressDto() }
        )

        val lock2 = state2.extraItems.first { it.id == "e-locked" }
        assertFalse(lock2.isLocked)
    }

    @Test
    fun mapToUiState_withQuizzes_mapsAttemptsAndBestScores() {
        val manifest = SubjectManifestDto(
            id = "sub-1",
            title = "Subject 1",
            version = 1,
            quizzes = listOf(
                QuizIndexDto(
                    id = "q-1",
                    title = "Quiz 1",
                    file = "q1.md",
                    questionCount = 10,
                    timeLimitMinutes = 15,
                    passPercentage = 80
                )
            )
        )
        val progress = SubjectProgressDto(
            subjectId = "sub-1",
            quizzes = mapOf(
                "q-1" to QuizProgressDto(
                    bestScorePercent = 90,
                    passed = true,
                    attempts = 2
                )
            )
        )

        val (state, _) = StudyUiStateMapper.mapToUiState(
            settings = LocalSettingsDto(),
            generalProgress = GeneralProgressDto(activeSubjectId = "sub-1"),
            manifest = manifest,
            subjectProgress = progress,
            navState = NavigationState(),
            catalog = null,
            getSubjectProgress = { progress }
        )

        assertEquals(1, state.quizTopics.size)
        val quiz = state.quizTopics.first()
        assertEquals(90, quiz.bestScorePercent)
        assertTrue(quiz.isPassed)
        assertEquals(2, quiz.attempts)
    }

    @Test
    fun mapToUiState_sequentialTopicIndexingAcrossPaths() {
        val manifest = SubjectManifestDto(
            id = "sub-1",
            title = "Subject 1",
            version = 1,
            paths = listOf(
                LearningPathDto(
                    id = "p-1",
                    title = "Path 1",
                    description = "D1",
                    icon = "code",
                    topics = listOf(
                        TopicIndexDto(id = "t-1", title = "T1", file = "01.md"),
                        TopicIndexDto(id = "t-2", title = "T2", file = "02.md")
                    )
                ),
                LearningPathDto(
                    id = "p-2",
                    title = "Path 2",
                    description = "D2",
                    icon = "code",
                    topics = listOf(
                        TopicIndexDto(id = "t-3", title = "T3", file = "03.md")
                    )
                )
            )
        )

        val (state, _) = StudyUiStateMapper.mapToUiState(
            settings = LocalSettingsDto(),
            generalProgress = GeneralProgressDto(activeSubjectId = "sub-1"),
            manifest = manifest,
            subjectProgress = SubjectProgressDto(subjectId = "sub-1"),
            navState = NavigationState(),
            catalog = null,
            getSubjectProgress = { SubjectProgressDto() }
        )

        assertEquals(3, state.topics.size)
        assertEquals(1, state.topics[0].topicIndex)
        assertEquals(2, state.topics[1].topicIndex)
        assertEquals(3, state.topics[2].topicIndex)
        assertTrue(state.topics[0].isCurrentResume)
    }

    @Test
    fun mapToUiState_fallbackCandidateIdTriggeredWhenActiveSubjectBlank() {
        val catalog = CatalogDto(
            version = 1,
            subjects = listOf(
                CatalogSubjectEntryDto(id = "sub-catalog-1", title = "Subject Catalog 1", description = "Desc", version = 1, path = "")
            )
        )

        val (state, fallbackId) = StudyUiStateMapper.mapToUiState(
            settings = LocalSettingsDto(),
            generalProgress = GeneralProgressDto(activeSubjectId = ""),
            manifest = null,
            subjectProgress = SubjectProgressDto(),
            navState = NavigationState(),
            catalog = catalog,
            getSubjectProgress = { SubjectProgressDto(subjectId = it) }
        )

        assertEquals("sub-catalog-1", fallbackId)
        assertNotNull(state.activeSubject)
        assertEquals("sub-catalog-1", state.activeSubject?.id)
    }
}
