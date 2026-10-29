package com.saulo.readquest.ui.viewmodel

import com.saulo.readquest.data.local.FileContentManager
import com.saulo.readquest.data.progress.ProgressRepository
import com.saulo.readquest.data.repository.StudyContentRepository
import com.saulo.readquest.data.settings.SettingsRepository
import com.saulo.readquest.ui.navigation.AppRoute
import com.saulo.readquest.ui.navigation.NavigationTab
import com.saulo.readquest.ui.theme.ThemeMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
class StudyHubViewModelTest {

    private lateinit var tempFilesDir: File
    private lateinit var contentDir: File
    private lateinit var settingsRepo: SettingsRepository
    private lateinit var progressRepo: ProgressRepository
    private lateinit var contentRepo: StudyContentRepository
    private lateinit var viewModel: StudyHubViewModel

    @Before
    fun setUp() {
        tempFilesDir = File(System.getProperty("java.io.tmpdir"), "readquest_vm_test_${java.util.UUID.randomUUID()}")
        tempFilesDir.mkdirs()
        contentDir = File(tempFilesDir, "content").apply { mkdirs() }

        settingsRepo = SettingsRepository(tempFilesDir)
        progressRepo = ProgressRepository(tempFilesDir)
        val fileManager = FileContentManager(contentDir)
        contentRepo = StudyContentRepository(fileManager)

        viewModel = StudyHubViewModel(
            settingsRepository = settingsRepo,
            progressRepository = progressRepo,
            contentRepository = contentRepo,
            customScope = CoroutineScope(Dispatchers.Unconfined),
            sharingStarted = SharingStarted.Eagerly
        )
    }

    @After
    fun tearDown() {
        tempFilesDir.deleteRecursively()
    }

    @Test
    fun testInitialState() {
        val state = viewModel.uiState.value
        assertEquals(NavigationTab.LEARN, state.selectedTab)
        assertEquals(AppRoute.HomeTabs, state.currentRoute)
        assertEquals("", state.activeSubjectId)
        org.junit.Assert.assertNull(state.activeSubject)
        org.junit.Assert.assertTrue(state.learningPaths.isEmpty())
        org.junit.Assert.assertTrue(state.topics.isEmpty())
        org.junit.Assert.assertTrue(state.availableSubjects.isEmpty())
        assertEquals(ThemeMode.SYSTEM, state.themeMode)
        assertEquals(1.0f, state.fontScale, 0.001f)
    }

    @Test
    fun testNavigationAndTabSelection() {
        viewModel.onTabSelected(NavigationTab.QUIZ)
        assertEquals(NavigationTab.QUIZ, viewModel.uiState.value.selectedTab)

        viewModel.onNavigate(AppRoute.Settings)
        assertEquals(AppRoute.Settings, viewModel.uiState.value.currentRoute)
    }

    @Test
    fun testOnNavigateBack_fromSettings_returnsToHomeTabs() {
        viewModel.onNavigate(AppRoute.Settings)
        assertEquals(AppRoute.Settings, viewModel.uiState.value.currentRoute)

        viewModel.onNavigateBack()
        assertEquals(AppRoute.HomeTabs, viewModel.uiState.value.currentRoute)
    }

    @Test
    fun testOnNavigateBack_fromPathTopics_returnsToHomeLearnTab() {
        viewModel.onTabSelected(NavigationTab.QUIZ)
        viewModel.onNavigate(AppRoute.PathTopics("path-test-1"))

        viewModel.onNavigateBack()
        assertEquals(AppRoute.HomeTabs, viewModel.uiState.value.currentRoute)
        assertEquals(NavigationTab.LEARN, viewModel.uiState.value.selectedTab)
    }

    @Test
    fun testOnNavigateBack_fromContentReaderTopic_returnsToHomeLearnTab() {
        viewModel.onTabSelected(NavigationTab.QUIZ)
        viewModel.onNavigate(AppRoute.ContentReader("topic-1", isExtra = false))

        viewModel.onNavigateBack()
        assertEquals(AppRoute.HomeTabs, viewModel.uiState.value.currentRoute)
        assertEquals(NavigationTab.LEARN, viewModel.uiState.value.selectedTab)
    }

    @Test
    fun testOnNavigateBack_fromContentReaderExtra_returnsToHomeExtraTab() {
        viewModel.onTabSelected(NavigationTab.LEARN)
        viewModel.onNavigate(AppRoute.ContentReader("extra-1", isExtra = true))

        viewModel.onNavigateBack()
        assertEquals(AppRoute.HomeTabs, viewModel.uiState.value.currentRoute)
        assertEquals(NavigationTab.EXTRA, viewModel.uiState.value.selectedTab)
    }

    @Test
    fun testOnNavigateBack_fromQuizRunner_returnsToHomeQuizTab() {
        viewModel.onTabSelected(NavigationTab.LEARN)
        viewModel.onNavigate(AppRoute.QuizRunner("quiz-1"))

        viewModel.onNavigateBack()
        assertEquals(AppRoute.HomeTabs, viewModel.uiState.value.currentRoute)
        assertEquals(NavigationTab.QUIZ, viewModel.uiState.value.selectedTab)
    }

    @Test
    fun testOnNavigateBack_fromQuizResultReview_returnsToHomeQuizTab() {
        viewModel.onTabSelected(NavigationTab.LEARN)
        viewModel.onNavigate(AppRoute.QuizResultReview("quiz-1", mapOf(0 to 1)))

        viewModel.onNavigateBack()
        assertEquals(AppRoute.HomeTabs, viewModel.uiState.value.currentRoute)
        assertEquals(NavigationTab.QUIZ, viewModel.uiState.value.selectedTab)
    }

    @Test
    fun testThemeModeAndFontScaleChange() = runBlocking {
        viewModel.onThemeModeChanged(ThemeMode.DARK).join()
        val darkState = viewModel.uiState.first { it.themeMode == ThemeMode.DARK }
        assertEquals(ThemeMode.DARK, darkState.themeMode)

        viewModel.onFontScaleChanged(1.30f).join()
        val scaleState = viewModel.uiState.first { it.fontScale == 1.30f }
        assertEquals(1.30f, scaleState.fontScale, 0.001f)
    }

    @Test
    fun testCustomThemeModeAndColorUpdates() = runBlocking {
        // 1. Switch to Custom mode
        viewModel.onThemeModeChanged(ThemeMode.CUSTOM).join()
        val customState = viewModel.uiState.first { it.themeMode == ThemeMode.CUSTOM }
        assertEquals(ThemeMode.CUSTOM, customState.themeMode)

        // 2. Set Sepia preset
        viewModel.onUpdateCustomColors("#F4ECD8", "#2C2416", "SEPIA").join()
        val sepiaState = viewModel.uiState.first { it.customThemePreset == "SEPIA" }
        assertEquals("#F4ECD8", sepiaState.customBackgroundHex)
        assertEquals("#2C2416", sepiaState.customTextHex)
        assertEquals("SEPIA", sepiaState.customThemePreset)

        // 3. Set Night Owl preset
        viewModel.onUpdateCustomColors("#1E1D1B", "#E8DFD1", "NIGHT_OWL").join()
        val nightOwlState = viewModel.uiState.first { it.customThemePreset == "NIGHT_OWL" }
        assertEquals("#1E1D1B", nightOwlState.customBackgroundHex)
        assertEquals("#E8DFD1", nightOwlState.customTextHex)
        assertEquals("NIGHT_OWL", nightOwlState.customThemePreset)

        // 4. Set custom swatch
        viewModel.onUpdateCustomColors("#E8F0E6", "#233827", "CUSTOM").join()
        val swatchState = viewModel.uiState.first { it.customBackgroundHex == "#E8F0E6" }
        assertEquals("#E8F0E6", swatchState.customBackgroundHex)
        assertEquals("#233827", swatchState.customTextHex)
        assertEquals("CUSTOM", swatchState.customThemePreset)
        assertEquals("#E8F0E6", settingsRepo.settingsFlow.value.customBackgroundHex)
    }

    @Test
    fun testSubjectSwitchingAndProgressReset() = runBlocking {
        viewModel.onSelectSubject("java-foundations").join()
        val state = viewModel.uiState.first { it.activeSubjectId == "java-foundations" }
        assertEquals("java-foundations", state.activeSubjectId)

        viewModel.onTopicCompleted("java-foundations", "topic-basics").join()
        val progress = progressRepo.getSubjectProgress("java-foundations")
        assertEquals(setOf("topic-basics"), progress.completedTopicIds)

        viewModel.onResetSubjectProgress("java-foundations").join()
        val resetProgress = progressRepo.getSubjectProgress("java-foundations")
        assertEquals(emptySet<String>(), resetProgress.completedTopicIds)
    }

    @Test
    fun testSyncFromWebPopulatesSubjects() = runBlocking {
        val fakeFetcher = object : com.saulo.readquest.data.remote.RemoteContentFetcher {
            override suspend fun fetchText(url: String): Result<String> {
                return when {
                    url.endsWith("catalog.json") -> Result.success("""{"version":1,"subjects":[{"id":"sys-des","title":"SysDes","description":"desc","version":1,"path":"content/sys-des/subject.json"}]}""")
                    url.endsWith("subject.json") -> Result.success("""{"id":"sys-des","title":"SysDes","version":1,"assets":[],"path":[{"id":"p1","title":"Path 1","description":"Desc","icon":"code","topics":[{"id":"t1","title":"T1","file":"learn/01.md"}]}],"extras":[],"quizzes":[]}""")
                    url.endsWith("01.md") -> Result.success("# Topic 1")
                    else -> Result.failure(java.io.IOException("Not found"))
                }
            }
            override suspend fun fetchBytes(url: String) = Result.success(ByteArray(0))
        }

        val syncRepo = com.saulo.readquest.data.remote.ContentSyncRepository(contentDir, fakeFetcher)
        val vmWithSync = StudyHubViewModel(
            settingsRepository = settingsRepo,
            progressRepository = progressRepo,
            contentRepository = contentRepo,
            contentSyncRepository = syncRepo,
            customScope = CoroutineScope(Dispatchers.Unconfined)
        )

        vmWithSync.syncFromWeb().join()
        val syncedState = vmWithSync.uiState.first { it.activeSubject != null }
        assertEquals("sys-des", syncedState.activeSubjectId)
        assertEquals("SysDes", syncedState.activeSubject?.title)
        assertEquals(1, syncedState.learningPaths.size)
        assertEquals("Path 1", syncedState.learningPaths.first().title)
        assertEquals(1, syncedState.topics.size)
        assertEquals("T1", syncedState.topics.first().title)
        assertEquals(1, syncedState.topics.first().topicIndex)
        assertTrue(syncedState.topics.first().isCurrentResume)
        assertTrue(syncedState.lastSyncTimestamp > 0L)
    }

    @Test
    fun testCustomRepoUrlPreservesHumanUrlInSettingsAndSyncUsesIt() = runBlocking {
        val requestedUrls = mutableListOf<String>()
        val fakeFetcher = object : com.saulo.readquest.data.remote.RemoteContentFetcher {
            override suspend fun fetchText(url: String): Result<String> {
                requestedUrls.add(url)
                return when {
                    url.endsWith("catalog.json") -> Result.success("""{"version":1,"subjects":[{"id":"sys-des","title":"SysDes","description":"desc","version":1,"path":"content/sys-des/subject.json"}]}""")
                    url.endsWith("subject.json") -> Result.success("""{"id":"sys-des","title":"SysDes","version":1,"assets":[],"path":[],"extras":[],"quizzes":[]}""")
                    else -> Result.failure(java.io.IOException("Not found"))
                }
            }
            override suspend fun fetchBytes(url: String) = Result.success(ByteArray(0))
        }

        val syncRepo = com.saulo.readquest.data.remote.ContentSyncRepository(contentDir, fakeFetcher)
        val vm = StudyHubViewModel(
            settingsRepository = settingsRepo,
            progressRepository = progressRepo,
            contentRepository = contentRepo,
            contentSyncRepository = syncRepo,
            customScope = CoroutineScope(Dispatchers.Unconfined),
            sharingStarted = SharingStarted.Eagerly
        )

        val humanUrl = "https://github.com/my-org/custom-repo"
        vm.onCustomRepoUrlChanged(humanUrl).join()

        // 1. Verify exact human-readable URL stored in settings
        val settingsState = vm.uiState.first { it.settings.customRepoUrl == humanUrl }
        assertEquals(humanUrl, settingsState.settings.customRepoUrl)

        // 2. Perform sync and verify resolution happens at network time without modifying settings
        vm.syncFromWeb().join()

        val postSyncState = vm.uiState.value
        assertEquals(humanUrl, postSyncState.settings.customRepoUrl)
        assertTrue(requestedUrls.isNotEmpty())
        assertTrue(requestedUrls.all { it.startsWith("https://raw.githubusercontent.com/my-org/custom-repo/main/") })
    }

    @Test
    fun testTopicsSequentialIndexingAndProgressFlags() = runBlocking {
        val multiPathManifestJson = """
        {
          "id": "subject-multi",
          "title": "Multi Path Subject",
          "version": 1,
          "assets": [],
          "path": [
            {
              "id": "path-a",
              "title": "Path A",
              "description": "First path",
              "icon": "code",
              "topics": [
                {"id": "t-1", "title": "Topic 1", "file": "learn/01.md"},
                {"id": "t-2", "title": "Topic 2", "file": "learn/02.md"}
              ]
            },
            {
              "id": "path-b",
              "title": "Path B",
              "description": "Second path",
              "icon": "terminal",
              "topics": [
                {"id": "t-3", "title": "Topic 3", "file": "learn/03.md"}
              ]
            }
          ],
          "extras": [],
          "quizzes": []
        }
        """.trimIndent()

        val fakeFetcher = object : com.saulo.readquest.data.remote.RemoteContentFetcher {
            override suspend fun fetchText(url: String): Result<String> {
                return when {
                    url.endsWith("catalog.json") -> Result.success("""{"version":1,"subjects":[{"id":"subject-multi","title":"Multi Path Subject","description":"desc","version":1,"path":"content/subject-multi/subject.json"}]}""")
                    url.endsWith("subject.json") -> Result.success(multiPathManifestJson)
                    url.endsWith("01.md") -> Result.success("# Topic 1")
                    url.endsWith("02.md") -> Result.success("# Topic 2")
                    url.endsWith("03.md") -> Result.success("# Topic 3")
                    else -> Result.failure(java.io.IOException("Not found"))
                }
            }
            override suspend fun fetchBytes(url: String) = Result.success(ByteArray(0))
        }

        val syncRepo = com.saulo.readquest.data.remote.ContentSyncRepository(contentDir, fakeFetcher)
        val vm = StudyHubViewModel(
            settingsRepository = settingsRepo,
            progressRepository = progressRepo,
            contentRepository = contentRepo,
            contentSyncRepository = syncRepo,
            customScope = CoroutineScope(Dispatchers.Unconfined),
            sharingStarted = SharingStarted.Eagerly
        )

        vm.syncFromWeb().join()
        val syncedState = vm.uiState.first { it.activeSubject != null }
        assertEquals(3, syncedState.topics.size)

        // Verify sequential 1..N indexing across multiple paths
        assertEquals(1, syncedState.topics[0].topicIndex)
        assertEquals("Topic 1", syncedState.topics[0].title)
        assertTrue(syncedState.topics[0].isCurrentResume)
        assertFalse(syncedState.topics[0].isCompleted)

        assertEquals(2, syncedState.topics[1].topicIndex)
        assertEquals("Topic 2", syncedState.topics[1].title)
        assertFalse(syncedState.topics[1].isCurrentResume)

        assertEquals(3, syncedState.topics[2].topicIndex)
        assertEquals("Topic 3", syncedState.topics[2].title)

        // Complete topic 1
        vm.onTopicCompleted("subject-multi", "t-1", "t-2").join()
        val updatedState = vm.uiState.first { it.topics[0].isCompleted }
        assertTrue(updatedState.topics[0].isCompleted)
        assertTrue(updatedState.topics[1].isCurrentResume)
    }

    @Test
    fun testResumeTopicAccessedState() = runBlocking {
        val testManifestJson = """
        {
          "id": "subject-access",
          "title": "Access Test",
          "version": 1,
          "assets": [],
          "path": [
            {
              "id": "p-1",
              "title": "Path 1",
              "description": "Path 1 Desc",
              "icon": "code",
              "topics": [
                {"id": "t-1", "title": "Topic 1", "file": "learn/01.md"},
                {"id": "t-2", "title": "Topic 2", "file": "learn/02.md"}
              ]
            }
          ],
          "extras": [],
          "quizzes": []
        }
        """.trimIndent()

        val fakeFetcher = object : com.saulo.readquest.data.remote.RemoteContentFetcher {
            override suspend fun fetchText(url: String): Result<String> {
                return when {
                    url.endsWith("catalog.json") -> Result.success("""{"version":1,"subjects":[{"id":"subject-access","title":"Access Test","description":"desc","version":1,"path":"content/subject-access/subject.json"}]}""")
                    url.endsWith("subject.json") -> Result.success(testManifestJson)
                    url.endsWith("01.md") -> Result.success("# Topic 1")
                    url.endsWith("02.md") -> Result.success("# Topic 2")
                    else -> Result.failure(java.io.IOException("Not found"))
                }
            }
            override suspend fun fetchBytes(url: String) = Result.success(ByteArray(0))
        }

        val syncRepo = com.saulo.readquest.data.remote.ContentSyncRepository(contentDir, fakeFetcher)
        val vm = StudyHubViewModel(
            settingsRepository = settingsRepo,
            progressRepository = progressRepo,
            contentRepository = contentRepo,
            contentSyncRepository = syncRepo,
            customScope = CoroutineScope(Dispatchers.Unconfined),
            sharingStarted = SharingStarted.Eagerly
        )

        vm.syncFromWeb().join()
        val initialState = vm.uiState.first { it.activeSubject != null }
        assertEquals("t-1", initialState.activeSubject?.resumeTopicId)
        // Never accessed before -> isResumeTopicAccessed should be false ("READ NOW")
        assertFalse(initialState.activeSubject?.isResumeTopicAccessed == true)

        // User accesses topic 1
        vm.onTopicAccessed("subject-access", "t-1").join()
        val accessedState = vm.uiState.first { it.activeSubject?.isResumeTopicAccessed == true }
        assertTrue(accessedState.activeSubject?.isResumeTopicAccessed == true)

        // Complete topic 1, moving resume to topic 2
        vm.onTopicCompleted("subject-access", "t-1", "t-2").join()
        val nextTopicState = vm.uiState.first { it.activeSubject?.resumeTopicId == "t-2" }
        // Topic 2 has not been accessed yet -> isResumeTopicAccessed should be false
        assertFalse(nextTopicState.activeSubject?.isResumeTopicAccessed == true)

        // User accesses topic 2
        vm.onTopicAccessed("subject-access", "t-2").join()
        val nextAccessedState = vm.uiState.first { it.activeSubject?.resumeTopicId == "t-2" && it.activeSubject?.isResumeTopicAccessed == true }
        assertTrue(nextAccessedState.activeSubject?.isResumeTopicAccessed == true)
    }
}
