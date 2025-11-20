package com.sina.weibo.agent.eclipse.wow.core

import com.sina.weibo.agent.eclipse.wow.plugin.Activator
import com.sina.weibo.agent.eclipse.wow.util.*
import org.eclipse.core.runtime.ILog
import org.eclipse.core.runtime.Platform
//import org.newsclub.net.unix.AFUNIXServerSocket
//import org.newsclub.net.unix.AFUNIXSocketAddress
import java.io.File
import java.io.IOException
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

class ExtensionSocketServer {
    private val logger: ILog = Platform.getLog(Activator.plugin?.bundle)
    private var serverSocket: ServerSocket? = null
    private val clientManagers = ConcurrentHashMap<Socket, ExtensionHostManager>()
    private var serverThread: Thread? = null
    private var projectPath: String = ""
    @Volatile
    private var isRunning = false

    fun start(projectPath: String): Int {
        if (isRunning) {
            logger.info("Socket server is already running")
            return serverSocket?.localPort ?: -1
        }
        this.projectPath = projectPath
        try {
            serverSocket = ServerSocket(0)
            val port = serverSocket?.localPort ?: -1
            if (port <= 0) {
                logger.error("Failed to get valid port for socket server")
                return -1
            }
            isRunning = true
            logger.info("Starting socket server on port: $port")
            serverThread = thread(start = true, name = "ExtensionSocketServer") {
                acceptConnections()
            }
            return port
        } catch (e: Exception) {
            logger.error("Failed to start socket server", e)
            stop()
            return -1
        }
    }

    fun stop() {
        if (!isRunning) {
            return
        }
        isRunning = false
        logger.info("Stopping socket server")
        clientManagers.forEach { (_, manager) ->
            try {
                manager.dispose()
            } catch (e: Exception) {
                logger.warn("Failed to dispose client manager", e)
            }
        }
        clientManagers.clear()
        try {
            serverSocket?.close()
        } catch (e: IOException) {
            logger.warn("Failed to close server socket", e)
        }
        serverThread?.interrupt()
        serverThread = null
        serverSocket = null
        logger.info("Socket server stopped")
    }

    private fun acceptConnections() {
        val server = serverSocket ?: return
        logger.info("Socket server started, waiting for connections..., tid: ${Thread.currentThread().id}")
        while (isRunning && !Thread.currentThread().isInterrupted) {
            try {
                val clientSocket = server.accept()
                logger.info("New client connected from: ${clientSocket.inetAddress.hostAddress}")
                clientSocket.tcpNoDelay = true
                val manager = ExtensionHostManager(clientSocket, projectPath)
                clientManagers[clientSocket] = manager
                handleClient(clientSocket, manager)
            } catch (e: IOException) {
                if (isRunning) {
                    logger.error("Error accepting client connection", e)
                } else {
                    logger.info("Socket server closed")
                    break
                }
            } catch (e: InterruptedException) {
                logger.info("Socket server thread interrupted")
                break
            } catch (e: Exception) {
                logger.error("Unexpected error in accept loop", e)
                if (isRunning) {
                    try {
                        Thread.sleep(1000)
                    } catch (ie: InterruptedException) {
                        logger.info("Socket server thread interrupted during sleep")
                        break
                    }
                }
            }
        }
        logger.info("Socket accept loop terminated")
    }

    private fun handleClient(clientSocket: Socket, manager: ExtensionHostManager) {
        try {
            manager.start()
            var lastCheckTime = System.currentTimeMillis()
            val CHECK_INTERVAL = 15000
            while (clientSocket.isConnected && !clientSocket.isClosed && isRunning) {
                try {
                    val currentTime = System.currentTimeMillis()
                    if (currentTime - lastCheckTime > CHECK_INTERVAL) {
                        lastCheckTime = currentTime
                        if (!isSocketHealthy(clientSocket)) {
                            logger.error("Detected unhealthy Socket connection, closing connection")
                            break
                        }
                        val responsiveState = manager.getResponsiveState()
                        if (responsiveState != null) {
                            logger.info("Current RPC response state: $responsiveState")
                        }
                    }
                    Thread.sleep(500)
                } catch (ie: InterruptedException) {
                    logger.info("Client handler thread interrupted, exiting loop")
                    break
                }
            }
        } catch (e: Exception) {
            if (e !is InterruptedException) {
                logger.error("Error handling client socket: ${e.message}", e)
            } else {
                logger.info("Client handler thread interrupted during processing")
            }
        } finally {
            manager.dispose()
            clientManagers.remove(clientSocket)
            if (!clientSocket.isClosed) {
                try {
                    clientSocket.close()
                } catch (e: IOException) {
                    logger.warn("Failed to close client socket", e)
                }
            }
            logger.info("Client socket closed and removed")
        }
    }

    private fun isSocketHealthy(socket: Socket): Boolean {
        val isHealthy = socket.isConnected &&
                !socket.isClosed &&
                !socket.isInputShutdown &&
                !socket.isOutputShutdown
        if (!isHealthy) {
            logger.warn("Socket health check failed: isConnected=${socket.isConnected}, " +
                    "isClosed=${socket.isClosed}, " +
                    "isInputShutdown=${socket.isInputShutdown}, " +
                    "isOutputShutdown=${socket.isOutputShutdown}")
        }
        return isHealthy
    }
}

class ExtensionUnixDomainSocketServer {
    private val logger: ILog = Platform.getLog(Activator.plugin?.bundle)
    // private var serverSocket: AFUNIXServerSocket? = null
    private var udsSocketPath: File? = null
    private val clientManagers = ConcurrentHashMap<Socket, ExtensionHostManager>()
    private var serverThread: Thread? = null
    private var projectPath: String = ""
    @Volatile private var isRunning = false

    fun start(projectPath: String): String? {
        if (isRunning) {
            logger.info("UDS server is already running")
            return udsSocketPath?.toString()
        }
        this.projectPath = projectPath
        return startUds()
    }

    private fun startUds(): String? {
        logger.error("[UDS] Not implemented due to dependency issues")
        return null
        /*
        try {
            val sockFile = File.createTempFile("roo-cline-idea-extension-ipc-", ".sock")
            sockFile.delete()
            udsSocketPath = sockFile
            val udsAddr = AFUNIXSocketAddress.of(sockFile.toPath())
            serverSocket = AFUNIXServerSocket.newInstance()
            serverSocket!!.bind(udsAddr)
            isRunning = true
            logger.info("[UDS] Listening on: $sockFile")
            serverThread =
                    thread(start = true, name = "ExtensionUDSSocketServer") {
                        acceptUdsConnections()
                    }
            return sockFile.toString()
        } catch (e: Exception) {
            logger.error("[UDS] Failed to start server", e)
            stop()
            return null
        }
        */
    }

    fun stop() {
        if (!isRunning) return
        isRunning = false
        logger.info("Stopping UDS socket server")
        clientManagers.forEach { (_, manager) ->
            try {
                manager.dispose()
            } catch (e: Exception) {
                logger.warn("Failed to dispose client manager", e)
            }
        }
        clientManagers.clear()
        /*
        try {
            serverSocket?.close()
        } catch (e: Exception) {
            logger.warn("Failed to close UDS server channel", e)
        }
        */
        try {
            udsSocketPath?.delete()
        } catch (e: Exception) {
            logger.warn("Failed to delete UDS socket file", e)
        }
        serverThread?.interrupt()
        serverThread = null
        // serverSocket = null
        udsSocketPath = null
        logger.info("UDS socket server stopped")
    }

    private fun acceptUdsConnections() {
        /*
        val server = serverSocket ?: return
        logger.info("[UDS] Waiting for connections..., tid: ${Thread.currentThread().id}")
        while (isRunning && !Thread.currentThread().isInterrupted) {
            try {
                val clientSocket = server.accept()
                logger.info("[UDS] New client connected")
                val manager = ExtensionHostManager(clientSocket, projectPath)
                clientManagers[clientSocket] = manager
                handleClient(clientSocket, manager)
            } catch (e: Exception) {
                if (isRunning) {
                    logger.error("[UDS] Accept failed, will retry in 1s", e)
                    Thread.sleep(1000)
                } else {
                    logger.info("[UDS] Accept loop exiting (server stopped)")
                    break
                }
            }
        }
        logger.info("[UDS] Accept loop terminated.")
        */
    }

    private fun handleClient(clientSocket: Socket, manager: ExtensionHostManager) {
        try {
            manager.start()
            var lastCheckTime = System.currentTimeMillis()
            val CHECK_INTERVAL = 15000
            while (clientSocket.isConnected && !clientSocket.isClosed && isRunning) {
                try {
                    val currentTime = System.currentTimeMillis()
                    if (currentTime - lastCheckTime > CHECK_INTERVAL) {
                        lastCheckTime = currentTime
                        if (!isSocketHealthy(clientSocket)) {
                            logger.error("[UDS] Client channel unhealthy, closing.")
                            break
                        }
                        val responsiveState = manager.getResponsiveState()
                        if (responsiveState != null) {
                            logger.info("[UDS] Client RPC state: $responsiveState")
                        }
                    }
                    Thread.sleep(500)
                } catch (ie: InterruptedException) {
                    logger.info("[UDS] Client handler interrupted, exiting loop")
                    break
                }
            }
        } catch (e: Exception) {
            if (e !is InterruptedException) {
                logger.error("[UDS] Error in client handler: ${e.message}", e)
            } else {
                logger.info("[UDS] Client handler interrupted during processing")
            }
        } finally {
            manager.dispose()
            clientManagers.remove(clientSocket)
            try {
                clientSocket.close()
            } catch (e: IOException) {
                logger.warn("[UDS] Close client channel error", e)
            }
            logger.info("[UDS] Client channel closed and removed.")
        }
    }

    private fun isSocketHealthy(socket: Socket): Boolean {
        return socket.isConnected && !socket.isClosed
    }
}

class ExtensionProcessManager {
    companion object {
        private const val NODE_MODULES_PATH = PluginConstants.NODE_MODULES_PATH
        private const val EXTENSION_ENTRY_FILE = PluginConstants.EXTENSION_ENTRY_FILE
        private const val RUNTIME_DIR = PluginConstants.RUNTIME_DIR
        private const val PLUGIN_ID = PluginConstants.PLUGIN_ID
        private val MIN_REQUIRED_NODE_VERSION = NodeVersion(20, 6, 0, "20.6.0")
    }
    private val LOG: ILog = Platform.getLog(Activator.plugin?.bundle)
    private var process: Process? = null
    private var monitorThread: Thread? = null
    @Volatile
    private var isRunning = false

    fun start(portOrPath: Any?): Boolean {
        if (isRunning) {
            LOG.info("Extension process is already running")
            return true
        }
        val isUds = portOrPath is String
        if (!ExtensionUtils.isValidPortOrPath(portOrPath)) {
            LOG.error("Invalid socket info: $portOrPath")
            return false
        }
        try {
            val nodePath = findNodeExecutable()
            if (nodePath == null) {
                LOG.error("Failed to find Node.js executable")
                NotificationUtil.showError(
                    "Node.js environment missing",
                    "Node.js environment not detected, please install Node.js and try again. Recommended version: $MIN_REQUIRED_NODE_VERSION or higher."
                )
                return false
            }
            val nodeVersion = NodeVersionUtil.getNodeVersion(nodePath)
            if (!NodeVersionUtil.isVersionSupported(nodeVersion, MIN_REQUIRED_NODE_VERSION)) {
                LOG.error("Node.js version is not supported: $nodeVersion, required: $MIN_REQUIRED_NODE_VERSION")
                NotificationUtil.showError(
                    "Node.js version too low",
                    "Current Node.js($nodePath) version is $nodeVersion, please upgrade to $MIN_REQUIRED_NODE_VERSION or higher for better compatibility."
                )
                return false
            }
            val extensionPath = findExtensionEntryFile()
            if (extensionPath == null) {
                LOG.error("Failed to find extension entry file")
                return false
            }
            val nodeModulesPath = findNodeModulesPath()
            if (nodeModulesPath == null) {
                LOG.error("Failed to find node_modules directory")
                return false
            }
            LOG.info("Starting extension process with node: $nodePath, entry: $extensionPath")
            val envVars = HashMap<String, String>(System.getenv())
            envVars["PATH"] = buildEnhancedPath(envVars, nodePath)
            LOG.info("Enhanced PATH for ${System.getProperty("os.name")}: ${envVars["PATH"]}")
            if (isUds) {
                envVars["VSCODE_EXTHOST_IPC_HOOK"] = portOrPath.toString()
            } else {
                envVars["VSCODE_EXTHOST_WILL_SEND_SOCKET"] = "1"
                envVars["VSCODE_EXTHOST_SOCKET_HOST"] = "127.0.0.1"
                envVars["VSCODE_EXTHOST_SOCKET_PORT"] = portOrPath.toString()
            }
            val commandArgs = mutableListOf(
                nodePath,
                "--experimental-global-webcrypto",
                "--no-deprecation",
                extensionPath,
                "--vscode-socket-port=${envVars["VSCODE_EXTHOST_SOCKET_PORT"]}",
                "--vscode-socket-host=${envVars["VSCODE_EXTHOST_SOCKET_HOST"]}",
                "--vscode-will-send-socket=${envVars["VSCODE_EXTHOST_WILL_SEND_SOCKET"]}"
            )
            try {
                val proxyEnvVars = ProxyConfigUtil.getProxyEnvVarsForProcessStart()
                envVars.putAll(proxyEnvVars)
                if (proxyEnvVars.isNotEmpty()) {
                    LOG.info("Applied proxy configuration for process startup")
                }
            } catch (e: Exception) {
                LOG.warn("Failed to configure proxy settings", e)
            }
            val builder = ProcessBuilder(commandArgs)
            LOG.info("Environment variables:")
            envVars.forEach { (key, value) ->
                LOG.info("  $key = $value")
            }
            builder.environment().putAll(envVars)
            builder.redirectErrorStream(true)
            process = builder.start()
            monitorThread = Thread {
                monitorProcess()
            }.apply {
                name = "ExtensionProcessMonitor"
                isDaemon = true
                start()
            }
            isRunning = true
            LOG.info("Extension process started")
            return true
        } catch (e: Exception) {
            LOG.error("Failed to start extension process", e)
            stopInternal()
            return false
        }
    }

    private fun monitorProcess() {
        val proc = process ?: return
        try {
            val logThread = Thread {
                proc.inputStream.bufferedReader().use { reader ->
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        LOG.info("Extension process: $line")
                    }
                }
            }
            logThread.name = "ExtensionProcessLogger"
            logThread.isDaemon = true
            logThread.start()
            try {
                val exitCode = proc.waitFor()
                LOG.info("Extension process exited with code: $exitCode")
            } catch (e: InterruptedException) {
                LOG.info("Process monitor interrupted")
            }
            logThread.interrupt()
            try {
                logThread.join(1000)
            } catch (e: InterruptedException) {
            }
        } catch (e: Exception) {
            LOG.error("Error monitoring extension process", e)
        } finally {
            synchronized(this) {
                if (process === proc) {
                    isRunning = false
                    process = null
                }
            }
        }
    }

    fun stop() {
        if (!isRunning) {
            return
        }
        stopInternal()
    }

    private fun stopInternal() {
        LOG.info("Stopping extension process")
        val proc = process
        if (proc != null) {
            try {
                if (proc.isAlive) {
                    proc.destroy()
                    if (!proc.waitFor(5, TimeUnit.SECONDS)) {
                        proc.destroyForcibly()
                        proc.waitFor(2, TimeUnit.SECONDS)
                    }
                }
            } catch (e: Exception) {
                LOG.error("Error stopping extension process", e)
            }
        }
        monitorThread?.interrupt()
        try {
            monitorThread?.join(1000)
        } catch (e: InterruptedException) {
        }
        process = null
        monitorThread = null
        isRunning = false
        LOG.info("Extension process stopped")
    }

    private fun findNodeExecutable(): String? {
        val resourcesPath = PluginResourceUtil.getResourcePath(PLUGIN_ID, NODE_MODULES_PATH)
        if (resourcesPath != null) {
            val resourceDir = File(resourcesPath)
            if (resourceDir.exists() && resourceDir.isDirectory) {
                val nodeBin = if (System.getProperty("os.name").lowercase().contains("win")) {
                    File(resourceDir, "node.exe")
                } else {
                    File(resourceDir, ".bin/node")
                }
                if (nodeBin.exists() && nodeBin.canExecute()) {
                    return nodeBin.absolutePath
                }
            }
        }
        return findExecutableInPath("node")
    }

    private fun findExecutableInPath(name: String): String? {
        val path = System.getenv("PATH")
        val paths = path.split(File.pathSeparator)
        for (p in paths) {
            val file = File(p, name)
            if (file.exists() && file.canExecute()) {
                return file.absolutePath
            }
        }
        return null
    }

    fun findExtensionEntryFile(): String? {
        val resourcesPath = PluginResourceUtil.getResourcePath(PLUGIN_ID, "$RUNTIME_DIR/$EXTENSION_ENTRY_FILE")
        if (resourcesPath != null) {
            val resource = File(resourcesPath)
            if (resource.exists() && resource.isFile) {
                return resourcesPath
            }
        }
        return null
    }

    private fun findNodeModulesPath(): String? {
        val nodePath = PluginResourceUtil.getResourcePath(PLUGIN_ID, NODE_MODULES_PATH)
        if (nodePath != null) {
            val nodeDir = File(nodePath)
            if (nodeDir.exists() && nodeDir.isDirectory) {
                return nodeDir.absolutePath
            }
        }
        return null
    }

    private fun buildEnhancedPath(envVars: MutableMap<String, String>, nodePath: String): String {
        val currentPath = envVars.filterKeys { it.equals("PATH", ignoreCase = true) }
            .values.firstOrNull() ?: ""
        val pathBuilder = mutableListOf<String>()
        val nodeDir = File(nodePath).parentFile?.absolutePath
        if (nodeDir != null && !currentPath.contains(nodeDir)) {
            pathBuilder.add(nodeDir)
        }
        val commonDevPaths = when {
            System.getProperty("os.name").lowercase().contains("mac") -> listOf(
                "/opt/homebrew/bin",
                "/opt/homebrew/sbin",
                "/usr/local/bin",
                "/usr/local/sbin",
                "${System.getProperty("user.home")}/.local/bin"
            )
            System.getProperty("os.name").lowercase().contains("win") -> listOf(
                "C:\\Windows\\System32",
                "C:\\Windows\\SysWOW64",
                "C:\\Windows",
                "C:\\Windows\\System32\\WindowsPowerShell\\v1.0",
                "C:\\Program Files\\PowerShell\\7",
                "C:\\Program Files (x86)\\PowerShell\\7"
            )
            else -> emptyList()
        }
        commonDevPaths.forEach { path ->
            if (File(path).exists() && !currentPath.contains(path)) {
                pathBuilder.add(path)
                LOG.info("Add path to PATH: $path")
            } else if (!File(path).exists()) {
                LOG.warn("Path does not exist, skip: $path")
            }
        }
        if (currentPath.isNotEmpty()) {
            pathBuilder.add(currentPath)
        }
        return pathBuilder.joinToString(File.pathSeparator)
    }
}
