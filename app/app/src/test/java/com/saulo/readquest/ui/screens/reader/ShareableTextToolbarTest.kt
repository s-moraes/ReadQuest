package com.saulo.readquest.ui.screens.reader

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class ShareableTextToolbarTest {

    @Test
    fun testMenuIdsAreUnique() {
        val ids = setOf(
            ShareableTextToolbar.MENU_COPY,
            ShareableTextToolbar.MENU_SHARE,
            ShareableTextToolbar.MENU_CUT,
            ShareableTextToolbar.MENU_PASTE,
            ShareableTextToolbar.MENU_SELECT_ALL
        )
        assertEquals(5, ids.size)
    }

    @Test
    fun testShareMenuIdDiffersFromCopy() {
        assertNotEquals(ShareableTextToolbar.MENU_COPY, ShareableTextToolbar.MENU_SHARE)
    }
}
