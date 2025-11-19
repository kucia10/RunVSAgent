// SPDX-FileCopyrightText: 2025 Weibo, Inc.
//
// SPDX-License-Identifier: Apache-2.0

package com.sina.weibo.agent.util

import org.eclipse.core.runtime.IStatus
import org.eclipse.core.runtime.Status
import org.eclipse.ui.statushandlers.StatusManager
import org.osgi.framework.FrameworkUtil

/**
 * Notification utility class
 * Used to encapsulate notification functionality for the plugin
 */
object NotificationUtil {

    private val BUNDLE_ID = FrameworkUtil.getBundle(NotificationUtil::class.java).symbolicName

    /**
     * Show error notification
     * @param title Notification title
     * @param content Notification content
     */
    fun showError(title: String, content: String) {
        showNotification(title, content, IStatus.ERROR)
    }

    /**
     * Show warning notification
     * @param title Notification title
     * @param content Notification content
     */
    fun showWarning(title: String, content: String) {
        showNotification(title, content, IStatus.WARNING)
    }

    /**
     * Show info notification
     * @param title Notification title
     * @param content Notification content
     */
    fun showInfo(title: String, content: String) {
        showNotification(title, content, IStatus.INFO)
    }

    /**
     * Show notification
     * @param title Notification title
     * @param content Notification content
     * @param severity Notification severity
     */
    private fun showNotification(title: String, content: String, severity: Int) {
        val status = Status(severity, BUNDLE_ID, content)
        StatusManager.getManager().handle(status, StatusManager.SHOW)
    }
}
