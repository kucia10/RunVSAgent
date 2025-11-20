package com.sina.weibo.agent.eclipse.wow.core

import com.sina.weibo.agent.eclipse.wow.plugin.Activator
import org.eclipse.core.runtime.ILog
import org.eclipse.core.runtime.Platform
import java.io.Closeable

class ExtensionHostManager(private val connection: Closeable, private val projectPath: String) {
    companion object {
        private val LOG: ILog = Platform.getLog(Activator.plugin?.bundle)
    }

    fun start() {
        LOG.info("Starting ExtensionHostManager...")
        // Placeholder for the communication logic
    }

    fun dispose() {
        LOG.info("Disposing ExtensionHostManager...")
        try {
            connection.close()
        } catch (e: Exception) {
            LOG.error("Error closing connection", e)
        }
    }

    fun getResponsiveState(): String? {
        // Placeholder
        return "responsive"
    }
}
