// SPDX-FileCopyrightText: 2025 Weibo, Inc.
//
// SPDX-License-Identifier: Apache-2.0

package com.sina.weibo.agent.extensions.ui.contextmenu

import org.eclipse.ui.actions.ActionGroup
import org.eclipse.jface.action.IMenuManager

/**
 * Dynamic extension context menu action group.
 * This class manages the dynamic context menu actions that appear in the right-click menu
 * when text is selected, based on the current active extension.
 */
class DynamicExtensionContextMenuGroup : ActionGroup() {

    /**
     * Manager that provides the current extension's context menu actions.
     */
    private var contextMenuManager: DynamicContextMenuManager? = null

    override fun fillContextMenu(menu: IMenuManager) {
        // TODO: Implement logic to check for text selection in the active editor.
        val hasSelection = true

        if (hasSelection) {
            loadDynamicContextMenuActions(menu)
        }
    }

    private fun loadDynamicContextMenuActions(menu: IMenuManager) {
        // TODO: Implement service lookup in Eclipse.
        if (contextMenuManager == null) {
            try {
                // contextMenuManager = DynamicContextMenuManager.getInstance(project)
                // contextMenuManager?.initialize()
            } catch (e: Exception) {
                return
            }
        }

        val actions = contextMenuManager?.getContextMenuActions() ?: emptyList()
        actions.forEach { action ->
            // menu.add(action)
        }
    }
}
