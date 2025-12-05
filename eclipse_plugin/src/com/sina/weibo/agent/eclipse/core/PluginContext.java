/*
 * SPDX-FileCopyrightText: 2025 Weibo, Inc.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.sina.weibo.agent.eclipse.core;

import java.io.File;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.core.resources.IProject;

import com.sina.weibo.agent.eclipse.Activator;
import com.sina.weibo.agent.eclipse.ipc.proxy.IRPCProtocol;
import com.sina.weibo.agent.eclipse.webview.WebViewManager;

/**
 * Singleton class that holds the plugin context and shared state.
 * This is the central hub for all plugin-wide resources and state management.
 */
public class PluginContext {

    /** Singleton instance */
    private static volatile PluginContext instance;

    /** Lock for singleton initialization */
    private static final Object LOCK = new Object();

    /** Reference to the plugin activator */
    private Activator activator;

    /** Debug mode flag */
    private boolean debugMode = false;

    /** Plugin initialized flag */
    private final AtomicBoolean initialized = new AtomicBoolean(false);

    /** Plugin disposed flag */
    private final AtomicBoolean disposed = new AtomicBoolean(false);

    /** Current active project */
    private IProject currentProject;

    /** RPC Protocol instance for the current project */
    private IRPCProtocol rpcProtocol;

    /** WebView Manager instance */
    private WebViewManager webViewManager;

    /** Extension Process Manager instance */
    private ExtensionProcessManager extensionProcessManager;

    /** Extension Host Manager instance */
    private ExtensionHostManager extensionHostManager;

    /** Extension Socket Server instance */
    private ExtensionSocketServer extensionSocketServer;

    /** RPC Manager instance */
    private RPCManager rpcManager;

    /** Project-specific contexts */
    private final ConcurrentHashMap<String, ProjectContext> projectContexts = new ConcurrentHashMap<>();

    /** Current extension ID */
    private String currentExtensionId = "roo-code";

    /** Plugin resource directory */
    private File resourceDirectory;

    /** Plugin configuration directory */
    private File configDirectory;

    /**
     * Private constructor for singleton pattern.
     */
    private PluginContext() {
        // Private constructor
    }

    /**
     * Get the singleton instance of PluginContext.
     *
     * @return the singleton instance
     */
    public static PluginContext getInstance() {
        if (instance == null) {
            synchronized (LOCK) {
                if (instance == null) {
                    instance = new PluginContext();
                }
            }
        }
        return instance;
    }

    /**
     * Initialize the plugin context.
     *
     * @param activator the plugin activator
     */
    public void initialize(Activator activator) {
        if (initialized.get()) {
            Activator.logWarning("PluginContext already initialized");
            return;
        }

        this.activator = activator;
        
        // Initialize directories
        initializeDirectories();

        // Check for debug mode from system properties or environment
        String debugProperty = System.getProperty("runvsagent.debug", "false");
        String debugEnv = System.getenv("RUNVSAGENT_DEBUG");
        this.debugMode = "true".equalsIgnoreCase(debugProperty) || "true".equalsIgnoreCase(debugEnv);

        if (debugMode) {
            Activator.logInfo("Debug mode enabled");
        }

        initialized.set(true);
        Activator.logInfo("PluginContext initialized");
    }

    /**
     * Initialize plugin directories.
     */
    private void initializeDirectories() {
        // Get user home directory
        String userHome = System.getProperty("user.home");
        
        // Config directory: ~/.runvsagent
        configDirectory = new File(userHome, ".runvsagent");
        if (!configDirectory.exists()) {
            configDirectory.mkdirs();
        }

        // Resource directory is within the plugin bundle
        // This will be set properly when we have access to bundle location
        resourceDirectory = new File(configDirectory, "resources");
        if (!resourceDirectory.exists()) {
            resourceDirectory.mkdirs();
        }

        Activator.logInfo("Config directory: " + configDirectory.getAbsolutePath());
        Activator.logInfo("Resource directory: " + resourceDirectory.getAbsolutePath());
    }

    /**
     * Dispose the plugin context and release all resources.
     */
    public void dispose() {
        if (disposed.getAndSet(true)) {
            return; // Already disposed
        }

        Activator.logInfo("Disposing PluginContext...");

        // Dispose project contexts
        for (ProjectContext ctx : projectContexts.values()) {
            try {
                ctx.dispose();
            } catch (Exception e) {
                Activator.logError("Error disposing project context", e);
            }
        }
        projectContexts.clear();

        // Dispose extension host manager
        if (extensionHostManager != null) {
            try {
                extensionHostManager.dispose();
            } catch (Exception e) {
                Activator.logError("Error disposing extension host manager", e);
            }
            extensionHostManager = null;
        }

        // Dispose extension process manager
        if (extensionProcessManager != null) {
            try {
                extensionProcessManager.dispose();
            } catch (Exception e) {
                Activator.logError("Error disposing extension process manager", e);
            }
            extensionProcessManager = null;
        }

        // Dispose socket server
        if (extensionSocketServer != null) {
            try {
                extensionSocketServer.dispose();
            } catch (Exception e) {
                Activator.logError("Error disposing socket server", e);
            }
            extensionSocketServer = null;
        }

        // Dispose RPC protocol
        if (rpcProtocol != null) {
            try {
                rpcProtocol.dispose();
            } catch (Exception e) {
                Activator.logError("Error disposing RPC protocol", e);
            }
            rpcProtocol = null;
        }

        // Dispose WebView manager
        if (webViewManager != null) {
            try {
                webViewManager.dispose();
            } catch (Exception e) {
                Activator.logError("Error disposing WebView manager", e);
            }
            webViewManager = null;
        }

        // Dispose RPC manager
        if (rpcManager != null) {
            try {
                rpcManager.dispose();
            } catch (Exception e) {
                Activator.logError("Error disposing RPC manager", e);
            }
            rpcManager = null;
        }

        initialized.set(false);
        Activator.logInfo("PluginContext disposed");
    }

    // ==================== Getters and Setters ====================

    /**
     * Check if the plugin is initialized.
     *
     * @return true if initialized
     */
    public boolean isInitialized() {
        return initialized.get();
    }

    /**
     * Check if the plugin is disposed.
     *
     * @return true if disposed
     */
    public boolean isDisposed() {
        return disposed.get();
    }

    /**
     * Check if debug mode is enabled.
     *
     * @return true if debug mode is enabled
     */
    public boolean isDebugMode() {
        return debugMode;
    }

    /**
     * Set debug mode.
     *
     * @param debugMode true to enable debug mode
     */
    public void setDebugMode(boolean debugMode) {
        this.debugMode = debugMode;
    }

    /**
     * Get the plugin activator.
     *
     * @return the plugin activator
     */
    public Activator getActivator() {
        return activator;
    }

    /**
     * Get the current project.
     *
     * @return the current project
     */
    public IProject getCurrentProject() {
        return currentProject;
    }

    /**
     * Set the current project.
     *
     * @param project the current project
     */
    public void setCurrentProject(IProject project) {
        this.currentProject = project;
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
     * Set the RPC protocol.
     *
     * @param rpcProtocol the RPC protocol
     */
    public void setRPCProtocol(IRPCProtocol rpcProtocol) {
        this.rpcProtocol = rpcProtocol;
    }

    /**
     * Get the WebView manager.
     *
     * @return the WebView manager
     */
    public WebViewManager getWebViewManager() {
        if (webViewManager == null) {
            webViewManager = new WebViewManager();
        }
        return webViewManager;
    }

    /**
     * Set the WebView manager.
     *
     * @param webViewManager the WebView manager
     */
    public void setWebViewManager(WebViewManager webViewManager) {
        this.webViewManager = webViewManager;
    }

    /**
     * Get the extension process manager.
     *
     * @return the extension process manager
     */
    public ExtensionProcessManager getExtensionProcessManager() {
        if (extensionProcessManager == null) {
            extensionProcessManager = new ExtensionProcessManager();
        }
        return extensionProcessManager;
    }

    /**
     * Set the extension process manager.
     *
     * @param extensionProcessManager the extension process manager
     */
    public void setExtensionProcessManager(ExtensionProcessManager extensionProcessManager) {
        this.extensionProcessManager = extensionProcessManager;
    }

    /**
     * Get the extension host manager.
     *
     * @return the extension host manager
     */
    public ExtensionHostManager getExtensionHostManager() {
        return extensionHostManager;
    }

    /**
     * Set the extension host manager.
     *
     * @param extensionHostManager the extension host manager
     */
    public void setExtensionHostManager(ExtensionHostManager extensionHostManager) {
        this.extensionHostManager = extensionHostManager;
    }

    /**
     * Get the extension socket server.
     *
     * @return the extension socket server
     */
    public ExtensionSocketServer getExtensionSocketServer() {
        if (extensionSocketServer == null) {
            extensionSocketServer = new ExtensionSocketServer();
        }
        return extensionSocketServer;
    }

    /**
     * Set the extension socket server.
     *
     * @param extensionSocketServer the extension socket server
     */
    public void setExtensionSocketServer(ExtensionSocketServer extensionSocketServer) {
        this.extensionSocketServer = extensionSocketServer;
    }

    /**
     * Get the RPC manager.
     *
     * @return the RPC manager
     */
    public RPCManager getRPCManager() {
        return rpcManager;
    }

    /**
     * Set the RPC manager.
     *
     * @param rpcManager the RPC manager
     */
    public void setRPCManager(RPCManager rpcManager) {
        this.rpcManager = rpcManager;
    }

    /**
     * Get the current extension ID.
     *
     * @return the current extension ID
     */
    public String getCurrentExtensionId() {
        return currentExtensionId;
    }

    /**
     * Set the current extension ID.
     *
     * @param extensionId the extension ID
     */
    public void setCurrentExtensionId(String extensionId) {
        this.currentExtensionId = extensionId;
    }

    /**
     * Get the resource directory.
     *
     * @return the resource directory
     */
    public File getResourceDirectory() {
        return resourceDirectory;
    }

    /**
     * Get the config directory.
     *
     * @return the config directory
     */
    public File getConfigDirectory() {
        return configDirectory;
    }

    /**
     * Get or create a project context.
     *
     * @param projectPath the project path
     * @return the project context
     */
    public ProjectContext getProjectContext(String projectPath) {
        return projectContexts.computeIfAbsent(projectPath, k -> new ProjectContext(projectPath));
    }

    /**
     * Remove a project context.
     *
     * @param projectPath the project path
     */
    public void removeProjectContext(String projectPath) {
        ProjectContext ctx = projectContexts.remove(projectPath);
        if (ctx != null) {
            ctx.dispose();
        }
    }

    /**
     * Clear the singleton instance (for testing purposes).
     */
    public static void clearInstance() {
        synchronized (LOCK) {
            if (instance != null) {
                instance.dispose();
                instance = null;
            }
        }
    }

    /**
     * Inner class to hold project-specific context.
     */
    public static class ProjectContext {
        private final String projectPath;
        private IRPCProtocol rpcProtocol;
        private ExtensionHostManager extensionHostManager;

        public ProjectContext(String projectPath) {
            this.projectPath = projectPath;
        }

        public String getProjectPath() {
            return projectPath;
        }

        public IRPCProtocol getRPCProtocol() {
            return rpcProtocol;
        }

        public void setRPCProtocol(IRPCProtocol rpcProtocol) {
            this.rpcProtocol = rpcProtocol;
        }

        public ExtensionHostManager getExtensionHostManager() {
            return extensionHostManager;
        }

        public void setExtensionHostManager(ExtensionHostManager extensionHostManager) {
            this.extensionHostManager = extensionHostManager;
        }

        public void dispose() {
            if (rpcProtocol != null) {
                try {
                    rpcProtocol.dispose();
                } catch (Exception e) {
                    Activator.logError("Error disposing project RPC protocol", e);
                }
                rpcProtocol = null;
            }
            if (extensionHostManager != null) {
                try {
                    extensionHostManager.dispose();
                } catch (Exception e) {
                    Activator.logError("Error disposing project extension host manager", e);
                }
                extensionHostManager = null;
            }
        }
    }
}
