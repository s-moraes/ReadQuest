package com.saulo.readquest.ui.screens.common

import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Test

class BottomActionBarDefaultsTest {

    @Test
    fun testBottomActionBarDefaultsTokens() {
        assertEquals(40.dp, BottomActionBarDefaults.ButtonHeight)
        assertEquals(7.dp, BottomActionBarDefaults.VerticalPadding)
        assertEquals(16.dp, BottomActionBarDefaults.HorizontalPadding)
        assertEquals(10.dp, BottomActionBarDefaults.ItemSpacing)
        assertEquals(10.dp, BottomActionBarDefaults.ButtonCornerRadius)
        assertEquals(4.dp, BottomActionBarDefaults.TonalElevation)
        assertEquals(4.dp, BottomActionBarDefaults.ShadowElevation)
    }
}
