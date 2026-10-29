package com.saulo.readquest.data.settings

import com.saulo.readquest.data.local.storage.AtomicJsonStore
import com.saulo.readquest.data.model.LocalSettingsDto
import com.saulo.readquest.data.repository.UserSettingsRepository
import com.saulo.readquest.ui.theme.ThemeMode
import kotlinx.coroutines.flow.StateFlow
import java.io.File

class SettingsRepository(
    filesDir: File
) : UserSettingsRepository {
    private val store = AtomicJsonStore(
        file = File(filesDir, "settings_local.json"),
        serializer = LocalSettingsDto.serializer(),
        defaultFactory = { LocalSettingsDto() }
    )

    override val settingsFlow: StateFlow<LocalSettingsDto> = store.stateFlow

    override suspend fun updateThemeMode(themeMode: ThemeMode): LocalSettingsDto {
        return store.update { current ->
            current.copy(themeMode = themeMode.name)
        }
    }

    override suspend fun updateFontScale(fontScale: Float): LocalSettingsDto {
        return store.update { current ->
            current.copy(fontScale = fontScale)
        }
    }

    override suspend fun updateCustomRepoUrl(customRepoUrl: String?): LocalSettingsDto {
        return store.update { current ->
            current.copy(customRepoUrl = customRepoUrl)
        }
    }

    override suspend fun updateCustomColors(
        backgroundHex: String,
        textHex: String,
        presetName: String
    ): LocalSettingsDto {
        return store.update { current ->
            current.copy(
                customBackgroundHex = backgroundHex,
                customTextHex = textHex,
                customThemePreset = presetName
            )
        }
    }

    override suspend fun updateLastSyncTimestamp(timestamp: Long): LocalSettingsDto {
        return store.update { current ->
            current.copy(lastSyncTimestamp = timestamp)
        }
    }
}
