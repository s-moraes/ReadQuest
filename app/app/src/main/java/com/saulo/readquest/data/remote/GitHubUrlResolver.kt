package com.saulo.readquest.data.remote

object GitHubUrlResolver {

    private val treeOrBlobRegex = Regex("""^(?:https?://)?(?:www\.)?github\.com/([^/]+)/([^/]+)/(?:tree|blob)/([^/]+)(?:/(.*))?$""")
    private val rootRepoRegex = Regex("""^(?:https?://)?(?:www\.)?github\.com/([^/]+)/([^/]+)$""")

    fun resolveRawBaseUrl(displayUrl: String): List<String> {
        // 1. Trim whitespace and trailing slashes (/) and strip any trailing .git suffix
        var trimmed = displayUrl.trim()
        while (trimmed.endsWith("/")) {
            trimmed = trimmed.dropLast(1)
        }
        if (trimmed.endsWith(".git")) {
            trimmed = trimmed.removeSuffix(".git")
            while (trimmed.endsWith("/")) {
                trimmed = trimmed.dropLast(1)
            }
        }

        if (trimmed.isBlank()) {
            return resolveRawBaseUrl(ContentSyncRepository.DEFAULT_BASE_URL)
        }

        // 4. If the URL already uses raw.githubusercontent.com
        if (trimmed.contains("raw.githubusercontent.com")) {
            val withScheme = ensureScheme(trimmed)
            return listOf("$withScheme/")
        }

        // 2. If the URL contains /tree/<branch> or /blob/<branch>
        val treeMatch = treeOrBlobRegex.find(trimmed)
        if (treeMatch != null) {
            val (owner, repo, branch, subPath) = treeMatch.destructured
            val formattedSubPath = if (subPath.isNotBlank()) "/${subPath.trim('/')}" else ""
            return listOf("https://raw.githubusercontent.com/$owner/$repo/$branch$formattedSubPath/")
        }

        // 3. If the URL is a root repository link (https://github.com/<owner>/<repo> or github.com/<owner>/<repo>)
        val repoMatch = rootRepoRegex.find(trimmed)
        if (repoMatch != null) {
            val (owner, repo) = repoMatch.destructured
            return listOf(
                "https://raw.githubusercontent.com/$owner/$repo/main/",
                "https://raw.githubusercontent.com/$owner/$repo/master/"
            )
        }

        // Fallback for non-GitHub URLs (e.g., custom host or mock servers in tests)
        val withScheme = ensureScheme(trimmed)
        return listOf("$withScheme/")
    }

    private fun ensureScheme(url: String): String {
        return if (!url.startsWith("http://") && !url.startsWith("https://")) {
            "https://$url"
        } else {
            url
        }
    }
}
