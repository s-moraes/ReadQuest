package com.saulo.readquest.data.remote

import org.junit.Assert.assertEquals
import org.junit.Test

class GitHubUrlResolverTest {

    @Test
    fun resolveRawBaseUrl_rootRepoUrl_returnsMainAndMasterCandidates() {
        val expected = listOf(
            "https://raw.githubusercontent.com/owner/repo/main/",
            "https://raw.githubusercontent.com/owner/repo/master/"
        )

        assertEquals(expected, GitHubUrlResolver.resolveRawBaseUrl("https://github.com/owner/repo"))
        assertEquals(expected, GitHubUrlResolver.resolveRawBaseUrl("http://github.com/owner/repo"))
        assertEquals(expected, GitHubUrlResolver.resolveRawBaseUrl("github.com/owner/repo"))
        assertEquals(expected, GitHubUrlResolver.resolveRawBaseUrl("https://www.github.com/owner/repo"))
    }

    @Test
    fun resolveRawBaseUrl_stripsWhitespaceTrailingSlashesAndGitSuffix() {
        val expected = listOf(
            "https://raw.githubusercontent.com/owner/repo/main/",
            "https://raw.githubusercontent.com/owner/repo/master/"
        )

        assertEquals(expected, GitHubUrlResolver.resolveRawBaseUrl("  https://github.com/owner/repo.git/  "))
        assertEquals(expected, GitHubUrlResolver.resolveRawBaseUrl("https://github.com/owner/repo.git"))
        assertEquals(expected, GitHubUrlResolver.resolveRawBaseUrl("https://github.com/owner/repo/"))
        assertEquals(expected, GitHubUrlResolver.resolveRawBaseUrl("https://github.com/owner/repo///"))
        assertEquals(expected, GitHubUrlResolver.resolveRawBaseUrl("github.com/owner/repo.git/"))
    }

    @Test
    fun resolveRawBaseUrl_treeAndBlobUrls_returnsExactRawBranchUrl() {
        assertEquals(
            listOf("https://raw.githubusercontent.com/owner/repo/main/"),
            GitHubUrlResolver.resolveRawBaseUrl("https://github.com/owner/repo/tree/main")
        )
        assertEquals(
            listOf("https://raw.githubusercontent.com/owner/repo/main/"),
            GitHubUrlResolver.resolveRawBaseUrl("https://github.com/owner/repo/tree/main/")
        )
        assertEquals(
            listOf("https://raw.githubusercontent.com/owner/repo/develop/"),
            GitHubUrlResolver.resolveRawBaseUrl("github.com/owner/repo/tree/develop")
        )
        assertEquals(
            listOf("https://raw.githubusercontent.com/owner/repo/release-v1/"),
            GitHubUrlResolver.resolveRawBaseUrl("https://github.com/owner/repo/blob/release-v1")
        )
    }

    @Test
    fun resolveRawBaseUrl_treeAndBlobUrls_withSubpath() {
        assertEquals(
            listOf("https://raw.githubusercontent.com/owner/repo/main/content/"),
            GitHubUrlResolver.resolveRawBaseUrl("https://github.com/owner/repo/tree/main/content")
        )
        assertEquals(
            listOf("https://raw.githubusercontent.com/owner/repo/main/content/"),
            GitHubUrlResolver.resolveRawBaseUrl("https://github.com/owner/repo/tree/main/content/")
        )
        assertEquals(
            listOf("https://raw.githubusercontent.com/owner/repo/feature/sub/dir/"),
            GitHubUrlResolver.resolveRawBaseUrl("https://github.com/owner/repo/blob/feature/sub/dir")
        )
    }

    @Test
    fun resolveRawBaseUrl_rawGitHubUrls_ensuresTrailingSlash() {
        assertEquals(
            listOf("https://raw.githubusercontent.com/owner/repo/main/"),
            GitHubUrlResolver.resolveRawBaseUrl("https://raw.githubusercontent.com/owner/repo/main")
        )
        assertEquals(
            listOf("https://raw.githubusercontent.com/owner/repo/main/"),
            GitHubUrlResolver.resolveRawBaseUrl("https://raw.githubusercontent.com/owner/repo/main/")
        )
        assertEquals(
            listOf("https://raw.githubusercontent.com/owner/repo/main/"),
            GitHubUrlResolver.resolveRawBaseUrl("raw.githubusercontent.com/owner/repo/main")
        )
    }

    @Test
    fun resolveRawBaseUrl_blankInputFallsBackToDefaultBaseUrl() {
        val expected = listOf(
            "https://raw.githubusercontent.com/s-moraes/ReadQuest/main/",
            "https://raw.githubusercontent.com/s-moraes/ReadQuest/master/"
        )

        assertEquals(expected, GitHubUrlResolver.resolveRawBaseUrl(""))
        assertEquals(expected, GitHubUrlResolver.resolveRawBaseUrl("   "))
    }

    @Test
    fun resolveRawBaseUrl_customNonGitHubUrl_fallsBackWithTrailingSlash() {
        assertEquals(
            listOf("https://example.com/subjects/main/"),
            GitHubUrlResolver.resolveRawBaseUrl("https://example.com/subjects/main")
        )
        assertEquals(
            listOf("https://example.com/subjects/main/"),
            GitHubUrlResolver.resolveRawBaseUrl("https://example.com/subjects/main/")
        )
    }
}
