package com.saulo.readquest.data.settings

import com.saulo.readquest.ui.theme.ThemeMode
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import java.io.File
import java.util.UUID

class SettingsRepositoryTest {

    private lateinit var tempFilesDir: File
    private lateinit var repository: SettingsRepository

    @Before
    fun setUp() {
        tempFilesDir = File(System.getProperty("java.io.tmpdir"), "settings_repo_test_${UUID.randomUUID()}")
        tempFilesDir.mkdirs()
        repository = SettingsRepository(tempFilesDir)
    }

    @After
    fun tearDown() {
        tempFilesDir.deleteRecursively()
    }

    @Test
    fun testInitialDefaults() {
        val settings = repository.settingsFlow.value
        assertEquals("SYSTEM", settings.themeMode)
        assertEquals(1.0f, settings.fontScale, 0.001f)
        assertNull(settings.customRepoUrl)
        assertEquals(0L, settings.lastSyncTimestamp)
        assertEquals("SEPIA", settings.customThemePreset)
    }

    @Test
    fun testUpdateLastSyncTimestamp() = runBlocking {
        val timestamp = 1730123456789L
        val updated = repository.updateLastSyncTimestamp(timestamp)

        assertEquals(timestamp, updated.lastSyncTimestamp)
        assertEquals(timestamp, repository.settingsFlow.value.lastSyncTimestamp)

        // Reload from file to ensure persistence
        val reloadedRepo = SettingsRepository(tempFilesDir)
        assertEquals(timestamp, reloadedRepo.settingsFlow.value.lastSyncTimestamp)
    }

    @Test
    fun testUpdateThemeModeAndFontScale() = runBlocking {
        repository.updateThemeMode(ThemeMode.DARK)
        assertEquals("DARK", repository.settingsFlow.value.themeMode)

        repository.updateFontScale(1.25f)
        assertEquals(1.25f, repository.settingsFlow.value.fontScale, 0.001f)
    }

    @Test
    fun testUpdateCustomColors() = runBlocking {
        repository.updateCustomColors("#121212", "#EEEEEE", "NIGHT_OWL")
        val settings = repository.settingsFlow.value
        assertEquals("#121212", settings.customBackgroundHex)
        assertEquals("#EEEEEE", settings.customTextHex)
        assertEquals("NIGHT_OWL", settings.customThemePreset)
    }
}
