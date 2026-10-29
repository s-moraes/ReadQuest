package com.saulo.readquest.ui.viewmodel

import androidx.compose.runtime.Immutable
import com.saulo.readquest.data.model.GeneralProgressDto
import com.saulo.readquest.data.model.LocalSettingsDto
import com.saulo.readquest.data.model.SubjectProgressDto
import com.saulo.readquest.ui.model.ExtraItemUiModel
import com.saulo.readquest.ui.model.LearningPathUiModel
import com.saulo.readquest.ui.model.QuizTopicUiModel
import com.saulo.readquest.ui.model.SubjectUiModel
import com.saulo.readquest.ui.model.TopicItemUiModel
import com.saulo.readquest.ui.navigation.AppRoute
import com.saulo.readquest.ui.navigation.NavigationTab
import com.saulo.readquest.ui.theme.ThemeMode

@Immutable
data class StudyHubUiState(
    val selectedTab: NavigationTab = NavigationTab.LEARN,
    val currentRoute: AppRoute = AppRoute.HomeTabs,
    val settings: LocalSettingsDto = LocalSettingsDto(),
    val generalProgress: GeneralProgressDto = GeneralProgressDto(),
    val currentSubjectProgress: SubjectProgressDto = SubjectProgressDto(),
    val activeSubject: SubjectUiModel? = null,
    val availableSubjects: List<SubjectUiModel> = emptyList(),
    val learningPaths: List<LearningPathUiModel> = emptyList(),
    val topics: List<TopicItemUiModel> = emptyList(),
    val extraItems: List<ExtraItemUiModel> = emptyList(),
    val quizTopics: List<QuizTopicUiModel> = emptyList(),
    val isLoading: Boolean = false,
    val isSyncing: Boolean = false,
    val syncProgressMessage: String? = null,
    val syncErrorMessage: String? = null,
    val syncSuccessMessage: String? = null
) {
    val themeMode: ThemeMode
        get() = try {
            ThemeMode.valueOf(settings.themeMode)
        } catch (_: Exception) {
            ThemeMode.SYSTEM
        }

    val fontScale: Float
        get() = settings.fontScale

    val activeSubjectId: String
        get() = generalProgress.activeSubjectId

    val customBackgroundHex: String
        get() = settings.customBackgroundHex

    val customTextHex: String
        get() = settings.customTextHex

    val customThemePreset: String
        get() = settings.customThemePreset

    val lastSyncTimestamp: Long
        get() = settings.lastSyncTimestamp
}
