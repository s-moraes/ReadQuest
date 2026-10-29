package com.saulo.readquest.quiz

import com.saulo.readquest.ui.screens.quiz.formatQuestionForShare
import org.junit.Assert.assertEquals
import org.junit.Test

class QuizShareFormatterTest {

    @Test
    fun testFormatQuestionForShare() {
        val prompt = "Which load balancing algorithm selects servers strictly in sequential cyclical order?"
        val options = listOf(
            "Round Robin",
            "Least Connections",
            "IP Hash",
            "Weighted Response Time"
        )

        val result = formatQuestionForShare(prompt, options)

        val expected = """
            Which load balancing algorithm selects servers strictly in sequential cyclical order?

            A) Round Robin
            B) Least Connections
            C) IP Hash
            D) Weighted Response Time
        """.trimIndent()

        assertEquals(expected, result)
    }

    @Test
    fun testFormatQuestionForShareWithCodeOptions() {
        val prompt = "What is the output of the following code snippet?"
        val options = listOf(
            "val x = 1\nprintln(x)",
            "Error",
            "null",
            "0"
        )

        val result = formatQuestionForShare(prompt, options)

        val expected = """
            What is the output of the following code snippet?

            A) val x = 1
            println(x)
            B) Error
            C) null
            D) 0
        """.trimIndent()

        assertEquals(expected, result)
    }
}
