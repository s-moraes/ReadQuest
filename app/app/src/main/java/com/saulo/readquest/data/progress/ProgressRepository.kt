package com.saulo.readquest.data.progress

import com.saulo.readquest.data.local.storage.AtomicJsonStore
import com.saulo.readquest.data.model.GeneralProgressDto
import com.saulo.readquest.data.model.QuizProgressDto
import com.saulo.readquest.data.model.SubjectProgressDto
import com.saulo.readquest.data.repository.UserProgressRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import java.io.File

/**
 * Single source of truth for user reading, quiz attempts, and active subject progress.
 */
class ProgressRepository(
    filesDir: File,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) : UserProgressRepository {
    val progressDir = File(filesDir, "progress").apply { if (!exists()) mkdirs() }

    private val generalStore = AtomicJsonStore(
        file = File(progressDir, "general_progress.json"),
        serializer = GeneralProgressDto.serializer(),
        defaultFactory = { GeneralProgressDto() }
    )

    override val generalProgressFlow: StateFlow<GeneralProgressDto> = generalStore.stateFlow

    private val subjectStores = mutableMapOf<String, AtomicJsonStore<SubjectProgressDto>>()

    private fun getSubjectStore(subjectId: String): AtomicJsonStore<SubjectProgressDto> {
        return synchronized(subjectStores) {
            subjectStores.getOrPut(subjectId) {
                AtomicJsonStore(
                    file = File(progressDir, "$subjectId.progress.json"),
                    serializer = SubjectProgressDto.serializer(),
                    defaultFactory = { SubjectProgressDto(subjectId = subjectId) }
                )
            }
        }
    }

    /**
     * Reactively emits the progress of whichever subject is currently active in [generalProgressFlow].
     * Driven by [flatMapLatest] to ensure automatic reactivity without manual assignment.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    override val activeSubjectProgressFlow: StateFlow<SubjectProgressDto> = generalProgressFlow
        .flatMapLatest { general ->
            getSubjectStore(general.activeSubjectId).stateFlow
        }
        .stateIn(
            scope = scope,
            started = SharingStarted.Eagerly,
            initialValue = getSubjectStore(generalProgressFlow.value.activeSubjectId).stateFlow.value
        )

    override suspend fun switchActiveSubject(subjectId: String) {
        val subjectStore = getSubjectStore(subjectId)
        generalStore.update { current ->
            current.copy(activeSubjectId = subjectId, updatedAt = System.currentTimeMillis())
        }
    }

    override suspend fun markTopicAccessed(subjectId: String, topicId: String) {
        val subjectStore = getSubjectStore(subjectId)
        val now = System.currentTimeMillis()

        subjectStore.update { current ->
            if (topicId in current.accessedTopicIds) {
                current
            } else {
                current.copy(
                    accessedTopicIds = current.accessedTopicIds + topicId,
                    updatedAt = now
                )
            }
        }

        generalStore.update { current ->
            current.copy(
                subjectSyncIndex = current.subjectSyncIndex + (subjectId to now),
                updatedAt = now
            )
        }
    }

    override suspend fun markTopicCompleted(subjectId: String, topicId: String, nextTopicId: String?) {
        val subjectStore = getSubjectStore(subjectId)
        val now = System.currentTimeMillis()

        subjectStore.update { current ->
            val updatedCompleted = current.completedTopicIds + topicId
            val updatedAccessed = current.accessedTopicIds + topicId
            current.copy(
                completedTopicIds = updatedCompleted,
                accessedTopicIds = updatedAccessed,
                resumeTopicId = nextTopicId ?: current.resumeTopicId,
                updatedAt = now
            )
        }

        generalStore.update { current ->
            current.copy(
                subjectSyncIndex = current.subjectSyncIndex + (subjectId to now),
                updatedAt = now
            )
        }
    }

    override suspend fun markExtraCompleted(subjectId: String, extraId: String) {
        val subjectStore = getSubjectStore(subjectId)
        val now = System.currentTimeMillis()

        subjectStore.update { current ->
            val updatedCompleted = current.completedExtraIds + extraId
            current.copy(
                completedExtraIds = updatedCompleted,
                updatedAt = now
            )
        }

        generalStore.update { current ->
            current.copy(
                subjectSyncIndex = current.subjectSyncIndex + (subjectId to now),
                updatedAt = now
            )
        }
    }

    override suspend fun recordQuizAttempt(
        subjectId: String,
        quizId: String,
        scorePercent: Int,
        passThreshold: Int
    ) {
        val subjectStore = getSubjectStore(subjectId)
        val now = System.currentTimeMillis()

        subjectStore.update { current ->
            val existing = current.quizzes[quizId] ?: QuizProgressDto()
            val newBest = maxOf(existing.bestScorePercent, scorePercent)
            val isPassed = existing.passed || (scorePercent >= passThreshold)
            val updatedQuiz = existing.copy(
                bestScorePercent = newBest,
                passed = isPassed,
                attempts = existing.attempts + 1,
                lastAttemptAt = now
            )
            val updatedQuizzes = current.quizzes + (quizId to updatedQuiz)
            current.copy(quizzes = updatedQuizzes, updatedAt = now)
        }

        generalStore.update { current ->
            current.copy(
                subjectSyncIndex = current.subjectSyncIndex + (subjectId to now),
                updatedAt = now
            )
        }
    }

    override suspend fun resetSubjectProgress(subjectId: String) {
        val subjectStore = getSubjectStore(subjectId)
        subjectStore.reset()

        generalStore.update { current ->
            val updatedIndex = current.subjectSyncIndex - subjectId
            current.copy(subjectSyncIndex = updatedIndex, updatedAt = System.currentTimeMillis())
        }
    }

    override fun getSubjectProgress(subjectId: String): SubjectProgressDto {
        return getSubjectStore(subjectId).stateFlow.value
    }
}
