// SPDX-FileCopyrightText: 2025 Weibo, Inc.
//
// SPDX-License-Identifier: Apache-2.0

package com.sina.weibo.agent.eclipse.neo.core

// import com.intellij.openapi.diagnostic.Logger
// import com.intellij.openapi.project.Project
import java.io.IOException
import java.net.StandardProtocolFamily
import java.net.UnixDomainSocketAddress
import java.nio.channels.ServerSocketChannel
import java.nio.channels.SocketChannel
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.ConcurrentHashMap
import kotlin.concurrent.thread

    // ExtensionUnixDomainSocketServer is responsible for communication between extension process and IDEA plugin process via Unix Domain Socket
class ExtensionUnixDomainSocketServer : ISocketServer {
    private fun logger(level: String, message: String, e: Exception? = null) {
        println("[$level] ExtensionUnixDomainSocketServer: $message")
        e?.printStackTrace()
    }

    // UDS server channel
    private var udsServerChannel: ServerSocketChannel? = null
    // UDS socket file path
    private var udsSocketPath: Path? = null
    // Mapping of client connections and managers
    private val clientManagers = ConcurrentHashMap<SocketChannel, Any>() // TODO: Replace Any with ExtensionHostManager
    // Server listening thread
    private var serverThread: Thread? = null
    // Current project path
    private var projectPath: String = ""

    // lateinit var project: Project

    @Volatile private var isRunning = false // Server running state

    // Start UDS server, return socket file path
    override fun start(projectPath: String): String? {
        if (isRunning) {
            logger("INFO", "UDS server is already running")
            return udsSocketPath?.toString()
        }
        this.projectPath = projectPath
        return startUds()
    }

    // Actual logic to start UDS server
    private fun startUds(): String? {
        try {
            val sockPath = createSocketFile() // Create socket file
            val udsAddr = UnixDomainSocketAddress.of(sockPath)
            udsServerChannel = ServerSocketChannel.open(StandardProtocolFamily.UNIX)
            udsServerChannel!!.bind(udsAddr)
            udsSocketPath = sockPath
            isRunning = true
            logger("INFO", "[UDS] Listening on: $sockPath")
            // Start listening thread, asynchronously accept client connections
            serverThread =
                    thread(start = true, name = "ExtensionUDSSocketServer") {
                        acceptUdsConnections()
                    }
            return sockPath.toString()
        } catch (e: Exception) {
            logger("ERROR", "[UDS] Failed to start server", e)
            stop()
            return null
        }
    }

    // Stop UDS server, release resources
    override fun stop() {
        if (!isRunning) return
        isRunning = false
        logger("INFO", "Stopping UDS socket server")
        // Close all client connections
        clientManagers.forEach { (_, manager) ->
            try {
                // (manager as ExtensionHostManager).dispose()
            } catch (e: Exception) {
                logger("WARN", "Failed to dispose client manager", e)
            }
        }
        clientManagers.clear()
        try {
            udsServerChannel?.close()
        } catch (e: Exception) {
            logger("WARN", "Failed to close UDS server channel", e)
        }
        try {
            udsSocketPath?.let { Files.deleteIfExists(it) }
        } catch (e: Exception) {
            logger("WARN", "Failed to delete UDS socket file", e)
        }
        // Thread and channel cleanup
        serverThread?.interrupt()
        serverThread = null
        udsServerChannel = null
        udsSocketPath = null
        logger("INFO", "UDS socket server stopped")
    }

    override fun isRunning(): Boolean = isRunning
    override fun dispose() {
        stop()
    }

    // Listen and accept UDS client connections
    private fun acceptUdsConnections() {
        val server = udsServerChannel ?: return
        logger("INFO", "[UDS] Waiting for connections..., tid: ${Thread.currentThread().id}")
        while (isRunning && !Thread.currentThread().isInterrupted) {
            try {
                val clientChannel = server.accept() // Block and wait for new connection
                logger("INFO", "[UDS] New client connected")
                // val manager = ExtensionHostManager(clientChannel, projectPath,project)
                // clientManagers[clientChannel] = manager
                // handleClient(clientChannel, manager) // Start client handler thread
            } catch (e: Exception) {
                if (isRunning) {
                    logger("ERROR", "[UDS] Accept failed, will retry in 1s", e)
                    Thread.sleep(1000)
                } else {
                    logger("INFO", "[UDS] Accept loop exiting (server stopped)")
                    break
                }
            }
        }
        logger("INFO", "[UDS] Accept loop terminated.")
    }

    // Handle single client connection, responsible for heartbeat check and resource release
    private fun handleClient(clientChannel: SocketChannel, manager: Any /* ExtensionHostManager */) {
        // ... (Implementation will be added once ExtensionHostManager is ported)
    }

    // Create temporary socket file, ensure uniqueness
    private fun createSocketFile(): Path {
        val tmpDir = java.nio.file.Paths.get("/tmp")
        val sockPath = Files.createTempFile(tmpDir, "roo-cline-idea-extension-ipc-", ".sock")
        Files.deleteIfExists(sockPath) // Ensure it does not exist
        return sockPath
    }
}
