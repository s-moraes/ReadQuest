package com.saulo.readquest.ui.screens.common

import com.saulo.readquest.ui.model.TopicItemUiModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CommonComponentsTest {

    @Test
    fun testTopicItemUiModelStatusFlags() {
        val completedTopic = TopicItemUiModel(
            id = "topic_1",
            pathId = "path_1",
            title = "Test Topic",
            topicIndex = 1,
            isCompleted = true,
            isCurrentResume = false,
            markdownContent = "Content"
        )
        assertTrue(completedTopic.isCompleted)
        assertFalse(completedTopic.isCurrentResume)

        val resumeTopic = TopicItemUiModel(
            id = "topic_2",
            pathId = "path_1",
            title = "Test Topic 2",
            topicIndex = 2,
            isCompleted = false,
            isCurrentResume = true,
            markdownContent = "Content"
        )
        assertFalse(resumeTopic.isCompleted)
        assertTrue(resumeTopic.isCurrentResume)

        val unaccessedTopic = TopicItemUiModel(
            id = "topic_3",
            pathId = "path_1",
            title = "Test Topic 3",
            topicIndex = 3,
            isCompleted = false,
            isCurrentResume = false,
            markdownContent = "Content"
        )
        assertFalse(unaccessedTopic.isCompleted)
        assertFalse(unaccessedTopic.isCurrentResume)
        assertEquals(3, unaccessedTopic.topicIndex)
    }
}
