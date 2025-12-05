/*
 * SPDX-FileCopyrightText: 2025 Weibo, Inc.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.sina.weibo.agent.eclipse;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import com.sina.weibo.agent.eclipse.core.PluginContext;
import com.sina.weibo.agent.eclipse.core.ExtensionProcessManager;

/**
 * The activator class controls the plug-in life cycle.
 * This is the main entry point for the RunVSAgent Eclipse plugin.
 */
public class Activator extends AbstractUIPlugin {

    /** The plug-in ID */
    public static final String PLUGIN_ID = "com.sina.weibo.agent.eclipse";

    /** Plugin version */
    public static final String VERSION = "0.2.5";

    /** The shared instance */
    private static Activator plugin;

    /** Logger instance */
    private static ILog logger;

    /** Bundle context */
    private BundleContext bundleContext;

    /**
     * The constructor
     */
    public Activator() {
        super();
    }

    /**
     * Called when the plugin is started.
     * 
     * @param context the bundle context for this plug-in
     * @throws Exception if this plug-in did not start up properly
     */
    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
        plugin = this;
        this.bundleContext = context;
        logger = Platform.getLog(getBundle());

        logInfo("RunVSAgent Eclipse Plugin starting...");
        logInfo("Plugin ID: " + PLUGIN_ID);
        logInfo("Version: " + VERSION);

        // Log system information
        logSystemInfo();

        // Initialize plugin context
        try {
            PluginContext.getInstance().initialize(this);
            logInfo("Plugin context initialized successfully");
        } catch (Exception e) {
            logError("Failed to initialize plugin context", e);
        }

        logInfo("RunVSAgent Eclipse Plugin started successfully");
    }

    /**
     * Called when the plugin is stopped.
     * 
     * @param context the bundle context for this plug-in
     * @throws Exception if this plug-in did not shut down properly
     */
    @Override
    public void stop(BundleContext context) throws Exception {
        logInfo("RunVSAgent Eclipse Plugin stopping...");

        try {
            // Dispose plugin context and all resources
            PluginContext.getInstance().dispose();
            logInfo("Plugin context disposed successfully");
        } catch (Exception e) {
            logError("Error during plugin context disposal", e);
        }

        plugin = null;
        this.bundleContext = null;

        super.stop(context);
        logInfo("RunVSAgent Eclipse Plugin stopped");
    }

    /**
     * Returns the shared instance.
     *
     * @return the shared instance
     */
    public static Activator getDefault() {
        return plugin;
    }

    /**
     * Returns the bundle context.
     *
     * @return the bundle context
     */
    public BundleContext getBundleContext() {
        return bundleContext;
    }

    /**
     * Returns an image descriptor for the image file at the given
     * plug-in relative path.
     *
     * @param path the path
     * @return the image descriptor
     */
    public static ImageDescriptor getImageDescriptor(String path) {
        return imageDescriptorFromPlugin(PLUGIN_ID, path);
    }

    /**
     * Log system information for debugging purposes.
     */
    private void logSystemInfo() {
        String osName = System.getProperty("os.name");
        String osVersion = System.getProperty("os.version");
        String osArch = System.getProperty("os.arch");
        String javaVersion = System.getProperty("java.version");
        String javaVendor = System.getProperty("java.vendor");

        logInfo("System Information:");
        logInfo("  OS: " + osName + " " + osVersion);
        logInfo("  Architecture: " + osArch);
        logInfo("  Java Version: " + javaVersion);
        logInfo("  Java Vendor: " + javaVendor);
        logInfo("  Eclipse Platform: " + Platform.getProduct().getName());
    }

    // ==================== Logging Methods ====================

    /**
     * Log an info message.
     *
     * @param message the message to log
     */
    public static void logInfo(String message) {
        if (logger != null) {
            logger.log(new Status(Status.INFO, PLUGIN_ID, message));
        } else {
            System.out.println("[RunVSAgent INFO] " + message);
        }
    }

    /**
     * Log a warning message.
     *
     * @param message the message to log
     */
    public static void logWarning(String message) {
        if (logger != null) {
            logger.log(new Status(Status.WARNING, PLUGIN_ID, message));
        } else {
            System.out.println("[RunVSAgent WARNING] " + message);
        }
    }

    /**
     * Log an error message.
     *
     * @param message the message to log
     */
    public static void logError(String message) {
        logError(message, null);
    }

    /**
     * Log an error message with exception.
     *
     * @param message the message to log
     * @param e the exception
     */
    public static void logError(String message, Throwable e) {
        if (logger != null) {
            logger.log(new Status(Status.ERROR, PLUGIN_ID, message, e));
        } else {
            System.err.println("[RunVSAgent ERROR] " + message);
            if (e != null) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Log a debug message.
     *
     * @param message the message to log
     */
    public static void logDebug(String message) {
        // Only log in debug mode
        if (PluginContext.getInstance().isDebugMode()) {
            logInfo("[DEBUG] " + message);
        }
    }
}
