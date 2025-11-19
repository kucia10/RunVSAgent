package com.sina.weibo.agent.eclipse.neo

import com.sina.weibo.agent.eclipse.neo.core.ExtensionProcessManager

class PluginService {

    private var isInitialized = false
    private val processManager = ExtensionProcessManager()

    fun initialize() {
        if (isInitialized) {
            println("PluginService is already initialized.")
            return
        }

        println("Initializing PluginService...")
        // For now, we'll start the process without a socket, for testing purposes.
        // This will be replaced with actual socket logic later.
        val success = processManager.start(null)
        if (!success) {
            println("Failed to start extension process.")
            return
        }

        isInitialized = true
        println("PluginService initialized successfully.")
    }

    fun dispose() {
        if (!isInitialized) {
            return
        }

        println("Disposing PluginService...")
        processManager.dispose()

        isInitialized = false
        println("PluginService disposed.")
    }
}
