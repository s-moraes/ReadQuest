package com.saulo.readquest.ui.screens.reader

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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalTextToolbar
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.saulo.readquest.ui.model.ExtraItemUiModel
import com.saulo.readquest.ui.model.TopicItemUiModel
import com.saulo.readquest.ui.screens.common.DualActionBottomBar
import com.saulo.readquest.ui.screens.common.SecondaryTopAppBar
import com.saulo.readquest.ui.theme.SuccessGreen
import com.saulo.readquest.ui.theme.ThemeMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContentReaderScreen(
    itemId: String,
    isExtra: Boolean,
    onBack: () -> Unit,
    onNavigateToExtra: (String) -> Unit,
    onNavigateToQuiz: (String) -> Unit,
    onTopicCompleted: () -> Unit,
    modifier: Modifier = Modifier,
    providedTopic: TopicItemUiModel? = null,
    providedExtra: ExtraItemUiModel? = null,
    providedContent: String? = null,
    nextTopicId: String? = null,
    nextTopicTitle: String? = null,
    onNavigateToNextTopic: ((String) -> Unit)? = null,
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    onThemeModeChanged: (ThemeMode) -> Unit = {},
    fontScale: Float = 1.0f,
    onFontScaleChanged: (Float) -> Unit = {},
    customThemePreset: String = "SEPIA",
    customBackgroundHex: String = "#F4ECD8",
    customTextHex: String = "#2C2416",
    onUpdateCustomColors: (String, String, String) -> Unit = { _, _, _ -> }
) {
    val uriHandler = LocalUriHandler.current
    val scrollState = rememberScrollState()

    val context = LocalContext.current
    val view = LocalView.current
    val shareableTextToolbar = remember(view, context) {
        ShareableTextToolbar(view, context)
    }

    var showAppearanceSheet by remember { mutableStateOf(false) }

    val topicItem: TopicItemUiModel? = remember(itemId, isExtra, providedTopic) {
        providedTopic
    }

    val extraItem = remember(itemId, isExtra, providedExtra) {
        providedExtra
    }

    var isExtraRead by remember(itemId) { mutableStateOf(false) }
    var isTopicCompletedState by remember(topicItem) {
        mutableStateOf(topicItem?.isCompleted ?: false)
    }

    val title = if (isExtra) {
        extraItem?.title ?: "Extra Reader"
    } else {
        topicItem?.title ?: "Topic Reader"
    }

    val markdownContent = providedContent ?: if (isExtra) {
        extraItem?.let {
            """
# ${it.title}

${it.summary}

## Overview
This study material covers practical patterns, architectures, and implementation trade-offs for real-world engineering systems.

### Key Highlights
- **Category:** ${it.category.displayName}
- **Tags:** ${it.tags.joinToString(", ")}

```java
// Production Reference Implementation
public class SystemWorker {
    public void execute() {
        System.out.println("Processing item: ${it.id}");
    }
}
```
            """.trimIndent()
        } ?: "# Content Not Found\n\nUnable to load the requested extra item."
    } else {
        topicItem?.markdownContent ?: "# Topic Content\n\nNo content available for this topic."
    }

    val scrollProgress = if (scrollState.maxValue > 0) {
        (scrollState.value.toFloat() / scrollState.maxValue).coerceIn(0f, 1f)
    } else {
        0f
    }

    if (showAppearanceSheet) {
        ReaderAppearanceBottomSheet(
            themeMode = themeMode,
            onThemeModeChanged = onThemeModeChanged,
            fontScale = fontScale,
            onFontScaleChanged = onFontScaleChanged,
            customThemePreset = customThemePreset,
            customBackgroundHex = customBackgroundHex,
            customTextHex = customTextHex,
            onUpdateCustomColors = onUpdateCustomColors,
            onDismissRequest = { showAppearanceSheet = false }
        )
    }

    CompositionLocalProvider(LocalTextToolbar provides shareableTextToolbar) {
        Scaffold(
            modifier = modifier.fillMaxSize(),
        topBar = {
            Column(modifier = Modifier.fillMaxWidth()) {
                SecondaryTopAppBar(
                    title = title,
                    onBack = onBack,
                    actions = {
                        IconButton(onClick = { showAppearanceSheet = true }) {
                            Icon(
                                imageVector = Icons.Default.FormatSize,
                                contentDescription = "Appearance & Text Size",
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                )
                // Scroll progress indicator at bottom edge of TopAppBar
                LinearProgressIndicator(
                    progress = { scrollProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    strokeCap = StrokeCap.Square
                )
            }
        },
        bottomBar = {
            if (!isExtra) {
                if (nextTopicId != null && onNavigateToNextTopic != null) {
                    DualActionBottomBar(
                        secondaryText = if (isTopicCompletedState) "Completed" else "Mark Complete",
                        secondaryLeadingIcon = if (isTopicCompletedState) {
                            {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = SuccessGreen,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        } else null,
                        onSecondaryClick = {
                            isTopicCompletedState = true
                            onTopicCompleted()
                        },
                        primaryText = "Next Topic →",
                        onPrimaryClick = {
                            isTopicCompletedState = true
                            onTopicCompleted()
                            onNavigateToNextTopic(nextTopicId)
                        }
                    )
                } else {
                    DualActionBottomBar(
                        primaryText = if (isTopicCompletedState) "✓ Completed" else "✓ Complete Topic",
                        primaryLeadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        primaryColors = ButtonDefaults.buttonColors(
                            containerColor = if (isTopicCompletedState) SuccessGreen else MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        onPrimaryClick = {
                            isTopicCompletedState = true
                            onTopicCompleted()
                        }
                    )
                }
            } else {
                if (extraItem?.externalUrl != null) {
                    DualActionBottomBar(
                        secondaryText = if (isExtraRead) "✓ Read" else "✓ Mark as Read",
                        onSecondaryClick = {
                            isExtraRead = !isExtraRead
                        },
                        primaryText = "Open Link",
                        primaryTrailingIcon = {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        onPrimaryClick = {
                            uriHandler.openUri(extraItem.externalUrl)
                        }
                    )
                } else {
                    DualActionBottomBar(
                        secondaryText = if (isExtraRead) "✓ Read" else "✓ Mark as Read",
                        onSecondaryClick = {
                            isExtraRead = !isExtraRead
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            if (isTopicCompletedState) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = SuccessGreen.copy(alpha = 0.15f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = SuccessGreen,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "Completed",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = SuccessGreen
                            )
                        }
                    }
                }
            }

            MarkdownRenderer(content = markdownContent)

            // Related Cross-Links Section
            if (!isExtra && topicItem != null && (topicItem.relatedExtraIds.isNotEmpty() || topicItem.relatedQuizId != null)) {
                Spacer(modifier = Modifier.height(28.dp))

                Text(
                    text = "Related Practice & Reading",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(12.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    topicItem.relatedExtraIds.forEach { extraId ->
                        val extraTitle = extraId.replace('-', ' ').replaceFirstChar { it.uppercase() }
                        AssistChip(
                            onClick = { onNavigateToExtra(extraId) },
                            label = {
                                Text(
                                    text = "💡 Related Extra: $extraTitle",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Lightbulb,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            shape = CircleShape,
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        )
                    }

                    if (topicItem.relatedQuizId != null) {
                        AssistChip(
                            onClick = { onNavigateToQuiz(topicItem.relatedQuizId) },
                            label = {
                                Text(
                                    text = "🎯 Practice: Take Topic Quiz",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Quiz,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            shape = CircleShape,
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    }
                }
            }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}
