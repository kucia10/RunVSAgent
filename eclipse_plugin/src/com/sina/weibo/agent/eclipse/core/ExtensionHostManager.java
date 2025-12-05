/*
 * SPDX-FileCopyrightText: 2025 Weibo, Inc.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.sina.weibo.agent.eclipse.core;

import java.net.Socket;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Platform;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.sina.weibo.agent.eclipse.Activator;
import com.sina.weibo.agent.eclipse.ipc.NodeSocket;
import com.sina.weibo.agent.eclipse.ipc.proxy.IRPCProtocol;

/**
 * Manages communication with the extension host process.
 * Handles message passing, initialization, and state management.
 */
public class ExtensionHostManager {

    /** Message type for ready */
    private static final int MSG_TYPE_READY = 1;

    /** Message type for initialized */
    private static final int MSG_TYPE_INITIALIZED = 2;

    /** Socket connection */
    private NodeSocket socket;

    /** Project path */
    private String projectPath;

    /** Current project */
    private IProject project;

    /** RPC Protocol */
    private IRPCProtocol rpcProtocol;

    /** Ready flag */
    private final AtomicBoolean ready = new AtomicBoolean(false);

    /** Initialized flag */
    private final AtomicBoolean initialized = new AtomicBoolean(false);

    /** Disposed flag */
    private final AtomicBoolean disposed = new AtomicBoolean(false);

    /** Ready future */
    private final CompletableFuture<Void> readyFuture = new CompletableFuture<>();

    /** Initialized future */
    private final CompletableFuture<Void> initializedFuture = new CompletableFuture<>();

    /** Gson for JSON processing */
    private final Gson gson = new Gson();

    /**
     * Create an ExtensionHostManager with a socket.
     *
     * @param clientSocket the client socket
     * @param projectPath the project path
     * @param project the Eclipse project
     */
    public ExtensionHostManager(Socket clientSocket, String projectPath, IProject project) {
        this.socket = new NodeSocket(clientSocket, "ExtensionHost");
        this.projectPath = projectPath;
        this.project = project;
        setupMessageHandler();
    }

    /**
     * Create an ExtensionHostManager with a socket channel.
     *
     * @param clientChannel the socket channel
     * @param projectPath the project path
     * @param project the Eclipse project
     */
    public ExtensionHostManager(SocketChannel clientChannel, String projectPath, IProject project) {
        this.socket = new NodeSocket(clientChannel, "ExtensionHost");
        this.projectPath = projectPath;
        this.project = project;
        setupMessageHandler();
    }

    /**
     * Create an ExtensionHostManager with a NodeSocket.
     *
     * @param nodeSocket the node socket
     * @param projectPath the project path
     * @param project the Eclipse project
     */
    public ExtensionHostManager(NodeSocket nodeSocket, String projectPath, IProject project) {
        this.socket = nodeSocket;
        this.projectPath = projectPath;
        this.project = project;
        setupMessageHandler();
    }

    /**
     * Setup message handler for incoming data.
     */
    private void setupMessageHandler() {
        socket.onData(data -> {
            try {
                handleMessage(data);
            } catch (Exception e) {
                Activator.logError("Error handling message from extension host", e);
            }
        });

        socket.onClose(event -> {
            Activator.logInfo("Extension host connection closed: " + event);
            ready.set(false);
            initialized.set(false);
        });
    }

    /**
     * Start the extension host manager.
     */
    public void start() {
        Activator.logInfo("Starting ExtensionHostManager...");
        socket.startReceiving();
        Activator.logInfo("ExtensionHostManager started");
    }

    /**
     * Handle incoming message from extension host.
     *
     * @param data the message data
     */
    private void handleMessage(byte[] data) {
        if (data == null || data.length == 0) {
            return;
        }

        // Check for special message types
        if (data.length >= 4) {
            int messageType = ((data[0] & 0xFF) << 24) | ((data[1] & 0xFF) << 16) |
                    ((data[2] & 0xFF) << 8) | (data[3] & 0xFF);

            if (messageType == MSG_TYPE_READY) {
                handleReadyMessage();
                return;
            } else if (messageType == MSG_TYPE_INITIALIZED) {
                handleInitializedMessage();
                return;
            }
        }

        // Forward to RPC protocol if available
        if (rpcProtocol != null) {
            rpcProtocol.send(data);
        }
    }

    /**
     * Handle ready message from extension host.
     */
    private void handleReadyMessage() {
        Activator.logInfo("Extension host ready");
        ready.set(true);
        readyFuture.complete(null);

        // Send initialization data
        sendInitData();
    }

    /**
     * Handle initialized message from extension host.
     */
    private void handleInitializedMessage() {
        Activator.logInfo("Extension host initialized");
        initialized.set(true);
        initializedFuture.complete(null);
    }

    /**
     * Send initialization data to extension host.
     */
    private void sendInitData() {
        Map<String, Object> initData = createInitData();
        String json = gson.toJson(initData);
        
        Activator.logInfo("Sending init data to extension host");
        socket.write(json.getBytes());
    }

    /**
     * Create initialization data for extension host.
     *
     * @return the init data map
     */
    private Map<String, Object> createInitData() {
        Map<String, Object> initData = new HashMap<>();

        // IDE information
        initData.put("ideName", getCurrentIDEName());
        initData.put("ideVersion", getIDEVersion());
        initData.put("pluginVersion", Activator.VERSION);

        // Project information
        initData.put("projectPath", projectPath);
        if (project != null) {
            initData.put("projectName", project.getName());
        }

        // Plugin directories
        PluginContext context = PluginContext.getInstance();
        initData.put("pluginDir", getPluginDir());
        initData.put("configDir", context.getConfigDirectory().getAbsolutePath());
        initData.put("resourceDir", context.getResourceDirectory().getAbsolutePath());

        // Extension configuration
        initData.put("extensionId", context.getCurrentExtensionId());
        
        // Extension path
        String extensionPath = getExtensionPath();
        if (extensionPath != null) {
            initData.put("extensionPath", extensionPath);
        }

        // System information
        initData.put("osName", System.getProperty("os.name"));
        initData.put("osVersion", System.getProperty("os.version"));
        initData.put("osArch", System.getProperty("os.arch"));

        return initData;
    }

    /**
     * Get current IDE name.
     *
     * @return the IDE name
     */
    private String getCurrentIDEName() {
        if (Platform.getProduct() != null) {
            return Platform.getProduct().getName();
        }
        return "Eclipse";
    }

    /**
     * Get IDE version.
     *
     * @return the IDE version
     */
    private String getIDEVersion() {
        if (Platform.getProduct() != null) {
            String version = Platform.getProduct().getDefiningBundle().getVersion().toString();
            return version;
        }
        return "Unknown";
    }

    /**
     * Get plugin directory.
     *
     * @return the plugin directory path
     */
    private String getPluginDir() {
        PluginContext context = PluginContext.getInstance();
        return context.getResourceDirectory().getAbsolutePath();
    }

    /**
     * Get extension path.
     *
     * @return the extension path or null
     */
    private String getExtensionPath() {
        PluginContext context = PluginContext.getInstance();
        String extensionId = context.getCurrentExtensionId();
        
        // Look for extension in resource directory
        java.io.File extensionDir = new java.io.File(context.getResourceDirectory(), extensionId);
        if (extensionDir.exists() && extensionDir.isDirectory()) {
            return extensionDir.getAbsolutePath();
        }

        return null;
    }

    /**
     * Get responsive state.
     *
     * @return the responsive state or null
     */
    public ResponsiveState getResponsiveState() {
        if (!ready.get()) {
            return ResponsiveState.NOT_READY;
        }
        if (!initialized.get()) {
            return ResponsiveState.INITIALIZING;
        }
        if (socket.isClosed()) {
            return ResponsiveState.DISCONNECTED;
        }
        return ResponsiveState.RESPONSIVE;
    }

    /**
     * Wait for ready state.
     *
     * @return a future that completes when ready
     */
    public CompletableFuture<Void> waitForReady() {
        return readyFuture;
    }

    /**
     * Wait for initialized state.
     *
     * @return a future that completes when initialized
     */
    public CompletableFuture<Void> waitForInitialized() {
        return initializedFuture;
    }

    /**
     * Set the RPC protocol.
     *
     * @param rpcProtocol the RPC protocol
     */
    public void setRPCProtocol(IRPCProtocol rpcProtocol) {
        this.rpcProtocol = rpcProtocol;
    }

    /**
     * Get the RPC protocol.
     *
     * @return the RPC protocol
     */
    public IRPCProtocol getRPCProtocol() {
        return rpcProtocol;
    }

    /**
     * Get the socket.
     *
     * @return the socket
     */
    public NodeSocket getSocket() {
        return socket;
    }

    /**
     * Check if ready.
     *
     * @return true if ready
     */
    public boolean isReady() {
        return ready.get();
    }

    /**
     * Check if initialized.
     *
     * @return true if initialized
     */
    public boolean isInitialized() {
        return initialized.get();
    }

    /**
     * Dispose and release resources.
     */
    public void dispose() {
        if (disposed.getAndSet(true)) {
            return;
        }

        Activator.logInfo("Disposing ExtensionHostManager...");

        if (socket != null) {
            socket.dispose();
            socket = null;
        }

        ready.set(false);
        initialized.set(false);

        Activator.logInfo("ExtensionHostManager disposed");
    }

    /**
     * Responsive state enum.
     */
    public enum ResponsiveState {
        NOT_READY,
        INITIALIZING,
        RESPONSIVE,
        UNRESPONSIVE,
        DISCONNECTED
    }
}
