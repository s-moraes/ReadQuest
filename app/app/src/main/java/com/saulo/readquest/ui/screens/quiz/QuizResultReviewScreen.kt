package com.saulo.readquest.ui.screens.quiz

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.saulo.readquest.ui.model.QuizQuestionUiModel
import com.saulo.readquest.ui.model.QuizTopicUiModel
import com.saulo.readquest.ui.screens.common.SecondaryTopAppBar
import com.saulo.readquest.ui.screens.common.SectionHeader
import com.saulo.readquest.ui.theme.SuccessGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizResultReviewScreen(
    quizId: String,
    selectedAnswers: Map<Int, Int>,
    onDone: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    quiz: QuizTopicUiModel? = null,
    questions: List<QuizQuestionUiModel> = emptyList()
) {
    val context = LocalContext.current
    val totalQuestions = questions.size.coerceAtLeast(1)
    val correctCount = remember(questions, selectedAnswers) {
        questions.mapIndexed { index, question ->
            selectedAnswers[index] == question.correctOptionIndex
        }.count { it }
    }

    val scorePercent = if (questions.isNotEmpty()) (correctCount * 100) / totalQuestions else 0
    val passPercentage = quiz?.passPercentage ?: 65
    val isPassed = scorePercent >= passPercentage

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            SecondaryTopAppBar(
                title = "Result & Review",
                onBack = onDone,
                navigationIconDescription = "Back to Quiz List",
                actions = {
                    IconButton(onClick = {
                        val reviewSummary = buildString {
                            appendLine("${quiz?.title ?: "Quiz"} - Results")
                            appendLine("Score: $correctCount/${questions.size} ($scorePercent%)")
                            appendLine(if (isPassed) "Status: Passed ✓" else "Status: Keep Practicing")
                        }.trim()
                        val sendIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, reviewSummary)
                            putExtra(Intent.EXTRA_SUBJECT, "${quiz?.title ?: "Quiz"} Results")
                        }
                        context.startActivity(Intent.createChooser(sendIntent, "Share Quiz Result"))
                    }) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Hero Score Card
            item {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 3.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = quiz?.title ?: "Quiz Review",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "$correctCount/$totalQuestions Correct",
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Score: $scorePercent%",
                                style = MaterialTheme.typography.titleMedium,
                                color = if (isPassed) SuccessGreen else MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (isPassed) "Awesome mastery! 🏆" else "Keep going 💪",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isPassed) SuccessGreen.copy(alpha = 0.15f)
                                    else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (isPassed) "🏆" else "📚",
                                style = MaterialTheme.typography.headlineMedium
                            )
                        }
                    }
                }
            }

            // Status Summary & Actions Block
            item {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp)
                    ) {
                        Text(
                            text = "✅ You got $correctCount/$totalQuestions correct.",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "📊 Score: $scorePercent% (Passing threshold: $passPercentage%)",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (isPassed) "🏆 Passed! Section completed." else "❌ Failed. Try again.",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isPassed) SuccessGreen else MaterialTheme.colorScheme.error
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = onRetry,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = "🔄 Retry Quiz",
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Button(
                                onClick = onDone,
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                ),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = "Done",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // Question-by-Question Review Header
            item {
                SectionHeader(title = "Question-by-Question Review")
            }

            // Question Cards
            items(questions.size) { questionIndex ->
                val question = questions[questionIndex]
                val userSelected = selectedAnswers[questionIndex]

                QuestionReviewCard(
                    question = question,
                    userSelectedOptionIndex = userSelected
                )
            }
        }
    }
}

@Composable
private fun QuestionReviewCard(
    question: QuizQuestionUiModel,
    userSelectedOptionIndex: Int?,
    modifier: Modifier = Modifier
) {
    OutlinedCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Text(
                text = "Q${question.questionNumber}: ${question.prompt}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Options List
            question.options.forEachIndexed { index, optionText ->
                val isCorrectOption = index == question.correctOptionIndex
                val isUserWrongChoice = userSelectedOptionIndex == index && !isCorrectOption
                val optionLetter = ('A'.code + index).toChar().toString()
                val isCode = optionText.contains("\n") || optionText.contains("{") || optionText.contains(";")

                val backgroundColor = when {
                    isCorrectOption -> SuccessGreen.copy(alpha = 0.18f)
                    isUserWrongChoice -> MaterialTheme.colorScheme.error.copy(alpha = 0.12f)
                    else -> MaterialTheme.colorScheme.surface
                }

                val borderColor = when {
                    isCorrectOption -> SuccessGreen
                    isUserWrongChoice -> MaterialTheme.colorScheme.error
                    else -> MaterialTheme.colorScheme.outlineVariant
                }

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = backgroundColor,
                    border = BorderStroke(if (isCorrectOption || isUserWrongChoice) 2.dp else 1.dp, borderColor)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        isCorrectOption -> SuccessGreen
                                        isUserWrongChoice -> MaterialTheme.colorScheme.error
                                        else -> MaterialTheme.colorScheme.surfaceVariant
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            when {
                                isCorrectOption -> {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Correct",
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                isUserWrongChoice -> {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Your answer (wrong)",
                                        tint = MaterialTheme.colorScheme.onError,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                else -> {
                                    Text(
                                        text = optionLetter,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = optionText,
                                style = if (isCode) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.bodyLarge,
                                fontFamily = if (isCode) FontFamily.Monospace else FontFamily.Default,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (isCorrectOption) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Correct Answer",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = SuccessGreen
                                )
                            } else if (isUserWrongChoice) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Your Answer",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Explanation Footer
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Text(
                    text = "💡 Explanation: ${question.explanation}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }
    }
}
