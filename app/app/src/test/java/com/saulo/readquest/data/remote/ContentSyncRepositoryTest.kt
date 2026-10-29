package com.saulo.readquest.data.remote

import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File
import java.io.IOException

class ContentSyncRepositoryTest {

    private lateinit var tempDir: File

    private val sampleCatalogJson = """
    {
      "version": 1,
      "subjects": [
        {
          "id": "kotlin-basics",
          "title": "Kotlin Basics",
          "description": "Learn Kotlin from scratch",
          "version": 1,
          "path": "content/kotlin-basics/subject.json"
        }
      ]
    }
    """.trimIndent()

    private val sampleSubjectManifestJson = """
    {
      "id": "kotlin-basics",
      "title": "Kotlin Basics",
      "version": 1,
      "assets": ["assets/diagram.webp"],
      "path": [
        {
          "id": "path-1",
          "title": "Path 1",
          "description": "First path",
          "icon": "code",
          "topics": [
            {
              "id": "topic-1",
              "title": "Variables",
              "file": "learn/01-variables.md"
            }
          ]
        }
      ],
      "extras": [
        {
          "id": "extra-1",
          "title": "Extra 1",
          "summary": "Extra summary",
          "type": "USE_CASE",
          "tags": ["Kotlin"],
          "tier": "FREE",
          "unlockAfterPathId": null,
          "externalUrl": null,
          "file": "extra/01-extra.md"
        }
      ],
      "quizzes": [
        {
          "id": "quiz-1",
          "title": "Quiz 1",
          "relatedPathId": "path-1",
          "questionCount": 1,
          "timeLimitMinutes": 2,
          "passPercentage": 70,
          "file": "quiz/01-quiz.md"
        }
      ]
    }
    """.trimIndent()

    @Before
    fun setUp() {
        tempDir = File(System.getProperty("java.io.tmpdir"), "content_sync_test_${System.currentTimeMillis()}")
        tempDir.mkdirs()
    }

    @After
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun syncContent_successfullyDownloadsAllFiles() = runBlocking {
        val fakeFetcher = object : RemoteContentFetcher {
            override suspend fun fetchText(url: String): Result<String> {
                return when {
                    url.endsWith("catalog.json") -> Result.success(sampleCatalogJson)
                    url.endsWith("subject.json") -> Result.success(sampleSubjectManifestJson)
                    url.endsWith("01-variables.md") -> Result.success("# Variables in Kotlin")
                    url.endsWith("01-extra.md") -> Result.success("# Extra Topic")
                    url.endsWith("01-quiz.md") -> Result.success("## Q1\nQuestion\n### Option A [CORRECT]\n### Option B\n### Option C\n### Option D\n### Explanation\nDone")
                    else -> Result.failure(IOException("Not found: $url"))
                }
            }

            override suspend fun fetchBytes(url: String): Result<ByteArray> {
                return if (url.endsWith("diagram.webp")) {
                    Result.success(byteArrayOf(1, 2, 3, 4))
                } else {
                    Result.failure(IOException("Asset not found"))
                }
            }
        }

        val syncRepo = ContentSyncRepository(
            contentDir = tempDir,
            fetcher = fakeFetcher
        )

        val progressEvents = mutableListOf<String>()
        val result = syncRepo.syncContent(
            baseUrl = "https://example.com/repo/main/",
            onProgress = { progressEvents.add(it) }
        )

        assertTrue(result.isSuccess)
        val summary = result.getOrThrow()
        assertEquals(1, summary.subjectsCount)
        assertEquals(6, summary.filesCount) // catalog + subject.json + topic + extra + quiz + asset

        // Verify files on disk
        val catalogFile = File(tempDir, "catalog.json")
        assertTrue(catalogFile.exists())

        val subjectManifest = File(tempDir, "kotlin-basics/subject.json")
        assertTrue(subjectManifest.exists())

        val topicFile = File(tempDir, "kotlin-basics/learn/01-variables.md")
        assertTrue(topicFile.exists())
        assertEquals("# Variables in Kotlin", topicFile.readText())

        val extraFile = File(tempDir, "kotlin-basics/extra/01-extra.md")
        assertTrue(extraFile.exists())

        val quizFile = File(tempDir, "kotlin-basics/quiz/01-quiz.md")
        assertTrue(quizFile.exists())

        val assetFile = File(tempDir, "kotlin-basics/assets/diagram.webp")
        assertTrue(assetFile.exists())
        assertEquals(4, assetFile.readBytes().size)

        assertFalse(progressEvents.isEmpty())
    }

    @Test
    fun syncContent_handlesNetworkFailureGracefully() = runBlocking {
        val failingFetcher = object : RemoteContentFetcher {
            override suspend fun fetchText(url: String): Result<String> {
                return Result.failure(IOException("Connection timed out"))
            }

            override suspend fun fetchBytes(url: String): Result<ByteArray> {
                return Result.failure(IOException("Connection timed out"))
            }
        }

        val syncRepo = ContentSyncRepository(
            contentDir = tempDir,
            fetcher = failingFetcher
        )

        val result = syncRepo.syncContent("https://failing.example.com")
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("Connection timed out") == true)
    }

    @Test
    fun normalizeBaseUrl_convertsGitHubTreeUrlsToRaw() {
        assertEquals(
            "https://raw.githubusercontent.com/s-moraes/ReadQuest/main/",
            ContentSyncRepository.normalizeBaseUrl("https://github.com/s-moraes/ReadQuest/tree/main/")
        )
        assertEquals(
            "https://raw.githubusercontent.com/s-moraes/ReadQuest/main/",
            ContentSyncRepository.normalizeBaseUrl("https://github.com/s-moraes/ReadQuest/tree/main")
        )
        assertEquals(
            "https://raw.githubusercontent.com/s-moraes/ReadQuest/feature-test/",
            ContentSyncRepository.normalizeBaseUrl("https://github.com/s-moraes/ReadQuest/tree/feature-test")
        )
        assertEquals(
            "https://raw.githubusercontent.com/s-moraes/ReadQuest/main/",
            ContentSyncRepository.normalizeBaseUrl("https://github.com/s-moraes/ReadQuest")
        )
        assertEquals(
            "https://raw.githubusercontent.com/s-moraes/ReadQuest/main/",
            ContentSyncRepository.normalizeBaseUrl("https://raw.githubusercontent.com/s-moraes/ReadQuest/main/")
        )
    }

    @Test
    fun syncContent_usesNormalizedUrlForGitHubTreeInput() = runBlocking {
        val requestedUrls = mutableListOf<String>()
        val capturingFetcher = object : RemoteContentFetcher {
            override suspend fun fetchText(url: String): Result<String> {
                requestedUrls.add(url)
                return when {
                    url.endsWith("catalog.json") -> Result.success(sampleCatalogJson)
                    url.endsWith("subject.json") -> Result.success(sampleSubjectManifestJson)
                    url.endsWith("01-variables.md") -> Result.success("# Variables")
                    url.endsWith("01-extra.md") -> Result.success("# Extra")
                    url.endsWith("01-quiz.md") -> Result.success("## Q1\nQ\n### Option A [CORRECT]\n### Option B\n### Option C\n### Option D\n### Explanation\nE")
                    else -> Result.failure(IOException("Not found"))
                }
            }

            override suspend fun fetchBytes(url: String): Result<ByteArray> {
                requestedUrls.add(url)
                return Result.success(byteArrayOf(1))
            }
        }

        val syncRepo = ContentSyncRepository(tempDir, capturingFetcher)
        val result = syncRepo.syncContent(ContentSyncRepository.DEFAULT_BASE_URL)

        assertTrue(result.isSuccess)
        assertTrue(requestedUrls.isNotEmpty())
        assertTrue(requestedUrls.all { it.startsWith("https://raw.githubusercontent.com/s-moraes/ReadQuest/main/") })
    }

    @Test
    fun syncContent_rootRepoUrl_fallsBackFromMainToMasterOn404() = runBlocking {
        val requestedUrls = mutableListOf<String>()
        val fallbackFetcher = object : RemoteContentFetcher {
            override suspend fun fetchText(url: String): Result<String> {
                requestedUrls.add(url)
                return when {
                    url == "https://raw.githubusercontent.com/my-org/my-repo/main/content/catalog.json" ->
                        Result.failure(HttpException(404, "HTTP 404 while fetching $url"))
                    url == "https://raw.githubusercontent.com/my-org/my-repo/master/content/catalog.json" ->
                        Result.success(sampleCatalogJson)
                    url == "https://raw.githubusercontent.com/my-org/my-repo/master/content/kotlin-basics/subject.json" ->
                        Result.success(sampleSubjectManifestJson)
                    url.endsWith("01-variables.md") -> Result.success("# Variables")
                    url.endsWith("01-extra.md") -> Result.success("# Extra")
                    url.endsWith("01-quiz.md") -> Result.success("## Q1\nQ\n### Option A [CORRECT]\n### Option B\n### Option C\n### Option D\n### Explanation\nE")
                    else -> Result.failure(IOException("Not found: $url"))
                }
            }

            override suspend fun fetchBytes(url: String): Result<ByteArray> {
                requestedUrls.add(url)
                return Result.success(byteArrayOf(1, 2))
            }
        }

        val syncRepo = ContentSyncRepository(tempDir, fallbackFetcher)
        val result = syncRepo.syncContent("https://github.com/my-org/my-repo")

        assertTrue(result.isSuccess)
        assertEquals("https://raw.githubusercontent.com/my-org/my-repo/master/", syncRepo.cachedResolvedBaseUrl)

        // Verify catalog retry happened
        assertTrue(requestedUrls.contains("https://raw.githubusercontent.com/my-org/my-repo/main/content/catalog.json"))
        assertTrue(requestedUrls.contains("https://raw.githubusercontent.com/my-org/my-repo/master/content/catalog.json"))

        // Verify subsequent downloads used master branch
        val subsequentUrls = requestedUrls.drop(2)
        assertTrue(subsequentUrls.isNotEmpty())
        assertTrue(subsequentUrls.all { it.startsWith("https://raw.githubusercontent.com/my-org/my-repo/master/") })
    }

    @Test
    fun syncContent_rootRepoUrl_usesMainWhenAvailableWithoutMasterFallback() = runBlocking {
        val requestedUrls = mutableListOf<String>()
        val mainFetcher = object : RemoteContentFetcher {
            override suspend fun fetchText(url: String): Result<String> {
                requestedUrls.add(url)
                return when {
                    url.endsWith("catalog.json") -> Result.success(sampleCatalogJson)
                    url.endsWith("subject.json") -> Result.success(sampleSubjectManifestJson)
                    url.endsWith("01-variables.md") -> Result.success("# Variables")
                    url.endsWith("01-extra.md") -> Result.success("# Extra")
                    url.endsWith("01-quiz.md") -> Result.success("## Q1\nQ\n### Option A [CORRECT]\n### Option B\n### Option C\n### Option D\n### Explanation\nE")
                    else -> Result.failure(IOException("Not found: $url"))
                }
            }

            override suspend fun fetchBytes(url: String): Result<ByteArray> {
                requestedUrls.add(url)
                return Result.success(byteArrayOf(1))
            }
        }

        val syncRepo = ContentSyncRepository(tempDir, mainFetcher)
        val result = syncRepo.syncContent("https://github.com/my-org/my-repo")

        assertTrue(result.isSuccess)
        assertEquals("https://raw.githubusercontent.com/my-org/my-repo/main/", syncRepo.cachedResolvedBaseUrl)
        assertFalse(requestedUrls.any { it.contains("/master/") })
    }

    @Test
    fun syncContent_non404ErrorDoesNotTriggerFallback() = runBlocking {
        val requestedUrls = mutableListOf<String>()
        val errorFetcher = object : RemoteContentFetcher {
            override suspend fun fetchText(url: String): Result<String> {
                requestedUrls.add(url)
                return Result.failure(HttpException(500, "HTTP 500 Internal Server Error"))
            }

            override suspend fun fetchBytes(url: String) = Result.success(byteArrayOf())
        }

        val syncRepo = ContentSyncRepository(tempDir, errorFetcher)
        val result = syncRepo.syncContent("https://github.com/my-org/my-repo")

        assertTrue(result.isFailure)
        assertEquals(1, requestedUrls.size)
        assertEquals("https://raw.githubusercontent.com/my-org/my-repo/main/content/catalog.json", requestedUrls.first())
    }

    @Test
    fun syncContent_bothBranchesFailWith404_returnsFailure() = runBlocking {
        val requestedUrls = mutableListOf<String>()
        val notFoundFetcher = object : RemoteContentFetcher {
            override suspend fun fetchText(url: String): Result<String> {
                requestedUrls.add(url)
                return Result.failure(HttpException(404, "HTTP 404 Not Found"))
            }

            override suspend fun fetchBytes(url: String) = Result.success(byteArrayOf())
        }

        val syncRepo = ContentSyncRepository(tempDir, notFoundFetcher)
        val result = syncRepo.syncContent("https://github.com/my-org/my-repo")

        assertTrue(result.isFailure)
        assertEquals(2, requestedUrls.size)
        assertEquals("https://raw.githubusercontent.com/my-org/my-repo/main/content/catalog.json", requestedUrls[0])
        assertEquals("https://raw.githubusercontent.com/my-org/my-repo/master/content/catalog.json", requestedUrls[1])
    }
}
