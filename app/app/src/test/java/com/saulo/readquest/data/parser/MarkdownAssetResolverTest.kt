package com.saulo.readquest.data.parser

import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.File

class MarkdownAssetResolverTest {

    @Test
    fun resolve_replacesRelativeAssetPathsWithAbsoluteFileUris() {
        val subjectDir = File("/test/subject/dir")
        val raw = """
            # Architecture
            Here is the diagram:
            ![URL Shortener Architecture](../assets/url-shortener.webp)
            And another one:
            ![Scalability](assets/scalability-diagram.webp)
        """.trimIndent()

        val resolved = MarkdownAssetResolver.resolve(raw, subjectDir)

        val expected = """
            # Architecture
            Here is the diagram:
            ![URL Shortener Architecture](file:///test/subject/dir/assets/url-shortener.webp)
            And another one:
            ![Scalability](file:///test/subject/dir/assets/scalability-diagram.webp)
        """.trimIndent()

        assertEquals(expected, resolved)
    }

    @Test
    fun resolve_doesNotAlterOtherMarkdown() {
        val subjectDir = File("/test/subject/dir")
        val raw = "# Title\n\nNo images here."
        val resolved = MarkdownAssetResolver.resolve(raw, subjectDir)
        assertEquals(raw, resolved)
    }
}
