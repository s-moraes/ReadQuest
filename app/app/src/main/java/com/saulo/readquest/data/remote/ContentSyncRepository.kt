package com.saulo.readquest.data.remote

import com.saulo.readquest.data.model.CatalogDto
import com.saulo.readquest.data.model.SubjectManifestDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.File
import java.io.IOException

typealias GitHubContentSyncClient = ContentSyncRepository

data class SyncSummary(
    val subjectsCount: Int,
    val filesCount: Int
)

class ContentSyncRepository(
    val contentDir: File,
    private val fetcher: RemoteContentFetcher = DefaultRemoteContentFetcher(),
    private val json: Json = Json { ignoreUnknownKeys = true },
    private val urlResolver: GitHubUrlResolver = GitHubUrlResolver
) {
    companion object {
        const val DEFAULT_BASE_URL = "https://github.com/s-moraes/ReadQuest"

        fun normalizeBaseUrl(inputUrl: String): String {
            return GitHubUrlResolver.resolveRawBaseUrl(inputUrl).first()
        }
    }

    var cachedResolvedBaseUrl: String? = null
        private set

    suspend fun syncContent(
        baseUrl: String = DEFAULT_BASE_URL,
        onProgress: (String) -> Unit = {}
    ): Result<SyncSummary> = withContext(Dispatchers.IO) {
        runCatching {
            val candidateUrls = urlResolver.resolveRawBaseUrl(baseUrl)
            var totalFilesDownloaded = 0

            // 1. Fetch catalog.json with automatic branch fallback
            onProgress("Connecting to repository...")
            var catalogText: String? = null
            var winningBaseUrl: String? = null
            var lastException: Throwable? = null

            for (candidate in candidateUrls) {
                val catalogUrl = "${candidate}content/catalog.json"
                val catalogTextResult = fetcher.fetchText(catalogUrl)
                if (catalogTextResult.isSuccess) {
                    catalogText = catalogTextResult.getOrThrow()
                    winningBaseUrl = candidate
                    break
                } else {
                    val ex = catalogTextResult.exceptionOrNull() ?: IOException("Failed to fetch $catalogUrl")
                    lastException = ex
                    val is404 = (ex as? HttpException)?.statusCode == 404 ||
                            ex.message?.contains("404") == true
                    if (!is404) {
                        throw ex
                    }
                }
            }

            if (winningBaseUrl == null || catalogText == null) {
                throw lastException ?: IOException("Failed to fetch content/catalog.json from repository")
            }

            cachedResolvedBaseUrl = winningBaseUrl

            if (!contentDir.exists()) {
                contentDir.mkdirs()
            }
            val catalogFile = File(contentDir, "catalog.json")
            catalogFile.writeText(catalogText)
            totalFilesDownloaded++

            val catalogDto = json.decodeFromString<CatalogDto>(catalogText)
            val totalSubjects = catalogDto.subjects.size

            // 2. Fetch each subject using winningBaseUrl
            for ((index, subjectEntry) in catalogDto.subjects.withIndex()) {
                onProgress("Syncing subject (${index + 1}/$totalSubjects): ${subjectEntry.title}...")

                val subjectDir = File(contentDir, subjectEntry.id).apply { mkdirs() }

                // Fetch subject.json manifest
                val manifestUrl = "${winningBaseUrl}${subjectEntry.path}"
                val manifestText = fetcher.fetchText(manifestUrl).getOrThrow()
                File(subjectDir, "subject.json").writeText(manifestText)
                totalFilesDownloaded++

                val manifestDto = json.decodeFromString<SubjectManifestDto>(manifestText)

                // Fetch learning path topic markdown files
                for (path in manifestDto.paths) {
                    for (topic in path.topics) {
                        val topicUrl = "${winningBaseUrl}content/${subjectEntry.id}/${topic.file}"
                        val topicFile = File(subjectDir, topic.file).apply { parentFile?.mkdirs() }
                        val topicText = fetcher.fetchText(topicUrl).getOrThrow()
                        topicFile.writeText(topicText)
                        totalFilesDownloaded++
                    }
                }

                // Fetch extra markdown files
                for (extra in manifestDto.extras) {
                    if (!extra.file.isNullOrBlank()) {
                        val extraUrl = "${winningBaseUrl}content/${subjectEntry.id}/${extra.file}"
                        val extraFile = File(subjectDir, extra.file).apply { parentFile?.mkdirs() }
                        val extraText = fetcher.fetchText(extraUrl).getOrThrow()
                        extraFile.writeText(extraText)
                        totalFilesDownloaded++
                    }
                }

                // Fetch quiz markdown files
                for (quiz in manifestDto.quizzes) {
                    val quizUrl = "${winningBaseUrl}content/${subjectEntry.id}/${quiz.file}"
                    val quizFile = File(subjectDir, quiz.file).apply { parentFile?.mkdirs() }
                    val quizText = fetcher.fetchText(quizUrl).getOrThrow()
                    quizFile.writeText(quizText)
                    totalFilesDownloaded++
                }

                // Fetch image assets
                for (asset in manifestDto.assets) {
                    val assetUrl = "${winningBaseUrl}content/${subjectEntry.id}/$asset"
                    val assetFile = File(subjectDir, asset).apply { parentFile?.mkdirs() }
                    val assetBytesResult = fetcher.fetchBytes(assetUrl)
                    if (assetBytesResult.isSuccess) {
                        assetFile.writeBytes(assetBytesResult.getOrThrow())
                        totalFilesDownloaded++
                    }
                }
            }

            onProgress("Content sync complete.")
            SyncSummary(subjectsCount = totalSubjects, filesCount = totalFilesDownloaded)
        }
    }
}
