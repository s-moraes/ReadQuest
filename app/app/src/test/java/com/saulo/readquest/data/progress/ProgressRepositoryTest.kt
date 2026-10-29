package com.saulo.readquest.data.progress

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
class ProgressRepositoryTest {

    private lateinit var tempFilesDir: File
    private lateinit var repository: ProgressRepository

    @Before
    fun setUp() {
        tempFilesDir = File(System.getProperty("java.io.tmpdir"), "readquest_progress_repo_test_${System.currentTimeMillis()}")
        tempFilesDir.mkdirs()
        repository = ProgressRepository(tempFilesDir, CoroutineScope(Dispatchers.Unconfined))
    }

    @After
    fun tearDown() {
        tempFilesDir.deleteRecursively()
    }

    @Test
    fun testInitialDefaultValues() = runBlocking {
        val general = repository.generalProgressFlow.value
        assertEquals("", general.activeSubjectId)

        val activeSubject = repository.activeSubjectProgressFlow.value
        assertEquals("", activeSubject.subjectId)
        assertTrue(activeSubject.completedTopicIds.isEmpty())
        assertTrue(activeSubject.completedExtraIds.isEmpty())
        assertTrue(activeSubject.quizzes.isEmpty())
    }

    @Test
    fun testSwitchActiveSubjectUpdatesGeneralStoreAndFlow() = runBlocking {
        repository.switchActiveSubject("java-foundations")

        assertEquals("java-foundations", repository.generalProgressFlow.value.activeSubjectId)
        val subjectProgress = repository.activeSubjectProgressFlow.value
        assertEquals("java-foundations", subjectProgress.subjectId)
    }

    @Test
    fun testMarkTopicAccessedUpdatesAccessedTopicIds() = runBlocking {
        repository.markTopicAccessed("system-design", "topic-101")

        val subjectProgress = repository.getSubjectProgress("system-design")
        assertTrue(subjectProgress.accessedTopicIds.contains("topic-101"))
        assertTrue(subjectProgress.completedTopicIds.isEmpty())
        assertTrue(subjectProgress.updatedAt > 0L)
    }

    @Test
    fun testMarkTopicCompletedUpdatesResumeAndCompletedSet() = runBlocking {
        repository.markTopicCompleted("system-design", "topic-101", "topic-102")

        val subjectProgress = repository.getSubjectProgress("system-design")
        assertTrue(subjectProgress.completedTopicIds.contains("topic-101"))
        assertTrue(subjectProgress.accessedTopicIds.contains("topic-101"))
        assertEquals("topic-102", subjectProgress.resumeTopicId)
        assertTrue(subjectProgress.updatedAt > 0L)
    }

    @Test
    fun testMarkExtraCompletedUpdatesCompletedExtraIds() = runBlocking {
        repository.markExtraCompleted("system-design", "extra-netflix")

        val subjectProgress = repository.getSubjectProgress("system-design")
        assertTrue(subjectProgress.completedExtraIds.contains("extra-netflix"))
    }

    @Test
    fun testRecordQuizAttemptComputesBestScoreAndPassThreshold() = runBlocking {
        // Attempt 1: Failed (60% < 70%)
        repository.recordQuizAttempt("system-design", "quiz-basics", scorePercent = 60, passThreshold = 70)
        var subjectProgress = repository.getSubjectProgress("system-design")
        var quiz = subjectProgress.quizzes["quiz-basics"]!!
        assertEquals(60, quiz.bestScorePercent)
        assertFalse(quiz.passed)
        assertEquals(1, quiz.attempts)

        // Attempt 2: Passed with 80% (80% >= 70%)
        repository.recordQuizAttempt("system-design", "quiz-basics", scorePercent = 80, passThreshold = 70)
        subjectProgress = repository.getSubjectProgress("system-design")
        quiz = subjectProgress.quizzes["quiz-basics"]!!
        assertEquals(80, quiz.bestScorePercent)
        assertTrue(quiz.passed)
        assertEquals(2, quiz.attempts)

        // Attempt 3: Lower score does not overwrite best score, remains passed
        repository.recordQuizAttempt("system-design", "quiz-basics", scorePercent = 50, passThreshold = 70)
        subjectProgress = repository.getSubjectProgress("system-design")
        quiz = subjectProgress.quizzes["quiz-basics"]!!
        assertEquals(80, quiz.bestScorePercent)
        assertTrue(quiz.passed)
        assertEquals(3, quiz.attempts)
    }

    @Test
    fun testResetSubjectProgressDeletesFileAndResetsState() = runBlocking {
        repository.markTopicCompleted("system-design", "topic-101")
        repository.recordQuizAttempt("system-design", "quiz-basics", scorePercent = 100, passThreshold = 70)

        val progressFile = File(repository.progressDir, "system-design.progress.json")
        assertTrue(progressFile.exists())
        assertTrue(repository.generalProgressFlow.value.subjectSyncIndex.containsKey("system-design"))

        repository.resetSubjectProgress("system-design")

        assertFalse(progressFile.exists())
        assertFalse(repository.generalProgressFlow.value.subjectSyncIndex.containsKey("system-design"))
        val subjectProgress = repository.getSubjectProgress("system-design")
        assertTrue(subjectProgress.completedTopicIds.isEmpty())
        assertTrue(subjectProgress.quizzes.isEmpty())
    }
}
