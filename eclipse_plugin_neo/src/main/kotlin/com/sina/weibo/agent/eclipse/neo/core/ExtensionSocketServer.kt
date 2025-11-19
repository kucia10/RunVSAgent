// SPDX-FileCopyrightText: 2025 Weibo, Inc.
//
// SPDX-License-Identifier: Apache-2.0

package com.sina.weibo.agent.eclipse.neo.core

// import com.intellij.openapi.Disposable
// import com.intellij.openapi.diagnostic.Logger
// import com.intellij.openapi.project.Project
import java.net.ServerSocket
import java.net.Socket
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap
import kotlin.concurrent.thread

/**
 * Extension process Socket server
 * Used to establish communication with extension process
 */
interface ISocketServer {
    fun start(projectPath: String = ""): Any?
    fun stop()
    fun isRunning(): Boolean
    fun dispose()
}

class ExtensionSocketServer() : ISocketServer {
    private fun logger(level: String, message: String, e: Exception? = null) {
        println("[$level] ExtensionSocketServer: $message")
        e?.printStackTrace()
    }

    // Server socket
    private var serverSocket: ServerSocket? = null

    // Connected client managers
    private val clientManagers = ConcurrentHashMap<Socket, Any>() // TODO: Replace Any with ExtensionHostManager

    // Server thread
    private var serverThread: Thread? = null

    // Current project path
    private var projectPath: String = ""

    // Whether running
    @Volatile
    private var isRunning = false

    // lateinit var project: Project

    /**
     * Start Socket server
     * @param projectPath Current project path
     * @return Server port, -1 if failed
     */
    override fun start(projectPath: String): Int {
        if (isRunning) {
            logger("INFO", "Socket server is already running")
            return serverSocket?.localPort ?: -1
        }

        this.projectPath = projectPath

        try {
            // Use 0 to indicate random port assignment
            serverSocket = ServerSocket(0)
            val port = serverSocket?.localPort ?: -1

            if (port <= 0) {
                logger("ERROR", "Failed to get valid port for socket server")
                return -1
            }

            isRunning = true
            logger("INFO", "Starting socket server on port: $port")

            // Start the thread to accept connections
            serverThread = thread(start = true, name = "ExtensionSocketServer") {
                acceptConnections()
            }

            return port
        } catch (e: Exception) {
            logger("ERROR", "Failed to start socket server", e)
            stop()
            return -1
        }
    }

    /**
     * Stop Socket server
     */
    override fun stop() {
        if (!isRunning) {
            return
        }

        isRunning = false
        logger("INFO", "Stopping socket server")

        // Close all client managers
        clientManagers.forEach { (_, manager) ->
            try {
                // (manager as ExtensionHostManager).dispose()
            } catch (e: Exception) {
                logger("WARN", "Failed to dispose client manager", e)
            }
        }
        clientManagers.clear()

        // Close the server
        try {
            serverSocket?.close()
        } catch (e: IOException) {
            logger("WARN", "Failed to close server socket", e)
        }

        // Interrupt the server thread
        serverThread?.interrupt()
        serverThread = null
        serverSocket = null

        logger("INFO", "Socket server stopped")
    }

    /**
     * Thread function for accepting connections
     */
    private fun acceptConnections() {
        val server = serverSocket ?: return

        logger("INFO", "Socket server started, waiting for connections..., tid: ${Thread.currentThread().id}")

        while (isRunning && !Thread.currentThread().isInterrupted) {
            try {
                val clientSocket = server.accept()
                logger("INFO", "New client connected from: ${clientSocket.inetAddress.hostAddress}")

                clientSocket.tcpNoDelay = true // Set no delay

                // Create extension host manager
                // val manager = ExtensionHostManager(clientSocket, projectPath,project)
                // clientManagers[clientSocket] = manager

                // handleClient(clientSocket, manager)
            } catch (e: IOException) {
                if (isRunning) {
                    logger("ERROR", "Error accepting client connection", e)
                } else {
                    // IOException is thrown when ServerSocket is closed, this is normal
                    logger("INFO", "Socket server closed")
                    break
                }
            } catch (e: InterruptedException) {
                // Thread interrupted, this is normal
                logger("INFO", "Socket server thread interrupted")
                break
            } catch (e: Exception) {
                logger("ERROR", "Unexpected error in accept loop", e)
                if (isRunning) {
                    try {
                        // Retry after short delay
                        Thread.sleep(1000)
                    } catch (ie: InterruptedException) {
                        // Thread interrupted, server is shutting down
                        logger("INFO", "Socket server thread interrupted during sleep")
                        break
                    }
                }
            }
        }

        logger("INFO", "Socket accept loop terminated")
    }

    /**
     * Handle client connection
     */
    private fun handleClient(clientSocket: Socket, manager: Any /* ExtensionHostManager */) {
        // ... (Implementation will be added once ExtensionHostManager is ported)
    }

    /**
     * Get current port
     */
    fun getPort(): Int {
        return serverSocket?.localPort ?: -1
    }

    /**
     * Whether running
     */
    override fun isRunning(): Boolean {
        return isRunning
    }

    /**
     * Resource cleanup
     */
    override fun dispose() {
        stop()
    }

    /**
     * Connect to debug host
     * @param host Debug host address
     * @param port Debug host port
     * @return Whether connection is successful
     */
    fun connectToDebugHost(host: String, port: Int): Boolean {
        if (isRunning) {
            logger("INFO", "Socket server is already running, stopping first")
            stop()
        }

        try {
            logger("INFO", "Connecting to debug host at $host:$port")

            // Directly connect to the specified address and port
            val clientSocket = Socket(host, port)
            clientSocket.tcpNoDelay = true // Set no delay

            isRunning = true

            // Create extension host manager
            // val manager = ExtensionHostManager(clientSocket, projectPath,project)
            // clientManagers[clientSocket] = manager

            // Start connection handling in background thread
            // thread(start = true, name = "DebugHostHandler") {
            //     handleClient(clientSocket, manager)
            // }

            logger("INFO", "Successfully connected to debug host at $host:$port")
            return true
        } catch (e: Exception) {
            logger("ERROR", "Failed to connect to debug host at $host:$port", e)
            stop()
            return false
        }
    }
}
