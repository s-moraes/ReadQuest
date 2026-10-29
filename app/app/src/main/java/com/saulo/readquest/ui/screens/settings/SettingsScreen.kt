package com.saulo.readquest.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.saulo.readquest.ui.model.SubjectUiModel
import com.saulo.readquest.ui.screens.settings.components.AppearanceSettingsCard
import com.saulo.readquest.ui.screens.settings.components.ProgressResetSettingsCard
import com.saulo.readquest.ui.screens.settings.components.SyncSettingsCard
import com.saulo.readquest.ui.screens.settings.components.TypographySettingsCard
import com.saulo.readquest.ui.theme.EyeComfortPresets
import com.saulo.readquest.ui.theme.ThemeMode

@Composable
fun SettingsScreen(
    activeSubjectId: String = "",
    onSubjectSelected: (String) -> Unit = {},
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    onThemeModeChanged: (ThemeMode) -> Unit = {},
    fontScale: Float = 1.0f,
    onFontScaleChanged: (Float) -> Unit = {},
    customRepoUrl: String? = null,
    onCustomRepoUrlChanged: (String?) -> Unit = {},
    isSyncing: Boolean = false,
    syncProgressMessage: String? = null,
    syncErrorMessage: String? = null,
    syncSuccessMessage: String? = null,
    lastSyncTimestamp: Long = 0L,
    onSyncClicked: () -> Unit = {},
    availableSubjects: List<SubjectUiModel> = emptyList(),
    onResetSubjectProgress: () -> Unit = {},
    customThemePreset: String = EyeComfortPresets.SEPIA_ID,
    customBackgroundHex: String = EyeComfortPresets.SEPIA_BG,
    customTextHex: String = EyeComfortPresets.SEPIA_TEXT,
    onUpdateCustomColors: (String, String, String) -> Unit = { _, _, _ -> },
    modifier: Modifier = Modifier
) {
    val activeSubject = availableSubjects.firstOrNull { it.id == activeSubjectId }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            SyncSettingsCard(
                customRepoUrl = customRepoUrl,
                isSyncing = isSyncing,
                syncProgressMessage = syncProgressMessage,
                syncErrorMessage = syncErrorMessage,
                syncSuccessMessage = syncSuccessMessage,
                lastSyncTimestamp = lastSyncTimestamp,
                hasAvailableSubjects = availableSubjects.isNotEmpty(),
                onCustomRepoUrlChanged = onCustomRepoUrlChanged,
                onSyncClicked = onSyncClicked
            )
        }

        item {
            AppearanceSettingsCard(
                themeMode = themeMode,
                onThemeModeChanged = onThemeModeChanged,
                customBackgroundHex = customBackgroundHex,
                customTextHex = customTextHex,
                onUpdateCustomColors = onUpdateCustomColors
            )
        }

        item {
            TypographySettingsCard(
                fontScale = fontScale,
                onFontScaleChanged = onFontScaleChanged
            )
        }

        if (activeSubject != null) {
            item {
                ProgressResetSettingsCard(
                    activeSubjectTitle = activeSubject.title,
                    onResetSubjectProgress = onResetSubjectProgress
                )
            }
        }
    }
}
