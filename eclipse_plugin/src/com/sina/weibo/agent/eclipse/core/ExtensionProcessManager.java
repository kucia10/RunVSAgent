/*
 * SPDX-FileCopyrightText: 2025 Weibo, Inc.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.sina.weibo.agent.eclipse.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import com.sina.weibo.agent.eclipse.Activator;
import com.sina.weibo.agent.eclipse.util.NodeVersionUtil;

/**
 * Manages the Node.js extension host process.
 * Handles starting, stopping, and monitoring the extension host.
 */
public class ExtensionProcessManager {

    /** Node.js process */
    private Process process;

    /** Process running flag */
    private final AtomicBoolean running = new AtomicBoolean(false);

    /** Disposed flag */
    private final AtomicBoolean disposed = new AtomicBoolean(false);

    /** Executor for process monitoring */
    private ExecutorService monitorExecutor;

    /** Node.js executable path */
    private String nodePath;

    /** Extension entry file path */
    private String extensionEntryFile;

    /** Node modules path */
    private String nodeModulesPath;

    /** Minimum required Node.js version */
    private static final String MIN_NODE_VERSION = "18.0.0";

    /**
     * Create a new ExtensionProcessManager.
     */
    public ExtensionProcessManager() {
        Activator.logInfo("ExtensionProcessManager created");
    }

    /**
     * Start the extension host process.
     *
     * @param portOrPath the port number or socket path
     * @return true if started successfully
     */
    public boolean start(Object portOrPath) {
        if (running.get()) {
            Activator.logWarning("Extension process already running");
            return true;
        }

        try {
            // Find Node.js executable
            nodePath = findNodeExecutable();
            if (nodePath == null) {
                Activator.logError("Node.js not found. Please install Node.js " + MIN_NODE_VERSION + " or later.");
                return false;
            }

            // Validate Node.js version
            if (!NodeVersionUtil.isVersionSatisfied(nodePath, MIN_NODE_VERSION)) {
                Activator.logError("Node.js version is too old. Required: " + MIN_NODE_VERSION + " or later.");
                return false;
            }

            // Find extension entry file
            extensionEntryFile = findExtensionEntryFile();
            if (extensionEntryFile == null) {
                Activator.logError("Extension entry file not found");
                return false;
            }

            // Find node_modules path
            nodeModulesPath = findNodeModulesPath();

            // Build command
            List<String> commandArgs = new ArrayList<>();
            commandArgs.add(nodePath);
            commandArgs.add(extensionEntryFile);

            // Add port or socket path
            if (portOrPath instanceof Integer) {
                commandArgs.add("--port=" + portOrPath);
            } else if (portOrPath instanceof String) {
                commandArgs.add("--socket=" + portOrPath);
            }

            // Build environment
            Map<String, String> env = new HashMap<>(System.getenv());
            
            // Enhance PATH
            String enhancedPath = buildEnhancedPath(env, nodePath);
            env.put("PATH", enhancedPath);

            // Set NODE_PATH if node_modules exists
            if (nodeModulesPath != null) {
                env.put("NODE_PATH", nodeModulesPath);
            }

            // Build process
            ProcessBuilder builder = new ProcessBuilder(commandArgs);
            builder.environment().putAll(env);
            builder.redirectErrorStream(false);

            Activator.logInfo("Starting extension host: " + String.join(" ", commandArgs));

            // Start process
            process = builder.start();
            running.set(true);

            // Start monitoring
            startMonitoring();

            Activator.logInfo("Extension host process started with PID: " + process.pid());
            return true;

        } catch (Exception e) {
            Activator.logError("Failed to start extension host", e);
            return false;
        }
    }

    /**
     * Start monitoring the process output.
     */
    private void startMonitoring() {
        monitorExecutor = Executors.newFixedThreadPool(2, r -> {
            Thread t = new Thread(r, "ExtensionProcess-Monitor");
            t.setDaemon(true);
            return t;
        });

        // Monitor stdout
        monitorExecutor.submit(() -> {
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null && running.get()) {
                    Activator.logInfo("[ExtHost] " + line);
                }
            } catch (IOException e) {
                if (running.get()) {
                    Activator.logError("Error reading stdout", e);
                }
            }
        });

        // Monitor stderr
        monitorExecutor.submit(() -> {
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getErrorStream()))) {
                String line;
                while ((line = reader.readLine()) != null && running.get()) {
                    Activator.logWarning("[ExtHost] " + line);
                }
            } catch (IOException e) {
                if (running.get()) {
                    Activator.logError("Error reading stderr", e);
                }
            }
        });
    }

    /**
     * Stop the extension host process.
     */
    public void stop() {
        if (!running.getAndSet(false)) {
            return;
        }

        Activator.logInfo("Stopping extension host process...");

        // Stop monitoring
        if (monitorExecutor != null) {
            monitorExecutor.shutdownNow();
            monitorExecutor = null;
        }

        // Destroy process
        if (process != null) {
            try {
                // Try graceful shutdown first
                process.destroy();
                
                // Wait for process to exit
                if (!process.waitFor(5, TimeUnit.SECONDS)) {
                    // Force kill if not exited
                    process.destroyForcibly();
                    process.waitFor(2, TimeUnit.SECONDS);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                process.destroyForcibly();
            }
            process = null;
        }

        Activator.logInfo("Extension host process stopped");
    }

    /**
     * Find Node.js executable.
     *
     * @return the path to Node.js or null
     */
    private String findNodeExecutable() {
        // Check common locations
        String[] commonPaths;
        
        if (isWindows()) {
            commonPaths = new String[]{
                    System.getenv("PROGRAMFILES") + "\\nodejs\\node.exe",
                    System.getenv("LOCALAPPDATA") + "\\Programs\\nodejs\\node.exe",
                    "C:\\Program Files\\nodejs\\node.exe",
                    "node.exe" // Try PATH
            };
        } else {
            commonPaths = new String[]{
                    "/usr/local/bin/node",
                    "/usr/bin/node",
                    "/opt/homebrew/bin/node", // macOS ARM
                    System.getProperty("user.home") + "/.nvm/current/bin/node",
                    "node" // Try PATH
            };
        }

        for (String path : commonPaths) {
            File file = new File(path);
            if (file.exists() && file.canExecute()) {
                return file.getAbsolutePath();
            }
        }

        // Try to find in PATH
        String pathNode = findInPath("node");
        if (pathNode != null) {
            return pathNode;
        }

        return null;
    }

    /**
     * Find executable in PATH.
     *
     * @param name the executable name
     * @return the path or null
     */
    private String findInPath(String name) {
        String pathEnv = System.getenv("PATH");
        if (pathEnv == null) {
            return null;
        }

        String separator = isWindows() ? ";" : ":";
        String[] paths = pathEnv.split(separator);
        String suffix = isWindows() ? ".exe" : "";

        for (String path : paths) {
            File file = new File(path, name + suffix);
            if (file.exists() && file.canExecute()) {
                return file.getAbsolutePath();
            }
        }

        return null;
    }

    /**
     * Find extension entry file.
     *
     * @return the path to entry file or null
     */
    public String findExtensionEntryFile() {
        PluginContext context = PluginContext.getInstance();
        File resourceDir = context.getResourceDirectory();

        // Look for entry file in various locations
        String[] possiblePaths = {
                new File(resourceDir, "runtime/dist/main.js").getAbsolutePath(),
                new File(resourceDir, "runtime/main.js").getAbsolutePath(),
                new File(resourceDir, "extension_host/dist/main.js").getAbsolutePath(),
        };

        for (String path : possiblePaths) {
            File file = new File(path);
            if (file.exists()) {
                return file.getAbsolutePath();
            }
        }

        return null;
    }

    /**
     * Find node_modules path.
     *
     * @return the path or null
     */
    private String findNodeModulesPath() {
        PluginContext context = PluginContext.getInstance();
        File resourceDir = context.getResourceDirectory();

        File nodeModules = new File(resourceDir, "node_modules");
        if (nodeModules.exists() && nodeModules.isDirectory()) {
            return nodeModules.getAbsolutePath();
        }

        return null;
    }

    /**
     * Build enhanced PATH for the process.
     *
     * @param envVars the environment variables
     * @param nodePath the path to Node.js
     * @return the enhanced PATH
     */
    private String buildEnhancedPath(Map<String, String> envVars, String nodePath) {
        String separator = isWindows() ? ";" : ":";
        StringBuilder pathBuilder = new StringBuilder();

        // Add Node.js directory
        File nodeDir = new File(nodePath).getParentFile();
        if (nodeDir != null) {
            pathBuilder.append(nodeDir.getAbsolutePath());
        }

        // Add common dev paths
        List<String> devPaths = new ArrayList<>();
        if (isWindows()) {
            devPaths.add(System.getenv("LOCALAPPDATA") + "\\Programs\\Microsoft VS Code\\bin");
        } else {
            devPaths.add("/usr/local/bin");
            devPaths.add("/opt/homebrew/bin"); // macOS ARM
        }

        for (String devPath : devPaths) {
            if (new File(devPath).exists()) {
                pathBuilder.append(separator).append(devPath);
            }
        }

        // Add existing PATH
        String existingPath = envVars.get("PATH");
        if (existingPath != null && !existingPath.isEmpty()) {
            pathBuilder.append(separator).append(existingPath);
        }

        return pathBuilder.toString();
    }

    /**
     * Check if running on Windows.
     *
     * @return true if Windows
     */
    private boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("windows");
    }

    /**
     * Check if the process is running.
     *
     * @return true if running
     */
    public boolean isRunning() {
        return running.get() && process != null && process.isAlive();
    }

    /**
     * Get the process.
     *
     * @return the process or null
     */
    public Process getProcess() {
        return process;
    }

    /**
     * Get the Node.js path.
     *
     * @return the Node.js path or null
     */
    public String getNodePath() {
        return nodePath;
    }

    /**
     * Dispose and release all resources.
     */
    public void dispose() {
        if (disposed.getAndSet(true)) {
            return;
        }

        stop();
        Activator.logInfo("ExtensionProcessManager disposed");
    }
}
