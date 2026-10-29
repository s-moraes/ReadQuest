package com.saulo.readquest.ui.screens.quiz

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.saulo.readquest.ui.model.QuizQuestionUiModel
import com.saulo.readquest.ui.model.QuizTopicUiModel
import com.saulo.readquest.ui.screens.common.AppConfirmDialog
import com.saulo.readquest.ui.screens.common.DualActionBottomBar
import com.saulo.readquest.ui.screens.common.PillBadge
import com.saulo.readquest.ui.screens.common.SecondaryTopAppBar
import kotlinx.coroutines.delay
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizRunnerScreen(
    quiz: QuizTopicUiModel,
    questions: List<QuizQuestionUiModel>,
    onBack: () -> Unit,
    onSubmit: (String, Map<Int, Int>) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var currentQuestionIndex by remember { mutableIntStateOf(0) }
    val selectedAnswers = remember { mutableStateMapOf<Int, Int>() }
    var remainingSeconds by remember { mutableIntStateOf(quiz.timeLimitMinutes * 60) }
    var isPaused by remember { mutableStateOf(false) }
    var showExitDialog by remember { mutableStateOf(false) }
    var showIncompleteSubmitDialog by remember { mutableStateOf(false) }

    BackHandler {
        showExitDialog = true
    }

    // Countdown Timer Loop
    LaunchedEffect(isPaused) {
        while (!isPaused && remainingSeconds > 0) {
            delay(1000L)
            remainingSeconds--
        }
        if (remainingSeconds == 0 && questions.isNotEmpty()) {
            onSubmit(quiz.id, selectedAnswers.toMap())
        }
    }

    val currentQuestion = questions.getOrNull(currentQuestionIndex)
    val totalQuestions = questions.size.coerceAtLeast(1)
    val progressPercent = ((currentQuestionIndex + 1) * 100) / totalQuestions

    val hours = remainingSeconds / 3600
    val minutes = (remainingSeconds % 3600) / 60
    val seconds = remainingSeconds % 60
    val formattedTime = String.format(Locale.US, "⏱️ %02d:%02d:%02d", hours, minutes, seconds)

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            SecondaryTopAppBar(
                title = quiz.title,
                onBack = { showExitDialog = true },
                navigationIconDescription = "Exit Quiz",
                actions = {
                    IconButton(onClick = { isPaused = !isPaused }) {
                        Icon(
                            imageVector = if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                            contentDescription = if (isPaused) "Resume" else "Pause",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    val timerBgColor = when {
                        remainingSeconds <= 30 -> MaterialTheme.colorScheme.errorContainer
                        remainingSeconds <= 120 -> MaterialTheme.colorScheme.tertiaryContainer
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    }
                    val timerTextColor = when {
                        remainingSeconds <= 30 -> MaterialTheme.colorScheme.onErrorContainer
                        remainingSeconds <= 120 -> MaterialTheme.colorScheme.onTertiaryContainer
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                    PillBadge(
                        text = formattedTime,
                        containerColor = timerBgColor,
                        contentColor = timerTextColor,
                        shape = CircleShape,
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier.padding(end = 12.dp)
                    )
                }
            )
        },
        bottomBar = {
            val isLastQuestion = currentQuestionIndex == questions.lastIndex
            DualActionBottomBar(
                secondaryText = "← Previous",
                secondaryEnabled = currentQuestionIndex > 0,
                onSecondaryClick = {
                    if (currentQuestionIndex > 0) {
                        currentQuestionIndex--
                    }
                },
                primaryText = if (isLastQuestion) "✓ Submit Quiz" else "Next →",
                onPrimaryClick = {
                    if (isLastQuestion) {
                        if (selectedAnswers.size < questions.size) {
                            showIncompleteSubmitDialog = true
                        } else {
                            onSubmit(quiz.id, selectedAnswers.toMap())
                        }
                    } else {
                        currentQuestionIndex++
                    }
                }
            )
        }
    ) { innerPadding ->
        if (currentQuestion != null) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Progress Header
                item {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Question ${currentQuestionIndex + 1} of ${questions.size}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Medium
                            )
                            val answeredCount = selectedAnswers.size
                            Text(
                                text = "$answeredCount/${questions.size} answered ($progressPercent%)",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        LinearProgressIndicator(
                            progress = { (currentQuestionIndex + 1).toFloat() / totalQuestions },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant,
                            strokeCap = StrokeCap.Round
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Question Palette Strip
                        val stripListState = rememberLazyListState()
                        LaunchedEffect(currentQuestionIndex) {
                            stripListState.animateScrollToItem(currentQuestionIndex)
                        }

                        LazyRow(
                            state = stripListState,
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            items(questions.size) { index ->
                                val isCurrent = index == currentQuestionIndex
                                val isAnswered = selectedAnswers.containsKey(index)

                                val backgroundColor = when {
                                    isCurrent -> MaterialTheme.colorScheme.primaryContainer
                                    isAnswered -> MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                    else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                }
                                val contentColor = when {
                                    isCurrent -> MaterialTheme.colorScheme.onPrimaryContainer
                                    isAnswered -> MaterialTheme.colorScheme.primary
                                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                                }
                                val borderStroke = when {
                                    isCurrent -> BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                                    isAnswered -> BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                                    else -> BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
                                }

                                Surface(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .clickable {
                                            currentQuestionIndex = index
                                        },
                                    shape = CircleShape,
                                    color = backgroundColor,
                                    border = borderStroke
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = "${index + 1}",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = if (isCurrent || isAnswered) FontWeight.Bold else FontWeight.Medium,
                                            color = contentColor
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Question Prompt Card
                item {
                    OutlinedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.outlinedCardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "${currentQuestionIndex + 1}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Text(
                                text = currentQuestion.prompt,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // Instruction Pill
                item {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = "🔘 Select one option",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }

                // 4 Selectable Options
                items(currentQuestion.options.size) { optionIndex ->
                    val optionText = currentQuestion.options[optionIndex]
                    val isSelected = selectedAnswers[currentQuestionIndex] == optionIndex
                    val optionLetter = ('A'.code + optionIndex).toChar().toString()
                    val isCode = optionText.contains("\n") ||
                            optionText.contains("{") ||
                            optionText.contains(";") ||
                            optionText.contains("class") ||
                            optionText.contains("package")

                    OutlinedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .clickable {
                                selectedAnswers[currentQuestionIndex] = optionIndex
                            },
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.outlinedCardColors(
                            containerColor = if (isSelected) {
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                            } else {
                                MaterialTheme.colorScheme.surface
                            }
                        ),
                        border = BorderStroke(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.surfaceVariant
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = optionLetter,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Text(
                                text = optionText,
                                style = if (isCode) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.bodyLarge,
                                fontFamily = if (isCode) FontFamily.Monospace else FontFamily.Default,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // Share Question Action
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        OutlinedButton(
                            onClick = {
                                val shareText = formatQuestionForShare(
                                    prompt = currentQuestion.prompt,
                                    options = currentQuestion.options
                                )
                                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, shareText)
                                    putExtra(Intent.EXTRA_SUBJECT, currentQuestion.prompt)
                                }
                                val chooser = Intent.createChooser(sendIntent, "Share Question")
                                context.startActivity(chooser)
                            },
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share Question",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Share",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }

    // Exit Confirmation Dialog
    if (showExitDialog) {
        AppConfirmDialog(
            title = "Leave Quiz?",
            message = "Leave quiz? Current progress will be lost.",
            confirmText = "Leave",
            dismissText = "Cancel",
            isDestructive = true,
            onConfirm = {
                showExitDialog = false
                onBack()
            },
            onDismiss = { showExitDialog = false }
        )
    }

    // Pause Modal Dialog
    if (isPaused) {
        AppConfirmDialog(
            title = "Quiz Paused",
            message = "Timer is paused. Take a breath and resume when ready.",
            confirmText = "Resume Quiz",
            dismissText = null,
            icon = Icons.Default.Pause,
            onConfirm = { isPaused = false },
            onDismiss = { isPaused = false }
        )
    }

    // Incomplete Submission Warning Dialog
    if (showIncompleteSubmitDialog) {
        val unansweredCount = questions.size - selectedAnswers.size
        AppConfirmDialog(
            title = "Unanswered Questions",
            message = "You have answered ${selectedAnswers.size} of ${questions.size} questions ($unansweredCount pending).\n\nAre you sure you want to submit your quiz now?",
            confirmText = "Submit Anyway",
            dismissText = "Review Questions",
            isDestructive = true,
            icon = Icons.Default.WarningAmber,
            onConfirm = {
                showIncompleteSubmitDialog = false
                onSubmit(quiz.id, selectedAnswers.toMap())
            },
            onDismiss = {
                showIncompleteSubmitDialog = false
                val firstUnanswered = questions.indices.firstOrNull { !selectedAnswers.containsKey(it) }
                if (firstUnanswered != null) {
                    currentQuestionIndex = firstUnanswered
                }
            }
        )
    }
}

fun formatQuestionForShare(prompt: String, options: List<String>): String {
    return buildString {
        appendLine(prompt)
        appendLine()
        options.forEachIndexed { index, option ->
            val letter = ('A'.code + index).toChar()
            appendLine("$letter) $option")
        }
    }.trim()
}
