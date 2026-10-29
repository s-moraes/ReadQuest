package com.saulo.readquest.ui.screens.reader

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MarkdownRendererParserTest {

    @Test
    fun testParseHeadingsAndParagraphs() {
        val markdown = """
            # Heading 1
            Regular paragraph text.
            ## Heading 2
            ### Heading 3
        """.trimIndent()

        val nodes = parseMarkdown(markdown)
        assertEquals(4, nodes.size)

        assertEquals(MarkdownNode.Heading(1, "Heading 1"), nodes[0])
        assertEquals(MarkdownNode.Paragraph("Regular paragraph text."), nodes[1])
        assertEquals(MarkdownNode.Heading(2, "Heading 2"), nodes[2])
        assertEquals(MarkdownNode.Heading(3, "Heading 3"), nodes[3])
    }

    @Test
    fun testParseHorizontalRule() {
        val markdown = """
            Top content
            ---
            Middle content
            ***
            Bottom content
        """.trimIndent()

        val nodes = parseMarkdown(markdown)
        assertEquals(5, nodes.size)
        assertEquals(MarkdownNode.Paragraph("Top content"), nodes[0])
        assertEquals(MarkdownNode.HorizontalRule, nodes[1])
        assertEquals(MarkdownNode.Paragraph("Middle content"), nodes[2])
        assertEquals(MarkdownNode.HorizontalRule, nodes[3])
        assertEquals(MarkdownNode.Paragraph("Bottom content"), nodes[4])
    }

    @Test
    fun testParseBlockquote() {
        val markdown = """
            > This is an important note or quote.
            > Spanning multiple consecutive lines.
            Regular paragraph below.
        """.trimIndent()

        val nodes = parseMarkdown(markdown)
        assertEquals(2, nodes.size)
        assertTrue(nodes[0] is MarkdownNode.Blockquote)
        val quote = nodes[0] as MarkdownNode.Blockquote
        assertEquals("This is an important note or quote.\nSpanning multiple consecutive lines.", quote.text)
        assertEquals(MarkdownNode.Paragraph("Regular paragraph below."), nodes[1])
    }

    @Test
    fun testParseGfmTable() {
        val markdown = """
            | Feature | State-Based | Migration-Based |
            | :--- | :--- | :--- |
            | Philosophy | Target state | Delta scripts |
            | Tools | Liquibase | Flyway |
        """.trimIndent()

        val nodes = parseMarkdown(markdown)
        assertEquals(1, nodes.size)
        assertTrue(nodes[0] is MarkdownNode.Table)
        val table = nodes[0] as MarkdownNode.Table
        assertEquals(listOf("Feature", "State-Based", "Migration-Based"), table.headers)
        assertEquals(2, table.rows.size)
        assertEquals(listOf("Philosophy", "Target state", "Delta scripts"), table.rows[0])
        assertEquals(listOf("Tools", "Liquibase", "Flyway"), table.rows[1])
    }

    @Test
    fun testParseListsAndCodeBlocks() {
        val markdown = """
            - Bullet 1
            - Bullet 2
            1. Numbered 1
            2. Numbered 2
            ```kotlin
            fun hello() = "world"
            ```
        """.trimIndent()

        val nodes = parseMarkdown(markdown)
        assertEquals(5, nodes.size)
        assertEquals(MarkdownNode.BulletItem("Bullet 1"), nodes[0])
        assertEquals(MarkdownNode.BulletItem("Bullet 2"), nodes[1])
        assertEquals(MarkdownNode.NumberedItem("1", "Numbered 1"), nodes[2])
        assertEquals(MarkdownNode.NumberedItem("2", "Numbered 2"), nodes[3])
        assertTrue(nodes[4] is MarkdownNode.CodeBlock)
        val code = nodes[4] as MarkdownNode.CodeBlock
        assertEquals("kotlin", code.language)
        assertEquals("fun hello() = \"world\"", code.code)
    }

    @Test
    fun testParseInlineMarkdownBoldAndItalic() {
        val text = "This is **bold text** and this is *italic text* and __another bold__."
        val annotated = parseInlineMarkdown(text)

        assertEquals("This is bold text and this is italic text and another bold.", annotated.text)

        // Check bold spans
        val boldSpans = annotated.spanStyles.filter { it.item.fontWeight == FontWeight.Bold }
        assertEquals(2, boldSpans.size)
        assertEquals(8, boldSpans[0].start)
        assertEquals(17, boldSpans[0].end) // "bold text"

        // Check italic span
        val italicSpans = annotated.spanStyles.filter { it.item.fontStyle == FontStyle.Italic }
        assertEquals(1, italicSpans.size)
        assertEquals(30, italicSpans[0].start)
        assertEquals(41, italicSpans[0].end) // "italic text"
    }

    @Test
    fun testParseInlineCodeAndLinks() {
        val text = "Run `flyway repair` or visit [Flyway](https://flywaydb.org)."
        val annotated = parseInlineMarkdown(text, primaryColor = Color.Blue)

        assertEquals("Run  flyway repair  or visit Flyway.", annotated.text)

        // Check code span
        val codeSpans = annotated.spanStyles.filter { it.item.fontFamily == FontFamily.Monospace }
        assertEquals(1, codeSpans.size)

        // Check URL annotation
        val urlAnnotations = annotated.getStringAnnotations(tag = "URL", start = 0, end = annotated.length)
        assertEquals(1, urlAnnotations.size)
        assertEquals("https://flywaydb.org", urlAnnotations[0].item)
    }

    @Test
    fun testParseNestedCodeInsideBold() {
        val text = "**`baseline-on-migrate`:** Configuration property."
        val annotated = parseInlineMarkdown(text)

        assertEquals(" baseline-on-migrate : Configuration property.", annotated.text)

        // Both bold and monospace applied to baseline-on-migrate
        val boldSpans = annotated.spanStyles.filter { it.item.fontWeight == FontWeight.Bold }
        assertTrue(boldSpans.isNotEmpty())
        val codeSpans = annotated.spanStyles.filter { it.item.fontFamily == FontFamily.Monospace }
        assertTrue(codeSpans.isNotEmpty())
    }

    @Test
    fun testParseAlerts() {
        val markdown = """
            > [!NOTE]
            > This is a note alert.
            > [!WARNING]
            > Be careful with migrations.
            > **Rule of Thumb:** Always unbox before comparing!
        """.trimIndent()

        val nodes = parseMarkdown(markdown)
        assertEquals(3, nodes.size)

        assertTrue(nodes[0] is MarkdownNode.Alert)
        val noteAlert = nodes[0] as MarkdownNode.Alert
        assertEquals(AlertType.NOTE, noteAlert.type)
        assertEquals("NOTE", noteAlert.title)
        assertEquals("This is a note alert.", noteAlert.text)

        assertTrue(nodes[1] is MarkdownNode.Alert)
        val warnAlert = nodes[1] as MarkdownNode.Alert
        assertEquals(AlertType.WARNING, warnAlert.type)
        assertEquals("WARNING", warnAlert.title)
        assertEquals("Be careful with migrations.", warnAlert.text)

        assertTrue(nodes[2] is MarkdownNode.Alert)
        val ruleAlert = nodes[2] as MarkdownNode.Alert
        assertEquals(AlertType.NOTE, ruleAlert.type)
        assertEquals("RULE OF THUMB", ruleAlert.title)
        assertEquals("Always unbox before comparing!", ruleAlert.text)
    }

    @Test
    fun testParseTaskItems() {
        val markdown = """
            - [ ] First uncompleted task
            - [x] Second completed task
            * [X] Third completed with capital X
        """.trimIndent()

        val nodes = parseMarkdown(markdown)
        assertEquals(3, nodes.size)

        assertTrue(nodes[0] is MarkdownNode.TaskItem)
        val task1 = nodes[0] as MarkdownNode.TaskItem
        assertEquals(false, task1.isChecked)
        assertEquals("First uncompleted task", task1.text)

        assertTrue(nodes[1] is MarkdownNode.TaskItem)
        val task2 = nodes[1] as MarkdownNode.TaskItem
        assertEquals(true, task2.isChecked)
        assertEquals("Second completed task", task2.text)

        assertTrue(nodes[2] is MarkdownNode.TaskItem)
        val task3 = nodes[2] as MarkdownNode.TaskItem
        assertEquals(true, task3.isChecked)
        assertEquals("Third completed with capital X", task3.text)
    }

    @Test
    fun testHighlightCodeSql() {
        val sql = "SELECT id, name FROM users WHERE id = 42; -- query"
        val highlighted = highlightCode(sql, "sql", isDark = true)
        assertEquals(sql, highlighted.text)

        // Verify keywords like SELECT, FROM, WHERE have bold span styles
        val boldSpans = highlighted.spanStyles.filter { it.item.fontWeight == FontWeight.Bold }
        assertTrue(boldSpans.size >= 3)

        // Verify comments have italic span styles
        val italicSpans = highlighted.spanStyles.filter { it.item.fontStyle == FontStyle.Italic }
        assertTrue(italicSpans.isNotEmpty())
    }

    @Test
    fun testHighlightCodeKotlin() {
        val kt = "fun calculate(name: String): Int {\n    return 42\n}"
        val highlighted = highlightCode(kt, "kotlin", isDark = true)
        assertEquals(kt, highlighted.text)

        val boldSpans = highlighted.spanStyles.filter { it.item.fontWeight == FontWeight.Bold }
        assertTrue(boldSpans.isNotEmpty()) // fun, return
    }
}
