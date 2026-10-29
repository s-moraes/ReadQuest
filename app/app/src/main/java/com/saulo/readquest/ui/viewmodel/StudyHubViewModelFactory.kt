package com.saulo.readquest.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.saulo.readquest.data.local.FileContentManager
import com.saulo.readquest.data.progress.ProgressRepository
import com.saulo.readquest.data.remote.ContentSyncRepository
import com.saulo.readquest.data.repository.StudyContentRepository
import com.saulo.readquest.data.settings.SettingsRepository
import java.io.File

class StudyHubViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StudyHubViewModel::class.java)) {
            val appContext = context.applicationContext
            val contentDir = File(appContext.filesDir, "content")
            val contentManager = FileContentManager(contentDir)
            val contentRepository = StudyContentRepository(contentManager)
            val settingsRepository = SettingsRepository(appContext.filesDir)
            val progressRepository = ProgressRepository(appContext.filesDir)
            val contentSyncRepository = ContentSyncRepository(contentDir)

            return StudyHubViewModel(
                settingsRepository = settingsRepository,
                progressRepository = progressRepository,
                contentRepository = contentRepository,
                contentSyncRepository = contentSyncRepository
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
