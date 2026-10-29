package com.saulo.readquest.ui.screens.reader

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle

fun highlightCode(
    code: String,
    language: String,
    isDark: Boolean = true
): AnnotatedString {
    val lang = language.lowercase().trim()
    if (lang.isBlank() || lang == "text" || lang == "txt" || lang == "plain") {
        return AnnotatedString(code)
    }

    val keywordColor = if (isDark) Color(0xFF79B8FF) else Color(0xFF005CC5)
    val typeColor = if (isDark) Color(0xFFB392F0) else Color(0xFF6F42C1)
    val stringColor = if (isDark) Color(0xFF85E89D) else Color(0xFF22863A)
    val commentColor = if (isDark) Color(0xFF8B949E) else Color(0xFF6A737D)
    val numberColor = if (isDark) Color(0xFFFFAB70) else Color(0xFFD73A49)

    val isSql = lang == "sql"
    val isJavaKotlin = lang in listOf("kotlin", "kt", "java")
    val isBash = lang in listOf("bash", "sh", "shell", "cli", "zsh")
    val isJson = lang == "json"
    val isYaml = lang in listOf("yaml", "yml")

    val keywords = when {
        isSql -> setOf(
            "SELECT", "FROM", "WHERE", "JOIN", "LEFT", "RIGHT", "INNER", "OUTER", "FULL", "ON",
            "GROUP", "BY", "ORDER", "HAVING", "LIMIT", "OFFSET", "INSERT", "INTO", "VALUES",
            "UPDATE", "SET", "DELETE", "CREATE", "ALTER", "DROP", "TABLE", "INDEX", "VIEW",
            "TRIGGER", "PRIMARY", "KEY", "FOREIGN", "REFERENCES", "CONSTRAINT", "NOT", "NULL",
            "DEFAULT", "UNIQUE", "CHECK", "AND", "OR", "IN", "IS", "LIKE", "AS", "CASCADE",
            "BEGIN", "COMMIT", "ROLLBACK", "TRANSACTION", "CONCURRENTLY", "EXPLAIN", "ANALYZE"
        )
        isJavaKotlin -> setOf(
            "fun", "val", "var", "class", "interface", "object", "enum", "package", "import",
            "return", "if", "else", "when", "for", "while", "do", "try", "catch", "finally",
            "throw", "override", "public", "private", "protected", "internal", "abstract",
            "final", "open", "sealed", "data", "companion", "init", "constructor", "static",
            "void", "new", "this", "super", "extends", "implements", "instanceof", "true",
            "false", "null", "suspend", "typealias", "inline", "const"
        )
        isBash -> setOf(
            "echo", "cd", "ls", "git", "flyway", "gradle", "mvn", "docker", "export", "source",
            "if", "then", "else", "elif", "fi", "for", "in", "do", "done", "case", "esac",
            "while", "until", "sudo", "curl", "cat", "mkdir", "rm", "cp", "mv", "grep", "chmod"
        )
        isJson || isYaml -> setOf("true", "false", "null", "yes", "no")
        else -> emptySet()
    }

    val types = when {
        isSql -> setOf(
            "VARCHAR", "CHAR", "TEXT", "INT", "INTEGER", "BIGINT", "SMALLINT", "BOOLEAN",
            "TIMESTAMP", "DATE", "TIME", "DECIMAL", "NUMERIC", "FLOAT", "DOUBLE", "SERIAL",
            "BIGSERIAL", "UUID", "JSON", "JSONB", "BLOB", "BYTEA"
        )
        isJavaKotlin -> setOf(
            "Int", "Long", "Float", "Double", "Boolean", "String", "List", "Map", "Set",
            "Array", "Unit", "Any", "Nothing", "Integer", "Object"
        )
        else -> emptySet()
    }

    val commentPrefix = when {
        isSql -> "--"
        isBash || isYaml -> "#"
        else -> "//"
    }

    return buildAnnotatedString {
        val lines = code.lines()
        lines.forEachIndexed { lineIdx, line ->
            val lineRegex = Regex(
                "(${Regex.escape(commentPrefix)}.*$)|" +
                "(\"[^\"]*\"|'[^']*')|" +
                "(\\b[a-zA-Z_][a-zA-Z0-9_]*\\b)|" +
                "(\\b\\d+(\\.\\d+)?\\b)|" +
                "(--[a-zA-Z0-9_-]+|-[a-zA-Z0-9]+)"
            )

            var lastPos = 0
            for (match in lineRegex.findAll(line)) {
                if (match.range.first > lastPos) {
                    append(line.substring(lastPos, match.range.first))
                }

                val matchedText = match.value
                when {
                    matchedText.startsWith(commentPrefix) -> {
                        withStyle(SpanStyle(color = commentColor, fontStyle = FontStyle.Italic)) {
                            append(matchedText)
                        }
                    }
                    matchedText.startsWith("\"") || matchedText.startsWith("'") -> {
                        withStyle(SpanStyle(color = stringColor)) {
                            append(matchedText)
                        }
                    }
                    isBash && (matchedText.startsWith("-") || matchedText.startsWith("--")) -> {
                        withStyle(SpanStyle(color = typeColor)) {
                            append(matchedText)
                        }
                    }
                    matchedText.matches(Regex("^\\d+(\\.\\d+)?$")) -> {
                        withStyle(SpanStyle(color = numberColor)) {
                            append(matchedText)
                        }
                    }
                    else -> {
                        val isKw = if (isSql) keywords.any { it.equals(matchedText, ignoreCase = true) } else matchedText in keywords
                        val isTp = if (isSql) types.any { it.equals(matchedText, ignoreCase = true) } else matchedText in types

                        when {
                            isKw -> withStyle(SpanStyle(color = keywordColor, fontWeight = FontWeight.Bold)) {
                                append(matchedText)
                            }
                            isTp -> withStyle(SpanStyle(color = typeColor)) {
                                append(matchedText)
                            }
                            else -> append(matchedText)
                        }
                    }
                }
                lastPos = match.range.last + 1
            }

            if (lastPos < line.length) {
                append(line.substring(lastPos))
            }

            if (lineIdx < lines.lastIndex) {
                append("\n")
            }
        }
    }
}
