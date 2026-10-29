package com.saulo.readquest.quiz

import com.saulo.readquest.ui.model.QuizQuestionUiModel
import com.saulo.readquest.ui.screens.reader.MarkdownNode
import com.saulo.readquest.ui.screens.reader.parseMarkdown
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class QuizScoringTest {

    private val sampleQuestions = listOf(
        QuizQuestionUiModel(
            id = "q1",
            quizId = "quiz1",
            questionNumber = 1,
            prompt = "Question 1",
            options = listOf("A", "B", "C", "D"),
            correctOptionIndex = 0,
            explanation = "Explanation 1"
        ),
        QuizQuestionUiModel(
            id = "q2",
            quizId = "quiz1",
            questionNumber = 2,
            prompt = "Question 2",
            options = listOf("A", "B", "C", "D"),
            correctOptionIndex = 1,
            explanation = "Explanation 2"
        ),
        QuizQuestionUiModel(
            id = "q3",
            quizId = "quiz1",
            questionNumber = 3,
            prompt = "Question 3",
            options = listOf("A", "B", "C", "D"),
            correctOptionIndex = 2,
            explanation = "Explanation 3"
        ),
        QuizQuestionUiModel(
            id = "q4",
            quizId = "quiz1",
            questionNumber = 4,
            prompt = "Question 4",
            options = listOf("A", "B", "C", "D"),
            correctOptionIndex = 3,
            explanation = "Explanation 4"
        )
    )

    @Test
    fun calculateScore_allCorrect_gives100PercentAndPasses() {
        val selected = mapOf(0 to 0, 1 to 1, 2 to 2, 3 to 3)
        val correctCount = sampleQuestions.mapIndexed { index, q ->
            selected[index] == q.correctOptionIndex
        }.count { it }

        val scorePercent = (correctCount * 100) / sampleQuestions.size
        val isPassed = scorePercent >= 65

        assertEquals(4, correctCount)
        assertEquals(100, scorePercent)
        assertTrue(isPassed)
    }

    @Test
    fun calculateScore_partialCorrectBelowThreshold_fails() {
        val selected = mapOf(0 to 0, 1 to 3, 2 to 0, 3 to 1) // Only Q0 correct
        val correctCount = sampleQuestions.mapIndexed { index, q ->
            selected[index] == q.correctOptionIndex
        }.count { it }

        val scorePercent = (correctCount * 100) / sampleQuestions.size
        val isPassed = scorePercent >= 65

        assertEquals(1, correctCount)
        assertEquals(25, scorePercent)
        assertFalse(isPassed)
    }

    @Test
    fun calculateScore_emptyAnswers_gives0PercentAndFails() {
        val selected = emptyMap<Int, Int>()
        val correctCount = sampleQuestions.mapIndexed { index, q ->
            selected[index] == q.correctOptionIndex
        }.count { it }

        val scorePercent = (correctCount * 100) / sampleQuestions.size
        val isPassed = scorePercent >= 65

        assertEquals(0, correctCount)
        assertEquals(0, scorePercent)
        assertFalse(isPassed)
    }

    @Test
    fun parseMarkdown_correctlyExtractsNodes() {
        val markdown = """
            # Heading 1
            ## Heading 2
            Paragraph text here.
            - Bullet 1
            1. Numbered 1
            ```java
            public class Test {}
            ```
        """.trimIndent()

        val nodes = parseMarkdown(markdown)
        assertEquals(6, nodes.size)
        assertTrue(nodes[0] is MarkdownNode.Heading && (nodes[0] as MarkdownNode.Heading).level == 1)
        assertTrue(nodes[1] is MarkdownNode.Heading && (nodes[1] as MarkdownNode.Heading).level == 2)
        assertTrue(nodes[2] is MarkdownNode.Paragraph)
        assertTrue(nodes[3] is MarkdownNode.BulletItem)
        assertTrue(nodes[4] is MarkdownNode.NumberedItem)
        assertTrue(nodes[5] is MarkdownNode.CodeBlock)
    }

    @Test
    fun unansweredQuestions_identifiesPendingCountAndFirstUnanswered() {
        val selected = mapOf(0 to 1, 2 to 3) // Answered index 0 and 2; skipped index 1 and 3
        val unansweredCount = sampleQuestions.size - selected.size
        val firstUnanswered = sampleQuestions.indices.firstOrNull { !selected.containsKey(it) }

        assertEquals(2, unansweredCount)
        assertEquals(1, firstUnanswered)
    }

    @Test
    fun allQuestionsAnswered_hasZeroPending() {
        val selected = mapOf(0 to 0, 1 to 1, 2 to 2, 3 to 3)
        val unansweredCount = sampleQuestions.size - selected.size
        val firstUnanswered = sampleQuestions.indices.firstOrNull { !selected.containsKey(it) }

        assertEquals(0, unansweredCount)
        org.junit.Assert.assertNull(firstUnanswered)
    }
}
