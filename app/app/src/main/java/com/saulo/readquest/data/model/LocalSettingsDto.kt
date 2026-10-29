package com.saulo.readquest.data.model

import kotlinx.serialization.Serializable

@Serializable
data class LocalSettingsDto(
    val themeMode: String = "SYSTEM",
    val fontScale: Float = 1.0f,
    val customRepoUrl: String? = null,
    val customThemePreset: String = "SEPIA",
    val customBackgroundHex: String = "#F4ECD8",
    val customTextHex: String = "#2C2416",
    val lastSyncTimestamp: Long = 0L
)
