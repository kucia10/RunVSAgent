package com.sina.weibo.agent.eclipse.wow.util

import com.sina.weibo.agent.eclipse.wow.plugin.Activator
import org.eclipse.core.runtime.ILog
import org.eclipse.core.runtime.Platform

object NotificationUtil {
    private val LOG: ILog = Platform.getLog(Activator.plugin?.bundle)

    fun showError(title: String, message: String) {
        LOG.error("$title: $message")
        // In a real scenario, this would show a UI notification
    }
}

object PluginResourceUtil {
    fun getResourcePath(pluginId: String, path: String): String? {
        // Placeholder
        return null
    }
}
