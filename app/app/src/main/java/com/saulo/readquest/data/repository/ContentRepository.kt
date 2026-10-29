package com.saulo.readquest.data.repository

import com.saulo.readquest.data.local.FileContentManager
import com.saulo.readquest.data.model.CatalogDto
import com.saulo.readquest.data.model.SubjectManifestDto
import com.saulo.readquest.ui.model.ExtraItemUiModel
import com.saulo.readquest.ui.model.LearningPathUiModel
import com.saulo.readquest.ui.model.QuizQuestionUiModel
import com.saulo.readquest.ui.model.QuizTopicUiModel
import com.saulo.readquest.ui.model.SubjectUiModel
import com.saulo.readquest.ui.model.TopicItemUiModel
import kotlinx.coroutines.flow.StateFlow

interface ContentRepository {
    val fileContentManager: FileContentManager
    val activeSubjectManifestFlow: StateFlow<SubjectManifestDto?>

    fun loadData()
    fun setActiveSubject(subjectId: String)
    fun getManifest(subjectId: String): SubjectManifestDto?
    fun getCatalog(): CatalogDto?
    fun getSubjects(): List<SubjectUiModel>
    fun getSubject(id: String): SubjectUiModel?
    fun getLearningPaths(subjectId: String): List<LearningPathUiModel>
    fun getPath(pathId: String): LearningPathUiModel?
    fun getExtraItems(subjectId: String): List<ExtraItemUiModel>
    fun getExtraItem(extraId: String): ExtraItemUiModel?
    fun getQuizTopics(subjectId: String): List<QuizTopicUiModel>
    fun getQuiz(quizId: String): QuizTopicUiModel?
    fun getTopicsForPath(
        pathId: String,
        completedIds: Set<String> = emptySet(),
        resumeTopicId: String? = null
    ): List<TopicItemUiModel>
    fun getTopic(topicId: String, completedIds: Set<String> = emptySet()): TopicItemUiModel?
    fun getExtraContent(subjectId: String, extraId: String): String
    fun getQuizQuestions(quizId: String): List<QuizQuestionUiModel>

    @Deprecated("Progress should be recorded exclusively via UserProgressRepository")
    fun completeTopic(topicId: String): TopicItemUiModel?

    @Deprecated("Progress should be recorded exclusively via UserProgressRepository")
    fun recordQuizResult(quizId: String, isPassed: Boolean)
}
