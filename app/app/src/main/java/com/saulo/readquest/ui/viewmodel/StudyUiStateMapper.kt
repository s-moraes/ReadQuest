package com.saulo.readquest.ui.viewmodel

import com.saulo.readquest.data.model.CatalogDto
import com.saulo.readquest.data.model.GeneralProgressDto
import com.saulo.readquest.data.model.LocalSettingsDto
import com.saulo.readquest.data.model.SubjectManifestDto
import com.saulo.readquest.data.model.SubjectProgressDto
import com.saulo.readquest.ui.model.ExtraCategory
import com.saulo.readquest.ui.model.ExtraItemUiModel
import com.saulo.readquest.ui.model.LearningPathUiModel
import com.saulo.readquest.ui.model.QuizTopicUiModel
import com.saulo.readquest.ui.model.SubjectUiModel
import com.saulo.readquest.ui.model.TopicItemUiModel

/**
 * Dedicated mapper that transforms data transfer objects (DTOs) and repository flows
 * into immutable [StudyHubUiState] for presentation.
 * Follows Single Responsibility Principle (SRP) and enables isolated unit testing.
 */
object StudyUiStateMapper {

    fun mapToUiState(
        settings: LocalSettingsDto,
        generalProgress: GeneralProgressDto,
        manifest: SubjectManifestDto?,
        subjectProgress: SubjectProgressDto,
        navState: NavigationState,
        catalog: CatalogDto?,
        getSubjectProgress: (String) -> SubjectProgressDto
    ): Pair<StudyHubUiState, String?> {
        val subjectId = generalProgress.activeSubjectId
        val activeSubjectManifest = manifest

        val learningPaths: List<LearningPathUiModel>
        val topics: List<TopicItemUiModel>
        val extraItems: List<ExtraItemUiModel>
        val quizTopics: List<QuizTopicUiModel>
        var activeSubject: SubjectUiModel? = null

        if (activeSubjectManifest != null) {
            learningPaths = activeSubjectManifest.paths.map { path ->
                val completedCount = path.topics.count { it.id in subjectProgress.completedTopicIds }
                LearningPathUiModel(
                    id = path.id,
                    subjectId = activeSubjectManifest.id,
                    title = path.title,
                    description = path.description,
                    completedCount = completedCount,
                    totalCount = path.topics.size
                )
            }

            extraItems = activeSubjectManifest.extras.map { extra ->
                val targetPath = if (extra.unlockAfterPathId != null) {
                    activeSubjectManifest.paths.firstOrNull { it.id == extra.unlockAfterPathId }
                } else null

                val isLocked = if (targetPath == null) {
                    false
                } else {
                    !targetPath.topics.all { it.id in subjectProgress.completedTopicIds }
                }

                val unlockRequirementTitle = if (isLocked) targetPath?.title else null
                val isCompleted = extra.id in subjectProgress.completedExtraIds

                val category = when (extra.type.uppercase()) {
                    "USE_CASE" -> ExtraCategory.USE_CASE
                    "CURIOSITY" -> ExtraCategory.CURIOSITY
                    "HISTORY" -> ExtraCategory.HISTORY
                    "EXTERNAL_LINK" -> ExtraCategory.EXTERNAL_LINK
                    else -> ExtraCategory.ALL
                }

                ExtraItemUiModel(
                    id = extra.id,
                    subjectId = activeSubjectManifest.id,
                    title = extra.title,
                    summary = extra.summary,
                    category = category,
                    tags = extra.tags,
                    isLocked = isLocked,
                    tierBadge = if (extra.tier.equals("CORE", ignoreCase = true)) "CORE" else null,
                    externalUrl = extra.externalUrl,
                    isCompleted = isCompleted,
                    unlockRequirementTitle = unlockRequirementTitle
                )
            }

            quizTopics = activeSubjectManifest.quizzes.map { quiz ->
                val qProgress = subjectProgress.quizzes[quiz.id]
                QuizTopicUiModel(
                    id = quiz.id,
                    subjectId = activeSubjectManifest.id,
                    title = quiz.title,
                    questionCount = quiz.questionCount,
                    timeLimitMinutes = quiz.timeLimitMinutes,
                    passPercentage = quiz.passPercentage,
                    isPassed = qProgress?.passed == true,
                    bestScorePercent = qProgress?.bestScorePercent ?: 0,
                    attempts = qProgress?.attempts ?: 0
                )
            }

            val allTopicsWithPath = activeSubjectManifest.paths.flatMap { path ->
                path.topics.map { topic -> topic to path }
            }

            val resumePair = if (subjectProgress.resumeTopicId != null) {
                allTopicsWithPath.firstOrNull { it.first.id == subjectProgress.resumeTopicId }
                    ?: allTopicsWithPath.firstOrNull { it.first.id !in subjectProgress.completedTopicIds }
                    ?: allTopicsWithPath.firstOrNull()
            } else {
                allTopicsWithPath.firstOrNull { it.first.id !in subjectProgress.completedTopicIds }
                    ?: allTopicsWithPath.firstOrNull()
            }

            val resumeTopic = resumePair?.first
            val resumePath = resumePair?.second
            val resumeIndex = if (resumeTopic != null) {
                allTopicsWithPath.indexOfFirst { it.first.id == resumeTopic.id } + 1
            } else 1
            val resumeTotal = if (allTopicsWithPath.isNotEmpty()) allTopicsWithPath.size else 1

            val isResumeAccessed = resumeTopic != null && (
                resumeTopic.id in subjectProgress.accessedTopicIds ||
                resumeTopic.id in subjectProgress.completedTopicIds
            )

            activeSubject = SubjectUiModel(
                id = activeSubjectManifest.id,
                title = activeSubjectManifest.title,
                resumeTopicId = resumeTopic?.id ?: "",
                resumeTopicTitle = resumeTopic?.title ?: "Start Learning",
                resumePathTitle = resumePath?.title ?: "",
                resumeTopicIndex = resumeIndex,
                resumeTotalTopics = resumeTotal,
                isResumeTopicAccessed = isResumeAccessed
            )

            var globalTopicIndex = 1
            topics = activeSubjectManifest.paths.flatMap { path ->
                path.topics.map { topicDto ->
                    val isDone = topicDto.id in subjectProgress.completedTopicIds
                    val isResume = (topicDto.id == (resumeTopic?.id ?: ""))
                    TopicItemUiModel(
                        id = topicDto.id,
                        pathId = path.id,
                        topicIndex = globalTopicIndex++,
                        title = topicDto.title,
                        isCompleted = isDone,
                        isCurrentResume = isResume,
                        markdownContent = "", // Loaded on demand by ContentReaderScreen
                        relatedQuizId = topicDto.relatedQuizId,
                        relatedExtraIds = topicDto.relatedExtraIds
                    )
                }
            }
        } else {
            learningPaths = emptyList()
            topics = emptyList()
            extraItems = emptyList()
            quizTopics = emptyList()
            activeSubject = null
        }

        val availableSubjects = catalog?.subjects?.map { entry ->
            val sProgress = getSubjectProgress(entry.id)
            val isCandidateResumeAccessed = sProgress.resumeTopicId != null && (
                sProgress.resumeTopicId in sProgress.accessedTopicIds ||
                sProgress.resumeTopicId in sProgress.completedTopicIds
            )
            SubjectUiModel(
                id = entry.id,
                title = entry.title,
                resumeTopicId = sProgress.resumeTopicId ?: "",
                resumeTopicTitle = "",
                resumePathTitle = "",
                resumeTopicIndex = 1,
                resumeTotalTopics = 1,
                isResumeTopicAccessed = isCandidateResumeAccessed
            )
        } ?: emptyList()

        var fallbackCandidateId: String? = null
        if (activeSubject == null && availableSubjects.isNotEmpty()) {
            val candidate = availableSubjects.firstOrNull { it.id == subjectId } ?: availableSubjects.first()
            activeSubject = candidate
            if (subjectId.isBlank() || subjectId != candidate.id) {
                fallbackCandidateId = candidate.id
            }
        }

        val state = StudyHubUiState(
            selectedTab = navState.selectedTab,
            currentRoute = navState.currentRoute,
            settings = settings,
            generalProgress = generalProgress,
            currentSubjectProgress = subjectProgress,
            activeSubject = activeSubject,
            availableSubjects = availableSubjects,
            learningPaths = learningPaths,
            topics = topics,
            extraItems = extraItems,
            quizTopics = quizTopics,
            isLoading = navState.isLoading,
            isSyncing = navState.isSyncing,
            syncProgressMessage = navState.syncProgressMessage,
            syncErrorMessage = navState.syncErrorMessage,
            syncSuccessMessage = navState.syncSuccessMessage
        )

        return state to fallbackCandidateId
    }
}
