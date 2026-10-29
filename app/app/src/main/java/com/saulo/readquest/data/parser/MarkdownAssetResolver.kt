package com.saulo.readquest.data.parser

import java.io.File

object MarkdownAssetResolver {

    private val assetRegex = Regex("""!\[(.*?)\]\((?:\.\./|\./)?assets/(.*?)\)""")

    fun resolve(rawMarkdown: String, subjectDir: File): String {
        val assetsDir = File(subjectDir, "assets")
        return assetRegex.replace(rawMarkdown) { matchResult ->
            val altText = matchResult.groupValues[1]
            val fileName = matchResult.groupValues[2]
            val resolvedFile = File(assetsDir, fileName)
            "![$altText](file://${resolvedFile.absolutePath})"
        }
    }
}
