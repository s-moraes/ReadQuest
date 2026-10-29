package com.saulo.readquest.ui.screens.reader

import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Rect as AndroidRect
import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.platform.TextToolbar
import androidx.compose.ui.platform.TextToolbarStatus
import com.saulo.readquest.R

/**
 * Custom [TextToolbar] that exposes standard text actions (Copy, Cut, Paste, Select All)
 * alongside a first-class "Share" action to share selected text across apps.
 */
class ShareableTextToolbar(
    private val view: View,
    private val context: Context
) : TextToolbar {

    private var actionMode: ActionMode? = null
    override var status: TextToolbarStatus = TextToolbarStatus.Hidden
        private set

    private var currentRect: Rect = Rect.Zero
    private var onCopy: (() -> Unit)? = null
    private var onSelectAll: (() -> Unit)? = null
    private var onCut: (() -> Unit)? = null
    private var onPaste: (() -> Unit)? = null

    private val callback = object : ActionMode.Callback2() {
        override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
            if (mode == null || menu == null) return false

            onCopy?.let {
                menu.add(Menu.NONE, MENU_COPY, 0, android.R.string.copy)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
            }

            // Share action is available when text can be copied
            onCopy?.let {
                val shareTitle = runCatching { context.getString(R.string.share) }.getOrDefault("Share")
                menu.add(Menu.NONE, MENU_SHARE, 1, shareTitle)
                    .setIcon(android.R.drawable.ic_menu_share)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
            }

            onCut?.let {
                menu.add(Menu.NONE, MENU_CUT, 2, android.R.string.cut)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM)
            }

            onPaste?.let {
                menu.add(Menu.NONE, MENU_PASTE, 3, android.R.string.paste)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM)
            }

            onSelectAll?.let {
                menu.add(Menu.NONE, MENU_SELECT_ALL, 4, android.R.string.selectAll)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM)
            }

            return true
        }

        override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
            if (mode == null || menu == null) return false
            updateMenuItems(menu)
            return true
        }

        override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
            when (item?.itemId) {
                MENU_COPY -> {
                    onCopy?.invoke()
                    mode?.finish()
                    return true
                }
                MENU_SHARE -> {
                    onCopy?.invoke()
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                    val textToShare = clipboard?.primaryClip?.let { clip ->
                        if (clip.itemCount > 0) clip.getItemAt(0)?.text?.toString() else null
                    }
                    if (!textToShare.isNullOrBlank()) {
                        val shareIntent = createShareTextIntent(textToShare)
                        context.startActivity(shareIntent)
                    }
                    mode?.finish()
                    return true
                }
                MENU_CUT -> {
                    onCut?.invoke()
                    mode?.finish()
                    return true
                }
                MENU_PASTE -> {
                    onPaste?.invoke()
                    mode?.finish()
                    return true
                }
                MENU_SELECT_ALL -> {
                    onSelectAll?.invoke()
                    return true
                }
            }
            return false
        }

        override fun onDestroyActionMode(mode: ActionMode?) {
            actionMode = null
            status = TextToolbarStatus.Hidden
        }

        override fun onGetContentRect(mode: ActionMode?, view: View?, outRect: AndroidRect?) {
            outRect?.set(
                currentRect.left.toInt(),
                currentRect.top.toInt(),
                currentRect.right.toInt(),
                currentRect.bottom.toInt()
            )
        }
    }

    private fun updateMenuItems(menu: Menu) {
        val shareTitle = runCatching { context.getString(R.string.share) }.getOrDefault("Share")
        addOrRemoveMenuItem(menu, MENU_COPY, 0, android.R.string.copy, null, MenuItem.SHOW_AS_ACTION_ALWAYS, onCopy)
        addOrRemoveMenuItem(menu, MENU_SHARE, 1, 0, shareTitle, MenuItem.SHOW_AS_ACTION_ALWAYS, onCopy)
        addOrRemoveMenuItem(menu, MENU_CUT, 2, android.R.string.cut, null, MenuItem.SHOW_AS_ACTION_IF_ROOM, onCut)
        addOrRemoveMenuItem(menu, MENU_PASTE, 3, android.R.string.paste, null, MenuItem.SHOW_AS_ACTION_IF_ROOM, onPaste)
        addOrRemoveMenuItem(menu, MENU_SELECT_ALL, 4, android.R.string.selectAll, null, MenuItem.SHOW_AS_ACTION_IF_ROOM, onSelectAll)
    }

    private fun addOrRemoveMenuItem(
        menu: Menu,
        itemId: Int,
        order: Int,
        titleRes: Int,
        titleStr: String?,
        showAsAction: Int,
        callback: (() -> Unit)?
    ) {
        val existingItem = menu.findItem(itemId)
        if (callback != null) {
            if (existingItem == null) {
                val item = if (titleStr != null) {
                    menu.add(Menu.NONE, itemId, order, titleStr)
                } else {
                    menu.add(Menu.NONE, itemId, order, titleRes)
                }
                if (itemId == MENU_SHARE) {
                    item.setIcon(android.R.drawable.ic_menu_share)
                }
                item.setShowAsAction(showAsAction)
            }
        } else {
            if (existingItem != null) {
                menu.removeItem(itemId)
            }
        }
    }

    override fun showMenu(
        rect: Rect,
        onCopyRequested: (() -> Unit)?,
        onPasteRequested: (() -> Unit)?,
        onCutRequested: (() -> Unit)?,
        onSelectAllRequested: (() -> Unit)?
    ) {
        currentRect = rect
        onCopy = onCopyRequested
        onPaste = onPasteRequested
        onCut = onCutRequested
        onSelectAll = onSelectAllRequested

        if (actionMode == null) {
            status = TextToolbarStatus.Shown
            actionMode = view.startActionMode(callback, ActionMode.TYPE_FLOATING)
        } else {
            actionMode?.invalidateContentRect()
            actionMode?.invalidate()
        }
    }

    override fun showMenu(
        rect: Rect,
        onCopyRequested: (() -> Unit)?,
        onPasteRequested: (() -> Unit)?,
        onCutRequested: (() -> Unit)?,
        onSelectAllRequested: (() -> Unit)?,
        onAutofillRequested: (() -> Unit)?
    ) {
        showMenu(
            rect = rect,
            onCopyRequested = onCopyRequested,
            onPasteRequested = onPasteRequested,
            onCutRequested = onCutRequested,
            onSelectAllRequested = onSelectAllRequested
        )
    }

    override fun hide() {
        status = TextToolbarStatus.Hidden
        actionMode?.finish()
        actionMode = null
    }

    companion object {
        const val MENU_COPY = 101
        const val MENU_SHARE = 102
        const val MENU_CUT = 103
        const val MENU_PASTE = 104
        const val MENU_SELECT_ALL = 105

        /**
         * Helper to build an [Intent.ACTION_SEND] chooser intent for selected text sharing.
         */
        fun createShareTextIntent(text: String): Intent {
            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, text)
            }
            return Intent.createChooser(sendIntent, null).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        }
    }
}
