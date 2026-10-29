package com.saulo.readquest.data.repository

import com.saulo.readquest.data.local.FileContentManager
import com.saulo.readquest.data.model.CatalogDto
import com.saulo.readquest.data.model.SubjectManifestDto
import com.saulo.readquest.data.parser.MarkdownAssetResolver
import com.saulo.readquest.data.parser.QuizMarkdownParser
import com.saulo.readquest.ui.model.ExtraCategory
import com.saulo.readquest.ui.model.ExtraItemUiModel
import com.saulo.readquest.ui.model.LearningPathUiModel
import com.saulo.readquest.ui.model.QuizQuestionUiModel
import com.saulo.readquest.ui.model.QuizTopicUiModel
import com.saulo.readquest.ui.model.SubjectUiModel
import com.saulo.readquest.ui.model.TopicItemUiModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File

class StudyContentRepository(
    override val fileContentManager: FileContentManager
) : ContentRepository {
    private val lock = Any()
    private val subjectsCache = mutableListOf<SubjectUiModel>()
    private val learningPathsCache = mutableMapOf<String, MutableList<LearningPathUiModel>>()
    private val extraItemsCache = mutableMapOf<String, MutableList<ExtraItemUiModel>>()
    private val quizTopicsCache = mutableMapOf<String, MutableList<QuizTopicUiModel>>()
    private val topicsCache = mutableMapOf<String, MutableList<TopicItemUiModel>>()
    private val completedTopicIds = mutableSetOf<String>()
    private val passedQuizIds = mutableSetOf<String>()

    private val _activeSubjectManifestFlow = MutableStateFlow<SubjectManifestDto?>(null)
    override val activeSubjectManifestFlow: StateFlow<SubjectManifestDto?> = _activeSubjectManifestFlow.asStateFlow()

    init {
        loadData()
    }

    override fun loadData() = synchronized(lock) {
        subjectsCache.clear()
        learningPathsCache.clear()
        extraItemsCache.clear()
        quizTopicsCache.clear()
        topicsCache.clear()

        val catalog = fileContentManager.readCatalog()
        if (catalog == null) {
            _activeSubjectManifestFlow.value = null
            return@synchronized
        }

        for (entry in catalog.subjects) {
            val manifest = fileContentManager.readSubjectManifest(entry.id) ?: continue
            val subjectDir = fileContentManager.getSubjectDir(entry.id)

            // Learning Paths & Topics
            val pathsList = mutableListOf<LearningPathUiModel>()
            var firstUncompletedTopic: TopicItemUiModel? = null
            var firstUncompletedPathTitle: String = ""

            for (pathDto in manifest.paths) {
                val pathTopics = mutableListOf<TopicItemUiModel>()
                for ((tIdx, topicDto) in pathDto.topics.withIndex()) {
                    val rawMd = fileContentManager.readSubjectFile(entry.id, topicDto.file)
                        ?: "# ${topicDto.title}\n\nContent is being generated."
                    val resolvedMd = MarkdownAssetResolver.resolve(rawMd, subjectDir)
                    val isDone = completedTopicIds.contains(topicDto.id)

                    val topicModel = TopicItemUiModel(
                        id = topicDto.id,
                        pathId = pathDto.id,
                        topicIndex = tIdx + 1,
                        title = topicDto.title,
                        isCompleted = isDone,
                        isCurrentResume = false,
                        markdownContent = resolvedMd,
                        relatedQuizId = topicDto.relatedQuizId,
                        relatedExtraIds = topicDto.relatedExtraIds
                    )
                    pathTopics.add(topicModel)

                    if (!isDone && firstUncompletedTopic == null) {
                        firstUncompletedTopic = topicModel
                        firstUncompletedPathTitle = pathDto.title
                    }
                }

                topicsCache[pathDto.id] = pathTopics

                val completedCount = pathTopics.count { it.isCompleted }
                pathsList.add(
                    LearningPathUiModel(
                        id = pathDto.id,
                        subjectId = manifest.id,
                        title = pathDto.title,
                        description = pathDto.description,
                        completedCount = completedCount,
                        totalCount = pathDto.topics.size
                    )
                )
            }
            learningPathsCache[manifest.id] = pathsList

            // Set current resume topic flag
            if (firstUncompletedTopic != null) {
                val pathTopics = topicsCache[firstUncompletedTopic.pathId]
                val tIndex = pathTopics?.indexOfFirst { it.id == firstUncompletedTopic.id } ?: -1
                if (tIndex != -1) {
                    pathTopics?.set(tIndex, firstUncompletedTopic.copy(isCurrentResume = true))
                }
            } else {
                // If all done or no uncompleted, pick first topic as resume
                val firstTopic = topicsCache.values.flatten().firstOrNull()
                if (firstTopic != null) {
                    firstUncompletedTopic = firstTopic
                    firstUncompletedPathTitle = pathsList.firstOrNull()?.title ?: ""
                }
            }

            // Extras
            val extrasList = manifest.extras.map { extraDto ->
                val category = when (extraDto.type.uppercase()) {
                    "USE_CASE" -> ExtraCategory.USE_CASE
                    "CURIOSITY" -> ExtraCategory.CURIOSITY
                    "HISTORY" -> ExtraCategory.HISTORY
                    "EXTERNAL_LINK" -> ExtraCategory.EXTERNAL_LINK
                    else -> ExtraCategory.ALL
                }
                ExtraItemUiModel(
                    id = extraDto.id,
                    subjectId = manifest.id,
                    title = extraDto.title,
                    summary = extraDto.summary,
                    category = category,
                    tags = extraDto.tags,
                    isLocked = extraDto.tier.equals("CORE", ignoreCase = true),
                    tierBadge = if (extraDto.tier.equals("CORE", ignoreCase = true)) "CORE" else null,
                    externalUrl = extraDto.externalUrl
                )
            }.toMutableList()
            extraItemsCache[manifest.id] = extrasList

            // Quizzes
            val quizzesList = manifest.quizzes.map { quizDto ->
                QuizTopicUiModel(
                    id = quizDto.id,
                    subjectId = manifest.id,
                    title = quizDto.title,
                    questionCount = quizDto.questionCount,
                    timeLimitMinutes = quizDto.timeLimitMinutes,
                    passPercentage = quizDto.passPercentage,
                    isPassed = passedQuizIds.contains(quizDto.id)
                )
            }.toMutableList()
            quizTopicsCache[manifest.id] = quizzesList

            // Subject UI Model
            val resumeId = firstUncompletedTopic?.id ?: "topic-none"
            val resumeTitle = firstUncompletedTopic?.title ?: "No Topic"
            val resumeIndex = firstUncompletedTopic?.topicIndex ?: 1
            val resumeTotal = topicsCache[firstUncompletedTopic?.pathId]?.size ?: 1

            subjectsCache.add(
                SubjectUiModel(
                    id = manifest.id,
                    title = manifest.title,
                    resumeTopicId = resumeId,
                    resumeTopicTitle = resumeTitle,
                    resumePathTitle = firstUncompletedPathTitle,
                    resumeTopicIndex = resumeIndex,
                    resumeTotalTopics = resumeTotal
                )
            )
        }
        // Set active subject manifest if not yet initialized
        if (_activeSubjectManifestFlow.value == null) {
            val firstSubjectId = catalog.subjects.firstOrNull()?.id
            _activeSubjectManifestFlow.value = firstSubjectId?.let { fileContentManager.readSubjectManifest(it) }
        }
    }

    override fun setActiveSubject(subjectId: String) {
        if (subjectId.isBlank()) {
            _activeSubjectManifestFlow.value = null
        } else {
            _activeSubjectManifestFlow.value = fileContentManager.readSubjectManifest(subjectId)
        }
    }

    override fun getManifest(subjectId: String): SubjectManifestDto? = fileContentManager.readSubjectManifest(subjectId)

    override fun getCatalog(): CatalogDto? = fileContentManager.readCatalog()

    override fun getSubjects(): List<SubjectUiModel> = synchronized(lock) { subjectsCache.toList() }

    override fun getSubject(id: String): SubjectUiModel? = synchronized(lock) { subjectsCache.firstOrNull { it.id == id } }

    override fun getLearningPaths(subjectId: String): List<LearningPathUiModel> =
        synchronized(lock) { learningPathsCache[subjectId]?.toList() ?: emptyList() }

    override fun getPath(pathId: String): LearningPathUiModel? =
        synchronized(lock) { learningPathsCache.values.flatten().firstOrNull { it.id == pathId } }

    override fun getExtraItems(subjectId: String): List<ExtraItemUiModel> =
        synchronized(lock) { extraItemsCache[subjectId]?.toList() ?: emptyList() }

    override fun getExtraItem(extraId: String): ExtraItemUiModel? =
        synchronized(lock) { extraItemsCache.values.flatten().firstOrNull { it.id == extraId } }

    override fun getQuizTopics(subjectId: String): List<QuizTopicUiModel> =
        synchronized(lock) { quizTopicsCache[subjectId]?.toList() ?: emptyList() }

    override fun getQuiz(quizId: String): QuizTopicUiModel? =
        synchronized(lock) { quizTopicsCache.values.flatten().firstOrNull { it.id == quizId } }

    override fun getTopicsForPath(
        pathId: String,
        completedIds: Set<String>,
        resumeTopicId: String?
    ): List<TopicItemUiModel> = synchronized(lock) {
        val list = topicsCache[pathId]?.toList() ?: return emptyList()
        if (completedIds.isEmpty() && resumeTopicId == null) return list
        return list.map { topic ->
            topic.copy(
                isCompleted = topic.id in completedIds,
                isCurrentResume = (topic.id == resumeTopicId)
            )
        }
    }

    override fun getTopic(topicId: String, completedIds: Set<String>): TopicItemUiModel? = synchronized(lock) {
        val topic = topicsCache.values.flatten().firstOrNull { it.id == topicId } ?: return null
        return if (completedIds.isNotEmpty()) topic.copy(isCompleted = topic.id in completedIds) else topic
    }

    override fun getExtraContent(subjectId: String, extraId: String): String {
        val manifest = fileContentManager.readSubjectManifest(subjectId)
        val extraDto = manifest?.extras?.firstOrNull { it.id == extraId }
        val subjectDir = fileContentManager.getSubjectDir(subjectId)
        val raw = if (extraDto?.file != null) {
            fileContentManager.readSubjectFile(subjectId, extraDto.file)
        } else null

        return if (raw != null) {
            MarkdownAssetResolver.resolve(raw, subjectDir)
        } else {
            val item = getExtraItem(extraId)
            "# ${item?.title ?: "Extra"}\n\n${item?.summary ?: ""}"
        }
    }

    override fun getQuizQuestions(quizId: String): List<QuizQuestionUiModel> {
        val subjectId = quizTopicsCache.entries.firstOrNull { entry ->
            entry.value.any { it.id == quizId }
        }?.key ?: subjectsCache.firstOrNull()?.id ?: return emptyList()

        val manifest = fileContentManager.readSubjectManifest(subjectId)
        val quizDto = manifest?.quizzes?.firstOrNull { it.id == quizId } ?: return emptyList()
        val raw = fileContentManager.readSubjectFile(subjectId, quizDto.file) ?: return emptyList()

        return QuizMarkdownParser.parse(raw, quizId)
    }

    @Deprecated("Progress should be recorded exclusively via UserProgressRepository")
    override fun completeTopic(topicId: String): TopicItemUiModel? = synchronized(lock) {
        completedTopicIds.add(topicId)
        for ((pathId, topicList) in topicsCache) {
            val idx = topicList.indexOfFirst { it.id == topicId }
            if (idx != -1) {
                val current = topicList[idx]
                val updated = current.copy(isCompleted = true, isCurrentResume = false)
                topicList[idx] = updated

                // Update parent learning path completedCount
                for (paths in learningPathsCache.values) {
                    val pIdx = paths.indexOfFirst { it.id == pathId }
                    if (pIdx != -1) {
                        val path = paths[pIdx]
                        val newCount = topicList.count { it.isCompleted }
                        paths[pIdx] = path.copy(completedCount = newCount)

                        // Update subject resume info
                        val nextTopic = topicList.firstOrNull { !it.isCompleted }
                        val subjectIdx = subjectsCache.indexOfFirst { it.id == path.subjectId }
                        if (subjectIdx != -1) {
                            val subject = subjectsCache[subjectIdx]
                            if (nextTopic != null) {
                                subjectsCache[subjectIdx] = subject.copy(
                                    resumeTopicId = nextTopic.id,
                                    resumeTopicTitle = nextTopic.title,
                                    resumeTopicIndex = nextTopic.topicIndex
                                )
                                val nextIdx = topicList.indexOfFirst { it.id == nextTopic.id }
                                if (nextIdx != -1) {
                                    topicList[nextIdx] = nextTopic.copy(isCurrentResume = true)
                                }
                            }
                        }
                        break
                    }
                }
                return updated
            }
        }
        return null
    }

    @Deprecated("Progress should be recorded exclusively via UserProgressRepository")
    override fun recordQuizResult(quizId: String, isPassed: Boolean) = synchronized(lock) {
        if (isPassed) {
            passedQuizIds.add(quizId)
            for (quizzes in quizTopicsCache.values) {
                val qIdx = quizzes.indexOfFirst { it.id == quizId }
                if (qIdx != -1) {
                    quizzes[qIdx] = quizzes[qIdx].copy(isPassed = true)
                    break
                }
            }
        }
    }
}
