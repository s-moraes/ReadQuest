package com.saulo.readquest.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.saulo.readquest.data.model.GeneralProgressDto
import com.saulo.readquest.data.model.LocalSettingsDto
import com.saulo.readquest.data.model.SubjectManifestDto
import com.saulo.readquest.data.model.SubjectProgressDto
import com.saulo.readquest.data.remote.ContentSyncRepository
import com.saulo.readquest.data.repository.ContentRepository
import com.saulo.readquest.data.repository.UserProgressRepository
import com.saulo.readquest.data.repository.UserSettingsRepository
import com.saulo.readquest.ui.model.ExtraItemUiModel
import com.saulo.readquest.ui.model.LearningPathUiModel
import com.saulo.readquest.ui.model.QuizQuestionUiModel
import com.saulo.readquest.ui.model.QuizTopicUiModel
import com.saulo.readquest.ui.model.TopicItemUiModel
import com.saulo.readquest.ui.navigation.AppRoute
import com.saulo.readquest.ui.navigation.NavigationTab
import com.saulo.readquest.ui.theme.ThemeMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class NavigationState(
    val selectedTab: NavigationTab = NavigationTab.LEARN,
    val currentRoute: AppRoute = AppRoute.HomeTabs,
    val isLoading: Boolean = false,
    val isSyncing: Boolean = false,
    val syncProgressMessage: String? = null,
    val syncErrorMessage: String? = null,
    val syncSuccessMessage: String? = null
)

class StudyHubViewModel(
    val settingsRepository: UserSettingsRepository,
    val progressRepository: UserProgressRepository,
    val contentRepository: ContentRepository,
    val contentSyncRepository: ContentSyncRepository? = null,
    customScope: CoroutineScope? = null,
    sharingStarted: SharingStarted = SharingStarted.WhileSubscribed(5_000)
) : ViewModel() {

    private val scope: CoroutineScope = customScope ?: viewModelScope

    private val navigationState = MutableStateFlow(NavigationState())

    init {
        scope.launch {
            progressRepository.generalProgressFlow.collect { general ->
                contentRepository.setActiveSubject(general.activeSubjectId)
            }
        }
    }

    val uiState: StateFlow<StudyHubUiState> = combine(
        settingsRepository.settingsFlow,
        progressRepository.generalProgressFlow,
        contentRepository.activeSubjectManifestFlow,
        progressRepository.activeSubjectProgressFlow,
        navigationState
    ) { settings, generalProgress, manifest, subjectProgress, navState ->
        buildUiState(settings, generalProgress, manifest, subjectProgress, navState)
    }.stateIn(
        scope = scope,
        started = sharingStarted,
        initialValue = StudyHubUiState()
    )

    private fun buildUiState(
        settings: LocalSettingsDto,
        generalProgress: GeneralProgressDto,
        manifest: SubjectManifestDto?,
        subjectProgress: SubjectProgressDto,
        navState: NavigationState
    ): StudyHubUiState {
        val activeSubjectManifest = manifest ?: if (generalProgress.activeSubjectId.isNotBlank()) {
            contentRepository.getManifest(generalProgress.activeSubjectId)
        } else null

        val (state, fallbackCandidateId) = StudyUiStateMapper.mapToUiState(
            settings = settings,
            generalProgress = generalProgress,
            manifest = activeSubjectManifest,
            subjectProgress = subjectProgress,
            navState = navState,
            catalog = contentRepository.getCatalog(),
            getSubjectProgress = { id -> progressRepository.getSubjectProgress(id) }
        )

        if (fallbackCandidateId != null) {
            scope.launch {
                onSelectSubject(fallbackCandidateId)
            }
        }

        return state
    }

    fun onTabSelected(tab: NavigationTab) {
        navigationState.value = navigationState.value.copy(selectedTab = tab)
    }

    fun onNavigate(route: AppRoute) {
        navigationState.value = navigationState.value.copy(currentRoute = route)
    }

    fun onNavigateBack() {
        when (val route = navigationState.value.currentRoute) {
            is AppRoute.Settings -> {
                onNavigate(AppRoute.HomeTabs)
            }
            is AppRoute.PathTopics -> {
                onNavigate(AppRoute.HomeTabs)
                onTabSelected(NavigationTab.LEARN)
            }
            is AppRoute.ContentReader -> {
                onNavigate(AppRoute.HomeTabs)
                if (route.isExtra) {
                    onTabSelected(NavigationTab.EXTRA)
                } else {
                    onTabSelected(NavigationTab.LEARN)
                }
            }
            is AppRoute.QuizResultReview -> {
                onNavigate(AppRoute.HomeTabs)
                onTabSelected(NavigationTab.QUIZ)
            }
            is AppRoute.QuizRunner -> {
                onNavigate(AppRoute.HomeTabs)
                onTabSelected(NavigationTab.QUIZ)
            }
            AppRoute.HomeTabs -> Unit
        }
    }

    fun onSelectSubject(subjectId: String): Job = scope.launch {
        progressRepository.switchActiveSubject(subjectId)
        contentRepository.setActiveSubject(subjectId)
    }

    fun onThemeModeChanged(themeMode: ThemeMode): Job = scope.launch {
        settingsRepository.updateThemeMode(themeMode)
    }

    fun onUpdateCustomColors(
        backgroundHex: String,
        textHex: String,
        presetName: String = "CUSTOM"
    ): Job = scope.launch {
        settingsRepository.updateCustomColors(backgroundHex, textHex, presetName)
    }

    fun onFontScaleChanged(fontScale: Float): Job = scope.launch {
        settingsRepository.updateFontScale(fontScale)
    }

    fun onCustomRepoUrlChanged(customRepoUrl: String?): Job = scope.launch {
        settingsRepository.updateCustomRepoUrl(customRepoUrl)
    }

    fun onTopicAccessed(subjectId: String, topicId: String): Job = scope.launch {
        progressRepository.markTopicAccessed(subjectId, topicId)
    }

    fun onTopicCompleted(subjectId: String, topicId: String, nextTopicId: String? = null): Job = scope.launch {
        progressRepository.markTopicCompleted(subjectId, topicId, nextTopicId)
    }

    fun onExtraCompleted(subjectId: String, extraId: String): Job = scope.launch {
        progressRepository.markExtraCompleted(subjectId, extraId)
    }

    fun onQuizSubmitted(subjectId: String, quizId: String, scorePercent: Int, passThreshold: Int): Job = scope.launch {
        progressRepository.recordQuizAttempt(subjectId, quizId, scorePercent, passThreshold)
    }

    fun onResetSubjectProgress(subjectId: String): Job = scope.launch {
        progressRepository.resetSubjectProgress(subjectId)
    }

    fun getPath(pathId: String): LearningPathUiModel? = contentRepository.getPath(pathId)

    fun getTopicsForPath(
        pathId: String,
        completedIds: Set<String> = uiState.value.currentSubjectProgress.completedTopicIds,
        resumeTopicId: String? = uiState.value.currentSubjectProgress.resumeTopicId
    ): List<TopicItemUiModel> = contentRepository.getTopicsForPath(pathId, completedIds, resumeTopicId)

    fun getTopic(
        topicId: String,
        completedIds: Set<String> = uiState.value.currentSubjectProgress.completedTopicIds
    ): TopicItemUiModel? = contentRepository.getTopic(topicId, completedIds)

    fun getTopicMarkdown(topicId: String): String? = contentRepository.getTopic(topicId)?.markdownContent

    fun getExtraItem(extraId: String): ExtraItemUiModel? = contentRepository.getExtraItem(extraId)

    fun getExtraContent(subjectId: String, extraId: String): String = contentRepository.getExtraContent(subjectId, extraId)

    fun getQuiz(quizId: String): QuizTopicUiModel? = contentRepository.getQuiz(quizId)

    fun getQuizQuestions(quizId: String): List<QuizQuestionUiModel> = contentRepository.getQuizQuestions(quizId)

    fun refreshContent() {
        contentRepository.loadData()
        val currentSubjectId = progressRepository.generalProgressFlow.value.activeSubjectId
        contentRepository.setActiveSubject(currentSubjectId)
    }

    fun syncFromWeb(): Job = scope.launch {
        navigationState.value = navigationState.value.copy(
            isSyncing = true,
            syncProgressMessage = "Connecting to repository...",
            syncErrorMessage = null,
            syncSuccessMessage = null
        )
        val baseUrl = settingsRepository.settingsFlow.value.customRepoUrl
            ?: ContentSyncRepository.DEFAULT_BASE_URL
        val syncRepo = contentSyncRepository ?: ContentSyncRepository(contentRepository.fileContentManager.contentDir)
        val result = syncRepo.syncContent(baseUrl) { stepMessage ->
            navigationState.value = navigationState.value.copy(syncProgressMessage = stepMessage)
        }

        if (result.isSuccess) {
            val summary = result.getOrNull()
            val now = System.currentTimeMillis()
            settingsRepository.updateLastSyncTimestamp(now)
            contentRepository.loadData()
            val firstSubjectId = contentRepository.getCatalog()?.subjects?.firstOrNull()?.id
            if (firstSubjectId != null && progressRepository.generalProgressFlow.value.activeSubjectId.isBlank()) {
                progressRepository.switchActiveSubject(firstSubjectId)
                contentRepository.setActiveSubject(firstSubjectId)
            }
            navigationState.value = navigationState.value.copy(
                isSyncing = false,
                syncProgressMessage = null,
                syncErrorMessage = null,
                syncSuccessMessage = "Downloaded ${summary?.subjectsCount ?: 0} subjects (${summary?.filesCount ?: 0} files)."
            )
        } else {
            val error = result.exceptionOrNull()?.localizedMessage ?: "Unknown network error"
            navigationState.value = navigationState.value.copy(
                isSyncing = false,
                syncProgressMessage = null,
                syncErrorMessage = "Sync failed: $error",
                syncSuccessMessage = null
            )
        }
    }
}
