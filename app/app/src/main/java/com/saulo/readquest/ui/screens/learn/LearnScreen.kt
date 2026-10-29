package com.saulo.readquest.ui.screens.learn

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.saulo.readquest.ui.model.LearningPathUiModel
import com.saulo.readquest.ui.model.SubjectUiModel
import com.saulo.readquest.ui.model.TopicItemUiModel
import com.saulo.readquest.ui.navigation.AppRoute
import com.saulo.readquest.ui.screens.common.SectionHeader
import com.saulo.readquest.ui.screens.common.SubjectPickerBottomSheet
import com.saulo.readquest.ui.screens.common.TopicRowCard
import com.saulo.readquest.ui.theme.SuccessGreen

@Composable
fun LearnScreen(
    subject: SubjectUiModel,
    topics: List<TopicItemUiModel> = emptyList(),
    learningPaths: List<LearningPathUiModel> = emptyList(),
    availableSubjects: List<SubjectUiModel> = emptyList(),
    onSubjectSelected: (String) -> Unit = {},
    onNavigate: (AppRoute) -> Unit,
    modifier: Modifier = Modifier
) {
    var showSubjectPickerSheet by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            LearnHeaderSection(
                subject = subject,
                topics = topics,
                onHeaderClick = {
                    if (availableSubjects.isNotEmpty()) {
                        showSubjectPickerSheet = true
                    }
                }
            )
        }

        item {
            ContinueLearningHeroCard(
                subject = subject,
                onCardClick = {
                    onNavigate(AppRoute.ContentReader(subject.resumeTopicId, isExtra = false))
                }
            )
        }

        item {
            SectionHeader(title = "Topics")
        }

        if (topics.isNotEmpty()) {
            items(topics, key = { it.id }) { topic ->
                TopicRowCard(
                    topic = topic,
                    onClick = { onNavigate(AppRoute.ContentReader(topic.id, isExtra = false)) }
                )
            }
        } else {
            items(learningPaths, key = { it.id }) { path ->
                LearningPathItemCard(
                    path = path,
                    onClick = { onNavigate(AppRoute.PathTopics(path.id)) }
                )
            }
        }
    }

    if (showSubjectPickerSheet && availableSubjects.isNotEmpty()) {
        SubjectPickerBottomSheet(
            subjects = availableSubjects,
            activeSubjectId = subject.id,
            onSubjectSelected = { newSubjectId ->
                showSubjectPickerSheet = false
                onSubjectSelected(newSubjectId)
            },
            onDismissRequest = {
                showSubjectPickerSheet = false
            }
        )
    }
}

@Composable
private fun LearnHeaderSection(
    subject: SubjectUiModel,
    topics: List<TopicItemUiModel>,
    onHeaderClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val completedCount = topics.count { it.isCompleted }
    val totalCount = if (topics.isNotEmpty()) topics.size else subject.resumeTotalTopics
    val progress = if (totalCount > 0) completedCount.toFloat() / totalCount else 0f

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .clickable(onClick = onHeaderClick)
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = subject.title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Select Subject",
                tint = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.size(28.dp)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Topic ${subject.resumeTopicIndex} of ${subject.resumeTotalTopics}",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(6.dp))

        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
            strokeCap = StrokeCap.Round
        )
    }
}

@Composable
private fun ContinueLearningHeroCard(
    subject: SubjectUiModel,
    onCardClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onCardClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 14.dp)
        ) {
            val badgeText = if (subject.isResumeTopicAccessed) "CONTINUE READING" else "READ NOW"
            Text(
                text = badgeText,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                letterSpacing = androidx.compose.ui.unit.TextUnit(1.2f, androidx.compose.ui.unit.TextUnitType.Sp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = subject.resumeTopicTitle,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun LearningPathItemCard(
    path: LearningPathUiModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val progress = if (path.totalCount > 0) {
        path.completedCount.toFloat() / path.totalCount
    } else {
        0f
    }

    val icon: ImageVector = when {
        path.title.contains("Distributed", ignoreCase = true) -> Icons.Default.Hub
        path.title.contains("Scaling", ignoreCase = true) -> Icons.Default.SwapHoriz
        else -> Icons.Default.AccountTree
    }

    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = path.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = path.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .weight(1f)
                            .height(5.dp)
                            .clip(RoundedCornerShape(2.5.dp)),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                        strokeCap = StrokeCap.Round
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "${path.completedCount}/${path.totalCount}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Open Path",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
