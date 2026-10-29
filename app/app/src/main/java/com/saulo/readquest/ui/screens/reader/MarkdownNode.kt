package com.saulo.readquest.ui.screens.reader

import androidx.compose.runtime.Immutable

@Immutable
enum class AlertType {
    NOTE, TIP, IMPORTANT, WARNING, CAUTION
}

@Immutable
sealed interface MarkdownNode {
    data class Heading(val level: Int, val text: String) : MarkdownNode
    data class Paragraph(val text: String) : MarkdownNode
    data class BulletItem(val text: String) : MarkdownNode
    data class NumberedItem(val number: String, val text: String) : MarkdownNode
    data class TaskItem(val isChecked: Boolean, val text: String) : MarkdownNode
    data class CodeBlock(val code: String, val language: String = "") : MarkdownNode
    data class DiagramImage(val caption: String, val url: String) : MarkdownNode
    data class Table(val headers: List<String>, val rows: List<List<String>>) : MarkdownNode
    data class Blockquote(val text: String) : MarkdownNode
    data class Alert(val type: AlertType, val title: String, val text: String) : MarkdownNode
    data object HorizontalRule : MarkdownNode
}
