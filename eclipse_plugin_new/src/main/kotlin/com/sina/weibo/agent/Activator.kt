// SPDX-FileCopyrightText: 2025 Weibo, Inc.
//
// SPDX-License-Identifier: Apache-2.0

package com.sina.weibo.agent

import com.sina.weibo.agent.workspace.WorkspaceFileChangeManager
import org.eclipse.ui.plugin.AbstractUIPlugin
import org.osgi.framework.BundleContext

/**
 * The activator class controls the plug-in life cycle
 */
class Activator : AbstractUIPlugin() {

    private var workspaceFileChangeManager: WorkspaceFileChangeManager? = null

    override fun start(context: BundleContext) {
        super.start(context)
        plugin = this
        workspaceFileChangeManager = WorkspaceFileChangeManager()
    }

    override fun stop(context: BundleContext) {
        workspaceFileChangeManager?.dispose()
        plugin = null
        super.stop(context)
    }

    companion object {
        @JvmStatic
        var plugin: Activator? = null
            private set
    }
}
