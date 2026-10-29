package com.saulo.readquest.data.parser

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class QuizMarkdownParserTest {

    @Test
    fun parse_validQuizMarkdown_extractsQuestionsAndAnswers() {
        val markdown = """
            ## Q1
            Which statement correctly defines class Printer?

            ### Option A
            public class Printer {}
            package com.exam;

            ### Option B [CORRECT]
            package com.exam;
            public class Printer {}

            ### Option C
            public class Printer {
                package com.exam;
            }

            ### Option D
            import java.util.*;
            package com.exam;

            ### Explanation
            Package statements must precede all class and import statements.
            ===
            ## Q2
            What is the default value of boolean?

            ### Option A
            true

            ### Option B [CORRECT]
            false

            ### Option C
            null

            ### Option D
            0

            ### Explanation
            Default boolean value is false.
        """.trimIndent()

        val questions = QuizMarkdownParser.parse(markdown, "test-quiz")
        assertEquals(2, questions.size)

        val q1 = questions[0]
        assertEquals("test-quiz-q1", q1.id)
        assertEquals(1, q1.questionNumber)
        assertEquals("Which statement correctly defines class Printer?", q1.prompt)
        assertEquals(4, q1.options.size)
        assertEquals(1, q1.correctOptionIndex)
        assertEquals("package com.exam;\npublic class Printer {}", q1.options[1])
        assertEquals("Package statements must precede all class and import statements.", q1.explanation)

        val q2 = questions[1]
        assertEquals("test-quiz-q2", q2.id)
        assertEquals(2, q2.questionNumber)
        assertEquals("What is the default value of boolean?", q2.prompt)
        assertEquals(1, q2.correctOptionIndex)
        assertEquals("false", q2.options[1])
    }

    @Test
    fun parse_readquestNavigationQuiz_parsesAllFiveQuestions() {
        val file = java.io.File("../../content/readquest-guide/quiz/navigation-quiz.md")
        val questions = QuizMarkdownParser.parse(file.readText(), "quiz-rq-1")
        assertEquals(5, questions.size)
        questions.forEach { q ->
            assertEquals(4, q.options.size)
            assertTrue(q.explanation.isNotEmpty())
            assertTrue(q.correctOptionIndex in 0..3)
        }
    }

    @Test
    fun parse_readquestAdvancedQuiz_parsesAllFiveQuestions() {
        val file = java.io.File("../../content/readquest-guide/quiz/advanced-quiz.md")
        val questions = QuizMarkdownParser.parse(file.readText(), "quiz-rq-2")
        assertEquals(5, questions.size)
        questions.forEach { q ->
            assertEquals(4, q.options.size)
            assertTrue(q.explanation.isNotEmpty())
            assertTrue(q.correctOptionIndex in 0..3)
        }
    }
}
