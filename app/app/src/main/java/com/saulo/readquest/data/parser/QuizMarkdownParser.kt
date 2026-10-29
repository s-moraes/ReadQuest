package com.saulo.readquest.data.parser

import com.saulo.readquest.ui.model.QuizQuestionUiModel

object QuizMarkdownParser {

    private val questionSplitterRegex = Regex("""\r?\n===\r?\n""")
    private val questionHeaderRegex = Regex("""##\s*Q(\d+)""")

    fun parse(rawMarkdown: String, quizId: String): List<QuizQuestionUiModel> {
        val blocks = rawMarkdown.split(questionSplitterRegex)
            .map { it.trim() }
            .filter { it.isNotEmpty() }

        val questions = mutableListOf<QuizQuestionUiModel>()

        for ((index, block) in blocks.withIndex()) {
            val qNumMatch = questionHeaderRegex.find(block)
            val questionNumber = qNumMatch?.groupValues?.get(1)?.toIntOrNull() ?: (index + 1)

            val optAIdx = findHeaderIndex(block, "### Option A")
            val optBIdx = findHeaderIndex(block, "### Option B")
            val optCIdx = findHeaderIndex(block, "### Option C")
            val optDIdx = findHeaderIndex(block, "### Option D")
            val explIdx = findHeaderIndex(block, "### Explanation")

            if (optAIdx == -1 || optBIdx == -1 || optCIdx == -1 || optDIdx == -1) {
                continue
            }

            // Prompt extraction
            val promptStartIndex = if (qNumMatch != null) {
                val endOfHeaderLine = block.indexOf('\n', qNumMatch.range.first)
                if (endOfHeaderLine != -1) endOfHeaderLine + 1 else qNumMatch.range.last + 1
            } else 0
            val prompt = block.substring(promptStartIndex, optAIdx).trim()

            // Option headers and contents
            val optAHeaderEnd = block.indexOf('\n', optAIdx)
            val optAHeader = if (optAHeaderEnd != -1) block.substring(optAIdx, optAHeaderEnd) else ""
            val optAText = block.substring(if (optAHeaderEnd != -1) optAHeaderEnd + 1 else optAIdx, optBIdx).trim()

            val optBHeaderEnd = block.indexOf('\n', optBIdx)
            val optBHeader = if (optBHeaderEnd != -1) block.substring(optBIdx, optBHeaderEnd) else ""
            val optBText = block.substring(if (optBHeaderEnd != -1) optBHeaderEnd + 1 else optBIdx, optCIdx).trim()

            val optCHeaderEnd = block.indexOf('\n', optCIdx)
            val optCHeader = if (optCHeaderEnd != -1) block.substring(optCIdx, optCHeaderEnd) else ""
            val optCText = block.substring(if (optCHeaderEnd != -1) optCHeaderEnd + 1 else optCIdx, optDIdx).trim()

            val endOfD = if (explIdx != -1) explIdx else block.length
            val optDHeaderEnd = block.indexOf('\n', optDIdx)
            val optDHeader = if (optDHeaderEnd != -1) block.substring(optDIdx, optDHeaderEnd) else ""
            val optDText = block.substring(if (optDHeaderEnd != -1) optDHeaderEnd + 1 else optDIdx, endOfD).trim()

            var correctIndex = 0
            when {
                optAHeader.contains("[CORRECT]", ignoreCase = true) -> correctIndex = 0
                optBHeader.contains("[CORRECT]", ignoreCase = true) -> correctIndex = 1
                optCHeader.contains("[CORRECT]", ignoreCase = true) -> correctIndex = 2
                optDHeader.contains("[CORRECT]", ignoreCase = true) -> correctIndex = 3
                optAText.contains("[CORRECT]", ignoreCase = true) -> correctIndex = 0
                optBText.contains("[CORRECT]", ignoreCase = true) -> correctIndex = 1
                optCText.contains("[CORRECT]", ignoreCase = true) -> correctIndex = 2
                optDText.contains("[CORRECT]", ignoreCase = true) -> correctIndex = 3
            }

            val cleanA = optAText.replace("[CORRECT]", "").trim()
            val cleanB = optBText.replace("[CORRECT]", "").trim()
            val cleanC = optCText.replace("[CORRECT]", "").trim()
            val cleanD = optDText.replace("[CORRECT]", "").trim()

            // Explanation extraction
            val explanation = if (explIdx != -1) {
                val endOfExplHeader = block.indexOf('\n', explIdx)
                if (endOfExplHeader != -1) {
                    block.substring(endOfExplHeader + 1).trim()
                } else {
                    ""
                }
            } else ""

            questions.add(
                QuizQuestionUiModel(
                    id = "$quizId-q$questionNumber",
                    quizId = quizId,
                    questionNumber = questionNumber,
                    prompt = prompt,
                    options = listOf(cleanA, cleanB, cleanC, cleanD),
                    correctOptionIndex = correctIndex,
                    explanation = explanation
                )
            )
        }

        return questions
    }

    private fun findHeaderIndex(text: String, prefix: String): Int {
        val directIndex = text.indexOf(prefix)
        if (directIndex != -1) return directIndex
        // Case-insensitive fallback
        return text.indexOf(prefix, ignoreCase = true)
    }
}
