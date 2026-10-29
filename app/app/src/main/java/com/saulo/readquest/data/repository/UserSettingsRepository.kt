package com.saulo.readquest.data.repository

import com.saulo.readquest.data.model.LocalSettingsDto
import com.saulo.readquest.ui.theme.ThemeMode
import kotlinx.coroutines.flow.StateFlow

interface UserSettingsRepository {
    val settingsFlow: StateFlow<LocalSettingsDto>

    suspend fun updateThemeMode(themeMode: ThemeMode): LocalSettingsDto
    suspend fun updateFontScale(fontScale: Float): LocalSettingsDto
    suspend fun updateCustomRepoUrl(customRepoUrl: String?): LocalSettingsDto
    suspend fun updateCustomColors(
        backgroundHex: String,
        textHex: String,
        presetName: String = "CUSTOM"
    ): LocalSettingsDto
    suspend fun updateLastSyncTimestamp(timestamp: Long): LocalSettingsDto
}
