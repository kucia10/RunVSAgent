package com.sina.weibo.agent.eclipse.wow.plugin

import com.sina.weibo.agent.eclipse.wow.core.ExtensionProcessManager
import com.sina.weibo.agent.eclipse.wow.core.ExtensionSocketServer
import com.sina.weibo.agent.eclipse.wow.core.ExtensionUnixDomainSocketServer
import com.sina.weibo.agent.eclipse.wow.core.ISocketServer
import com.sina.weibo.agent.eclipse.wow.editor.EditorListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.eclipse.core.runtime.ILog
import org.eclipse.core.runtime.Platform
import org.eclipse.ui.IStartup
import org.eclipse.ui.PlatformUI
import java.util.concurrent.CompletableFuture

class Startup : IStartup {
    companion object {
        private val LOG: ILog = Platform.getLog(Activator.plugin?.bundle)
    }

    override fun earlyStartup() {
        LOG.info("Initializing RunVSAgent-wow plugin...")

        // Add the editor listener
        PlatformUI.getWorkbench().activeWorkbenchWindow.partService.addPartListener(EditorListener())

        // Placeholder for the logic from WecoderPlugin.runActivity
        // In a real scenario, we would translate the logic for:
        // - Getting project information
        // - Initializing ExtensionConfigurationManager
        // - Initializing ExtensionManager
        // - Setting up background monitoring

        // For now, we'll just log a message
        LOG.info("RunVSAgent-wow plugin startup logic goes here.")

        // Initialize and start the plugin service
        val pluginService = WecoderPluginService.getInstance()
        pluginService.initialize()
    }
}

// Main plugin service
class WecoderPluginService {
    private val LOG: ILog = Platform.getLog(Activator.plugin?.bundle)

    @Volatile
    private var isInitialized = false
    private val initializationComplete = CompletableFuture<Boolean>()
    private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val socketServer = ExtensionSocketServer()
    private val udsSocketServer = ExtensionUnixDomainSocketServer()
    private val processManager = ExtensionProcessManager()

    companion object {
        private var instance: WecoderPluginService? = null

        @JvmStatic
        fun getInstance(): WecoderPluginService {
            if (instance == null) {
                instance = WecoderPluginService()
            }
            return instance!!
        }
    }

    fun initialize() {
        if (isInitialized) {
            LOG.info("WecoderPluginService already initialized")
            return
        }

        LOG.info("Initializing WecoderPluginService...")

        coroutineScope.launch {
            try {
                // In a real scenario, we would get the project path from the workspace
                val projectPath = ""

                val server: ISocketServer = if (System.getProperty("os.name").lowercase().contains("win")) {
                    socketServer
                } else {
                    udsSocketServer
                }

                val portOrPath = server.start(projectPath)
                if (portOrPath.isBlank()) {
                    LOG.error("Failed to start socket server")
                    initializationComplete.complete(false)
                    return@launch
                }

                LOG.info("Socket server started on: $portOrPath")

                if (!processManager.start(portOrPath)) {
                    LOG.error("Failed to start extension process")
                    server.stop()
                    initializationComplete.complete(false)
                    return@launch
                }

                isInitialized = true
                initializationComplete.complete(true)
                LOG.info("WecoderPluginService initialization completed")

            } catch (e: Exception) {
                LOG.error("Error during WecoderPluginService initialization", e)
                cleanup()
                initializationComplete.complete(false)
            }
        }
    }

    private fun cleanup() {
        try {
            processManager.stop()
        } catch (e: Exception) {
            LOG.error("Error stopping process manager", e)
        }
        try {
            socketServer.stop()
            udsSocketServer.stop()
        } catch (e: Exception) {
            LOG.error("Error stopping socket server", e)
        }
        isInitialized = false
    }

    fun dispose() {
        if (!isInitialized) {
            return
        }
        LOG.info("Disposing WecoderPluginService")
        coroutineScope.launch {
            cleanup()
        }
        LOG.info("WecoderPluginService disposed")
    }
}
