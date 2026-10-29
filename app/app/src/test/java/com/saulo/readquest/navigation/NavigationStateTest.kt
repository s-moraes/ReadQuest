package com.saulo.readquest.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.Quiz
import com.saulo.readquest.ui.navigation.AppRoute
import com.saulo.readquest.ui.navigation.NavigationTab
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NavigationStateTest {

    @Test
    fun navigationTab_hasCorrectLabels() {
        assertEquals("Read", NavigationTab.LEARN.label)
        assertEquals("Extra", NavigationTab.EXTRA.label)
        assertEquals("Quiz", NavigationTab.QUIZ.label)
    }

    @Test
    fun navigationTab_hasCorrectCombination1Icons() {
        assertEquals(Icons.Outlined.AutoStories.name, NavigationTab.LEARN.unselectedIcon.name)
        assertEquals(Icons.Filled.AutoStories.name, NavigationTab.LEARN.selectedIcon.name)
        assertEquals(NavigationTab.LEARN.unselectedIcon, NavigationTab.LEARN.icon)

        assertEquals(Icons.Outlined.Lightbulb.name, NavigationTab.EXTRA.unselectedIcon.name)
        assertEquals(Icons.Filled.Lightbulb.name, NavigationTab.EXTRA.selectedIcon.name)
        assertEquals(NavigationTab.EXTRA.unselectedIcon, NavigationTab.EXTRA.icon)

        assertEquals(Icons.Outlined.Quiz.name, NavigationTab.QUIZ.unselectedIcon.name)
        assertEquals(Icons.Filled.Quiz.name, NavigationTab.QUIZ.selectedIcon.name)
        assertEquals(NavigationTab.QUIZ.unselectedIcon, NavigationTab.QUIZ.icon)
    }

    @Test
    fun appRoutes_instantiateCorrectly() {
        val home = AppRoute.HomeTabs
        val settings = AppRoute.Settings
        val path = AppRoute.PathTopics("path-sd-1")
        val content = AppRoute.ContentReader("extra-sd-1", isExtra = true)
        val quiz = AppRoute.QuizRunner("quiz-sd-1")

        assertEquals("path-sd-1", path.pathId)
        assertEquals("extra-sd-1", content.itemId)
        assertTrue(content.isExtra)
        assertEquals("quiz-sd-1", quiz.quizId)
    }
}
