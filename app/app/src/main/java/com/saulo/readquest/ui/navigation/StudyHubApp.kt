package com.saulo.readquest.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.saulo.readquest.ui.screens.extra.ExtraScreen
import com.saulo.readquest.ui.screens.learn.LearnScreen
import com.saulo.readquest.ui.screens.learn.PathTopicsScreen
import com.saulo.readquest.ui.screens.placeholder.EmptyContentScreen
import com.saulo.readquest.ui.screens.quiz.QuizResultReviewScreen
import com.saulo.readquest.ui.screens.quiz.QuizRunnerScreen
import com.saulo.readquest.ui.screens.quiz.QuizScreen
import com.saulo.readquest.ui.screens.reader.ContentReaderScreen
import com.saulo.readquest.ui.screens.settings.SettingsScreen
import com.saulo.readquest.ui.theme.StudyHubTheme
import com.saulo.readquest.ui.viewmodel.StudyHubViewModel
import com.saulo.readquest.ui.viewmodel.StudyHubViewModelFactory
import kotlinx.coroutines.launch

@Composable
fun StudyHubApp(
    modifier: Modifier = Modifier,
    viewModel: StudyHubViewModel? = null
) {
    val context = LocalContext.current
    val vm: StudyHubViewModel = viewModel ?: viewModel(
        factory = StudyHubViewModelFactory(context)
    )

    val uiState by vm.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val currentRoute = uiState.currentRoute
    val selectedTab = uiState.selectedTab
    val showShellBars = currentRoute is AppRoute.HomeTabs || currentRoute is AppRoute.Settings

    BackHandler(enabled = currentRoute !is AppRoute.HomeTabs && currentRoute !is AppRoute.QuizRunner) {
        vm.onNavigateBack()
    }

    StudyHubTheme(
        themeMode = uiState.themeMode,
        fontScale = uiState.fontScale,
        customBackgroundHex = uiState.customBackgroundHex,
        customTextHex = uiState.customTextHex
    ) {
        Scaffold(
            modifier = modifier.fillMaxSize(),
            topBar = {
                if (showShellBars) {
                    StudyHubTopBar(
                        currentRoute = currentRoute,
                        selectedTab = selectedTab,
                        onNavigateToSettings = {
                            vm.onNavigate(AppRoute.Settings)
                        },
                        onNavigateBack = {
                            vm.onNavigateBack()
                        }
                    )
                }
            },
            bottomBar = {
                if (currentRoute is AppRoute.HomeTabs) {
                    FloatingBottomNavigationBar(
                        selectedTab = selectedTab,
                        onTabSelected = { tab ->
                            vm.onTabSelected(tab)
                        }
                    )
                }
            },
            snackbarHost = {
                SnackbarHost(hostState = snackbarHostState)
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .then(
                        if (showShellBars) {
                            if (currentRoute is AppRoute.HomeTabs) {
                                Modifier.padding(top = innerPadding.calculateTopPadding())
                            } else {
                                Modifier.padding(innerPadding)
                            }
                        } else Modifier
                    )
            ) {
                when (val route = currentRoute) {
                    is AppRoute.HomeTabs -> {
                        Crossfade(
                            targetState = selectedTab,
                            label = "homeTabCrossfade"
                        ) { tab ->
                            when (tab) {
                                NavigationTab.LEARN -> {
                                    val subject = uiState.activeSubject
                                    if (subject != null && (uiState.topics.isNotEmpty() || uiState.learningPaths.isNotEmpty())) {
                                        LearnScreen(
                                            subject = subject,
                                            topics = uiState.topics,
                                            learningPaths = uiState.learningPaths,
                                            availableSubjects = uiState.availableSubjects,
                                            onSubjectSelected = { newSubjectId ->
                                                vm.onSelectSubject(newSubjectId)
                                            },
                                            onNavigate = { destination ->
                                                vm.onNavigate(destination)
                                            }
                                        )
                                    } else {
                                        EmptyContentScreen(
                                            tab = NavigationTab.LEARN,
                                            isSyncing = uiState.isSyncing,
                                            syncProgressMessage = uiState.syncProgressMessage,
                                            syncErrorMessage = uiState.syncErrorMessage,
                                            onSyncClick = { vm.syncFromWeb() },
                                            onNavigateToSettings = { vm.onNavigate(AppRoute.Settings) }
                                        )
                                    }
                                }
                                NavigationTab.EXTRA -> {
                                    if (uiState.extraItems.isNotEmpty()) {
                                        ExtraScreen(
                                            items = uiState.extraItems,
                                            onNavigate = { destination ->
                                                vm.onNavigate(destination)
                                            },
                                            onShowLockedSnackbar = { message ->
                                                scope.launch {
                                                    snackbarHostState.showSnackbar(message)
                                                }
                                            }
                                        )
                                    } else {
                                        EmptyContentScreen(
                                            tab = NavigationTab.EXTRA,
                                            isSyncing = uiState.isSyncing,
                                            syncProgressMessage = uiState.syncProgressMessage,
                                            syncErrorMessage = uiState.syncErrorMessage,
                                            onSyncClick = { vm.syncFromWeb() },
                                            onNavigateToSettings = { vm.onNavigate(AppRoute.Settings) }
                                        )
                                    }
                                }
                                NavigationTab.QUIZ -> {
                                    if (uiState.quizTopics.isNotEmpty()) {
                                        QuizScreen(
                                            quizzes = uiState.quizTopics,
                                            onNavigate = { destination ->
                                                vm.onNavigate(destination)
                                            }
                                        )
                                    } else {
                                        EmptyContentScreen(
                                            tab = NavigationTab.QUIZ,
                                            isSyncing = uiState.isSyncing,
                                            syncProgressMessage = uiState.syncProgressMessage,
                                            syncErrorMessage = uiState.syncErrorMessage,
                                            onSyncClick = { vm.syncFromWeb() },
                                            onNavigateToSettings = { vm.onNavigate(AppRoute.Settings) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                    is AppRoute.Settings -> {
                        SettingsScreen(
                            activeSubjectId = uiState.activeSubjectId,
                            onSubjectSelected = { newSubjectId ->
                                vm.onSelectSubject(newSubjectId)
                            },
                            themeMode = uiState.themeMode,
                            onThemeModeChanged = { newThemeMode ->
                                vm.onThemeModeChanged(newThemeMode)
                            },
                            fontScale = uiState.fontScale,
                            onFontScaleChanged = { newScale ->
                                vm.onFontScaleChanged(newScale)
                            },
                            customRepoUrl = uiState.settings.customRepoUrl,
                            onCustomRepoUrlChanged = { newUrl ->
                                vm.onCustomRepoUrlChanged(newUrl)
                            },
                            isSyncing = uiState.isSyncing,
                            syncProgressMessage = uiState.syncProgressMessage,
                            syncErrorMessage = uiState.syncErrorMessage,
                            syncSuccessMessage = uiState.syncSuccessMessage,
                            lastSyncTimestamp = uiState.lastSyncTimestamp,
                            onSyncClicked = { vm.syncFromWeb() },
                            availableSubjects = uiState.availableSubjects,
                            onResetSubjectProgress = {
                                vm.onResetSubjectProgress(uiState.activeSubjectId)
                            },
                            customThemePreset = uiState.customThemePreset,
                            customBackgroundHex = uiState.customBackgroundHex,
                            customTextHex = uiState.customTextHex,
                            onUpdateCustomColors = { bg, text, preset ->
                                vm.onUpdateCustomColors(bg, text, preset)
                            }
                        )
                    }
                    is AppRoute.PathTopics -> {
                        val path = remember(route.pathId, uiState.learningPaths) {
                            uiState.learningPaths.firstOrNull { it.id == route.pathId }
                                ?: vm.getPath(route.pathId)
                        }
                        val topics = remember(route.pathId, uiState.currentSubjectProgress) {
                            vm.getTopicsForPath(
                                pathId = route.pathId,
                                completedIds = uiState.currentSubjectProgress.completedTopicIds,
                                resumeTopicId = uiState.currentSubjectProgress.resumeTopicId
                            )
                        }

                        if (path != null && topics.isNotEmpty()) {
                            PathTopicsScreen(
                                path = path,
                                topics = topics,
                                onBack = {
                                    vm.onNavigateBack()
                                },
                                onTopicClick = { topicId ->
                                    vm.onNavigate(AppRoute.ContentReader(topicId, isExtra = false))
                                }
                            )
                        } else {
                            vm.onNavigate(AppRoute.HomeTabs)
                        }
                    }
                    is AppRoute.ContentReader -> {
                        LaunchedEffect(route.itemId, route.isExtra, uiState.activeSubjectId) {
                            if (!route.isExtra && uiState.activeSubjectId.isNotBlank() && route.itemId.isNotBlank()) {
                                vm.onTopicAccessed(uiState.activeSubjectId, route.itemId)
                            }
                        }
                        val providedTopic = remember(route.itemId, uiState.currentSubjectProgress) {
                            vm.getTopic(route.itemId, uiState.currentSubjectProgress.completedTopicIds)
                        }
                        val providedExtra = remember(route.itemId, uiState.extraItems) {
                            uiState.extraItems.firstOrNull { it.id == route.itemId }
                                ?: vm.getExtraItem(route.itemId)
                        }
                        val providedContent = remember(route.itemId, route.isExtra, uiState.activeSubjectId) {
                            if (route.isExtra) {
                                vm.getExtraContent(uiState.activeSubjectId, route.itemId)
                            } else {
                                vm.getTopicMarkdown(route.itemId)
                            }
                        }

                        val allTopics = remember(uiState.learningPaths) {
                            uiState.learningPaths.flatMap { p ->
                                vm.getTopicsForPath(p.id)
                            }
                        }
                        val currentIdx = if (!route.isExtra) allTopics.indexOfFirst { it.id == route.itemId } else -1
                        val nextTopic = if (currentIdx != -1 && currentIdx < allTopics.lastIndex) {
                            allTopics[currentIdx + 1]
                        } else null

                        ContentReaderScreen(
                            itemId = route.itemId,
                            isExtra = route.isExtra,
                            onBack = {
                                vm.onNavigateBack()
                            },
                            onNavigateToExtra = { extraId ->
                                vm.onNavigate(AppRoute.ContentReader(extraId, isExtra = true))
                            },
                            onNavigateToQuiz = { quizId ->
                                vm.onNavigate(AppRoute.QuizRunner(quizId))
                            },
                            onTopicCompleted = {
                                if (route.isExtra) {
                                    vm.onExtraCompleted(uiState.activeSubjectId, route.itemId)
                                } else {
                                    vm.onTopicCompleted(uiState.activeSubjectId, route.itemId, nextTopic?.id)
                                }
                            },
                            nextTopicId = nextTopic?.id,
                            nextTopicTitle = nextTopic?.title,
                            onNavigateToNextTopic = { nextId ->
                                vm.onNavigate(AppRoute.ContentReader(nextId, isExtra = false))
                            },
                            providedTopic = providedTopic,
                            providedExtra = providedExtra,
                            providedContent = providedContent,
                            themeMode = uiState.themeMode,
                            onThemeModeChanged = { mode -> vm.onThemeModeChanged(mode) },
                            fontScale = uiState.fontScale,
                            onFontScaleChanged = { scale -> vm.onFontScaleChanged(scale) },
                            customThemePreset = uiState.customThemePreset,
                            customBackgroundHex = uiState.customBackgroundHex,
                            customTextHex = uiState.customTextHex,
                            onUpdateCustomColors = { bg, text, preset ->
                                vm.onUpdateCustomColors(bg, text, preset)
                            }
                        )
                    }
                    is AppRoute.QuizRunner -> {
                        val quiz = remember(route.quizId, uiState.quizTopics) {
                            uiState.quizTopics.firstOrNull { it.id == route.quizId }
                                ?: vm.getQuiz(route.quizId)
                        }
                        val questions = remember(route.quizId) {
                            vm.getQuizQuestions(route.quizId)
                        }

                        if (quiz != null && questions.isNotEmpty()) {
                            QuizRunnerScreen(
                                quiz = quiz,
                                questions = questions,
                                onBack = {
                                    vm.onNavigateBack()
                                },
                                onSubmit = { quizId, selectedAnswers ->
                                    val qList = vm.getQuizQuestions(quizId)
                                    val correctCount = qList.indices.count { idx ->
                                        selectedAnswers[idx] == qList[idx].correctOptionIndex
                                    }
                                    val scorePercent = if (qList.isNotEmpty()) (correctCount * 100) / qList.size else 0
                                    vm.onQuizSubmitted(
                                        subjectId = uiState.activeSubjectId,
                                        quizId = quizId,
                                        scorePercent = scorePercent,
                                        passThreshold = quiz.passPercentage
                                    )
                                    vm.onNavigate(AppRoute.QuizResultReview(quizId, selectedAnswers))
                                }
                            )
                        } else {
                            vm.onNavigate(AppRoute.HomeTabs)
                        }
                    }
                    is AppRoute.QuizResultReview -> {
                        val quiz = remember(route.quizId, uiState.quizTopics) {
                            uiState.quizTopics.firstOrNull { it.id == route.quizId }
                                ?: vm.getQuiz(route.quizId)
                        }
                        val questions = remember(route.quizId) {
                            vm.getQuizQuestions(route.quizId)
                        }

                        QuizResultReviewScreen(
                            quizId = route.quizId,
                            selectedAnswers = route.selectedAnswers,
                            quiz = quiz,
                            questions = questions,
                            onDone = {
                                vm.onNavigateBack()
                            },
                            onRetry = {
                                vm.onNavigate(AppRoute.QuizRunner(route.quizId))
                            }
                        )
                    }
                }
            }
        }
    }
}
