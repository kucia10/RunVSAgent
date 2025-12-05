/*
 * SPDX-FileCopyrightText: 2025 Weibo, Inc.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.sina.weibo.agent.eclipse.core;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import com.sina.weibo.agent.eclipse.Activator;
import com.sina.weibo.agent.eclipse.ipc.NodeSocket;

/**
 * Socket server for extension host communication.
 * Listens for connections from the Node.js extension host process.
 */
public class ExtensionSocketServer {

    /** Server socket */
    private ServerSocket serverSocket;

    /** Server port */
    private int port;

    /** Running flag */
    private final AtomicBoolean running = new AtomicBoolean(false);

    /** Disposed flag */
    private final AtomicBoolean disposed = new AtomicBoolean(false);

    /** Executor for accept loop */
    private ExecutorService acceptExecutor;

    /** Client connection handler */
    private Consumer<NodeSocket> connectionHandler;

    /** Current project path */
    private String projectPath;

    /** Current connected client */
    private NodeSocket currentClient;

    /**
     * Create a new ExtensionSocketServer.
     */
    public ExtensionSocketServer() {
        Activator.logInfo("ExtensionSocketServer created");
    }

    /**
     * Start the server.
     *
     * @param projectPath the project path
     * @return the port number, or -1 if failed
     */
    public int start(String projectPath) {
        if (running.get()) {
            Activator.logWarning("Server already running on port " + port);
            return port;
        }

        this.projectPath = projectPath;

        try {
            // Create server socket on a random available port
            serverSocket = new ServerSocket(0, 50, InetAddress.getLoopbackAddress());
            port = serverSocket.getLocalPort();

            Activator.logInfo("ExtensionSocketServer started on port " + port);

            // Start accept loop
            acceptExecutor = Executors.newSingleThreadExecutor(r -> {
                Thread t = new Thread(r, "ExtensionSocketServer-Accept");
                t.setDaemon(true);
                return t;
            });

            running.set(true);
            acceptExecutor.submit(this::acceptConnections);

            return port;

        } catch (IOException e) {
            Activator.logError("Failed to start ExtensionSocketServer", e);
            return -1;
        }
    }

    /**
     * Accept loop for incoming connections.
     */
    private void acceptConnections() {
        while (running.get() && !disposed.get()) {
            try {
                Socket clientSocket = serverSocket.accept();
                Activator.logInfo("Client connected from " + clientSocket.getRemoteSocketAddress());

                // Wrap in NodeSocket
                NodeSocket nodeSocket = new NodeSocket(clientSocket, "ExtHostClient");
                currentClient = nodeSocket;

                // Notify connection handler
                if (connectionHandler != null) {
                    connectionHandler.accept(nodeSocket);
                }

                // Start receiving data
                nodeSocket.startReceiving();

            } catch (IOException e) {
                if (running.get() && !disposed.get()) {
                    Activator.logError("Error accepting connection", e);
                }
            }
        }
    }

    /**
     * Stop the server.
     */
    public void stop() {
        if (!running.getAndSet(false)) {
            return;
        }

        Activator.logInfo("Stopping ExtensionSocketServer...");

        // Close current client
        if (currentClient != null) {
            currentClient.dispose();
            currentClient = null;
        }

        // Close server socket
        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                Activator.logError("Error closing server socket", e);
            }
            serverSocket = null;
        }

        // Shutdown executor
        if (acceptExecutor != null) {
            acceptExecutor.shutdownNow();
            acceptExecutor = null;
        }

        Activator.logInfo("ExtensionSocketServer stopped");
    }

    /**
     * Connect to a debug host (for development).
     *
     * @param host the host address
     * @param port the port number
     * @return true if connected
     */
    public boolean connectToDebugHost(String host, int port) {
        try {
            Activator.logInfo("Connecting to debug host at " + host + ":" + port);
            
            Socket socket = new Socket(host, port);
            NodeSocket nodeSocket = new NodeSocket(socket, "DebugHost");
            currentClient = nodeSocket;

            // Notify connection handler
            if (connectionHandler != null) {
                connectionHandler.accept(nodeSocket);
            }

            // Start receiving data
            nodeSocket.startReceiving();

            Activator.logInfo("Connected to debug host");
            return true;

        } catch (IOException e) {
            Activator.logError("Failed to connect to debug host", e);
            return false;
        }
    }

    /**
     * Set the connection handler.
     *
     * @param handler the handler to call when a client connects
     */
    public void setConnectionHandler(Consumer<NodeSocket> handler) {
        this.connectionHandler = handler;
    }

    /**
     * Get the server port.
     *
     * @return the port or -1 if not started
     */
    public int getPort() {
        return serverSocket != null ? port : -1;
    }

    /**
     * Check if the server is running.
     *
     * @return true if running
     */
    public boolean isRunning() {
        return running.get();
    }

    /**
     * Get the current client socket.
     *
     * @return the current client or null
     */
    public NodeSocket getCurrentClient() {
        return currentClient;
    }

    /**
     * Get the project path.
     *
     * @return the project path
     */
    public String getProjectPath() {
        return projectPath;
    }

    /**
     * Dispose the server and release resources.
     */
    public void dispose() {
        if (disposed.getAndSet(true)) {
            return;
        }

        stop();
        Activator.logInfo("ExtensionSocketServer disposed");
    }
}
