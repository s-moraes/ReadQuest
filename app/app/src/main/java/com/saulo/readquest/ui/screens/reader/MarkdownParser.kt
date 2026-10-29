package com.saulo.readquest.ui.screens.reader

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle

fun parseInlineMarkdown(
    text: String,
    primaryColor: Color = Color.Unspecified,
    codeBgColor: Color = Color.Unspecified,
    codeTextColor: Color = Color.Unspecified
): AnnotatedString {
    return buildAnnotatedString {
        val tokenRegex = Regex(
            "(`([^`]+)`)|" +
            "(\\*\\*(.*?)\\*\\*)|" +
            "(__([^_]+)__)|" +
            "(~~(.*?)~~)|" +
            "(\\*([^*]+)\\*)|" +
            "(_([^_]+)_)|" +
            "(\\[([^\\]]+)\\]\\(([^)]+)\\))"
        )

        var lastIndex = 0
        for (match in tokenRegex.findAll(text)) {
            val range = match.range
            if (range.first > lastIndex) {
                append(text.substring(lastIndex, range.first))
            }

            val value = match.value
            when {
                // 1. Inline code: `code`
                value.startsWith("`") -> {
                    val codeContent = match.groups[2]?.value ?: ""
                    withStyle(
                        SpanStyle(
                            fontFamily = FontFamily.Monospace,
                            background = codeBgColor,
                            color = codeTextColor
                        )
                    ) {
                        append(" $codeContent ")
                    }
                }
                // 2. Bold: **bold** or __bold__
                value.startsWith("**") || value.startsWith("__") -> {
                    val boldContent = if (value.startsWith("**")) {
                        match.groups[4]?.value ?: ""
                    } else {
                        match.groups[6]?.value ?: ""
                    }
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                        if (boldContent.contains("`")) {
                            val segments = boldContent.split("`")
                            segments.forEachIndexed { idx, segment ->
                                if (idx % 2 == 1) {
                                    withStyle(
                                        SpanStyle(
                                            fontFamily = FontFamily.Monospace,
                                            background = codeBgColor,
                                            color = codeTextColor
                                        )
                                    ) {
                                        append(" $segment ")
                                    }
                                } else if (segment.isNotEmpty()) {
                                    append(segment)
                                }
                            }
                        } else {
                            append(boldContent)
                        }
                    }
                }
                // 3. Strikethrough: ~~strikethrough~~
                value.startsWith("~~") -> {
                    val strikeContent = match.groups[8]?.value ?: ""
                    withStyle(SpanStyle(textDecoration = TextDecoration.LineThrough)) {
                        append(strikeContent)
                    }
                }
                // 4. Italic: *italic* or _italic_
                value.startsWith("*") || value.startsWith("_") -> {
                    val italicContent = if (value.startsWith("*")) {
                        match.groups[10]?.value ?: ""
                    } else {
                        match.groups[12]?.value ?: ""
                    }
                    withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                        append(italicContent)
                    }
                }
                // 5. Link: [text](url)
                value.startsWith("[") -> {
                    val linkText = match.groups[14]?.value ?: ""
                    val linkUrl = match.groups[15]?.value ?: ""
                    val linkStart = length
                    pushStringAnnotation(tag = "URL", annotation = linkUrl)
                    try {
                        val linkAnnotation = LinkAnnotation.Url(
                            url = linkUrl,
                            styles = TextLinkStyles(
                                style = SpanStyle(
                                    color = primaryColor,
                                    textDecoration = TextDecoration.Underline,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        )
                        addLink(linkAnnotation, linkStart, linkStart + linkText.length)
                    } catch (_: Throwable) {
                        // Fallback for environments where LinkAnnotation runtime is absent
                    }
                    withStyle(
                        SpanStyle(
                            color = primaryColor,
                            textDecoration = TextDecoration.Underline,
                            fontWeight = FontWeight.Medium
                        )
                    ) {
                        append(linkText)
                    }
                    pop()
                }
            }
            lastIndex = range.last + 1
        }

        if (lastIndex < text.length) {
            append(text.substring(lastIndex))
        }
    }
}

internal data class AlertInfo(val type: AlertType, val title: String, val inlineBody: String)

internal fun extractAlert(firstLine: String): AlertInfo? {
    val trimmed = firstLine.trim()
    val matchGfm = Regex("^\\[!(NOTE|TIP|IMPORTANT|WARNING|CAUTION)\\]\\s*(.*)", RegexOption.IGNORE_CASE).matchEntire(trimmed)
    if (matchGfm != null) {
        val type = AlertType.valueOf(matchGfm.groupValues[1].uppercase())
        val rest = matchGfm.groupValues[2].trim()
        return AlertInfo(type, type.name, rest)
    }

    val matchColon = Regex("^\\*\\*(Note|Tip|Important|Warning|Caution|Rule[\\w\\s]*):?\\*\\*\\s*(.*)", RegexOption.IGNORE_CASE).matchEntire(trimmed)
    if (matchColon != null) {
        val rawKeyword = matchColon.groupValues[1]
        val keyword = rawKeyword.uppercase()
        val type = when {
            keyword.startsWith("NOTE") || keyword.startsWith("RULE") -> AlertType.NOTE
            keyword.startsWith("TIP") -> AlertType.TIP
            keyword.startsWith("IMPORTANT") -> AlertType.IMPORTANT
            keyword.startsWith("WARNING") -> AlertType.WARNING
            keyword.startsWith("CAUTION") -> AlertType.CAUTION
            else -> AlertType.NOTE
        }
        val title = rawKeyword.uppercase()
        val rest = matchColon.groupValues[2].trim()
        return AlertInfo(type, title, rest)
    }

    return null
}

private fun isHorizontalRule(line: String): Boolean {
    if (line.length < 3) return false
    val first = line[0]
    if (first != '-' && first != '*' && first != '_') return false
    return line.all { it == first || it == ' ' } && line.count { it == first } >= 3
}

private fun isTableSeparator(line: String): Boolean {
    if (!line.contains("|") || !line.contains("-")) return false
    val cleaned = line.replace("|", "").replace(":", "").replace("-", "").replace(" ", "")
    return cleaned.isEmpty()
}

private fun parseTableRow(line: String): List<String> {
    var content = line
    if (content.startsWith("|")) content = content.substring(1)
    if (content.endsWith("|")) content = content.substring(0, content.length - 1)
    return content.split("|").map { it.trim() }
}

fun parseMarkdown(content: String): List<MarkdownNode> {
    val nodes = mutableListOf<MarkdownNode>()
    val lines = content.lines()
    var inCodeBlock = false
    val codeLines = mutableListOf<String>()
    var codeLang = ""
    var i = 0

    while (i < lines.size) {
        val line = lines[i]
        val trimmed = line.trim()

        if (trimmed.startsWith("```")) {
            if (inCodeBlock) {
                nodes.add(MarkdownNode.CodeBlock(codeLines.joinToString("\n"), codeLang))
                codeLines.clear()
                codeLang = ""
                inCodeBlock = false
            } else {
                inCodeBlock = true
                codeLang = trimmed.removePrefix("```").trim()
            }
            i++
            continue
        }

        if (inCodeBlock) {
            codeLines.add(line)
            i++
            continue
        }

        if (trimmed.isEmpty()) {
            i++
            continue
        }

        // 1. Horizontal Rule: ---, ***, ___
        if (isHorizontalRule(trimmed)) {
            nodes.add(MarkdownNode.HorizontalRule)
            i++
            continue
        }

        // 2. Headings: #, ##, ###
        if (trimmed.startsWith("#")) {
            val level = trimmed.takeWhile { it == '#' }.length
            if (level in 1..6 && trimmed.length > level && trimmed[level] == ' ') {
                nodes.add(MarkdownNode.Heading(level, trimmed.substring(level).trim()))
                i++
                continue
            }
        }

        // 3. GFM Table: line containing '|' followed by table separator
        if (trimmed.contains("|") && i + 1 < lines.size && isTableSeparator(lines[i + 1].trim())) {
            val headers = parseTableRow(trimmed)
            i += 2 // skip header and separator
            val rows = mutableListOf<List<String>>()
            while (i < lines.size && lines[i].trim().contains("|") && !lines[i].trim().startsWith("```")) {
                rows.add(parseTableRow(lines[i].trim()))
                i++
            }
            nodes.add(MarkdownNode.Table(headers, rows))
            continue
        }

        // 4. Blockquote & Alerts (> text, > [!NOTE], > **Rule:**)
        if (trimmed.startsWith(">")) {
            val quoteLines = mutableListOf<String>()
            while (i < lines.size && lines[i].trim().startsWith(">")) {
                val currentLineContent = lines[i].trim().removePrefix(">").trim()
                if (quoteLines.isNotEmpty() && extractAlert(currentLineContent) != null) {
                    break // Next line is a new alert, finish the current one
                }
                quoteLines.add(currentLineContent)
                i++
            }
            val firstLine = quoteLines.firstOrNull() ?: ""
            val alertInfo = extractAlert(firstLine)
            if (alertInfo != null) {
                val fullBody = if (quoteLines.size > 1) {
                    if (alertInfo.inlineBody.isNotBlank()) {
                        alertInfo.inlineBody + "\n" + quoteLines.drop(1).joinToString("\n")
                    } else {
                        quoteLines.drop(1).joinToString("\n")
                    }
                } else {
                    alertInfo.inlineBody
                }
                nodes.add(MarkdownNode.Alert(alertInfo.type, alertInfo.title, fullBody.trim()))
            } else {
                nodes.add(MarkdownNode.Blockquote(quoteLines.joinToString("\n")))
            }
            continue
        }

        // 5. GFM Task List items: - [ ] or - [x]
        val taskRegex = Regex("^[-*]\\s+\\[([ xX])\\]\\s+(.*)")
        val taskMatch = taskRegex.matchEntire(trimmed)
        if (taskMatch != null) {
            val isChecked = taskMatch.groupValues[1].equals("x", ignoreCase = true)
            val text = taskMatch.groupValues[2].trim()
            nodes.add(MarkdownNode.TaskItem(isChecked, text))
            i++
            continue
        }

        // 6. Regular Bullet items: - text, * text
        if (trimmed.startsWith("- ") || trimmed.startsWith("* ")) {
            nodes.add(MarkdownNode.BulletItem(trimmed.substring(2).trim()))
            i++
            continue
        }

        // 7. Numbered items: 1. text
        if (trimmed.matches(Regex("^\\d+\\.\\s+.*"))) {
            val dotIndex = trimmed.indexOf('.')
            val number = trimmed.substring(0, dotIndex)
            val text = trimmed.substring(dotIndex + 1).trim()
            nodes.add(MarkdownNode.NumberedItem(number, text))
            i++
            continue
        }

        // 8. Diagram / Image: ![caption](url)
        if (trimmed.startsWith("![") && trimmed.contains("](") && trimmed.endsWith(")")) {
            val caption = trimmed.substringAfter("![").substringBefore("](")
            val url = trimmed.substringAfter("](").removeSuffix(")")
            nodes.add(MarkdownNode.DiagramImage(caption, url))
            i++
            continue
        }

        // 9. Regular Paragraph
        nodes.add(MarkdownNode.Paragraph(trimmed))
        i++
    }

    if (inCodeBlock && codeLines.isNotEmpty()) {
        nodes.add(MarkdownNode.CodeBlock(codeLines.joinToString("\n"), codeLang))
    }

    return nodes
}
