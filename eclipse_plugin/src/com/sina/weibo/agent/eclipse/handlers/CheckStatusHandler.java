/*
 * SPDX-FileCopyrightText: 2025 Weibo, Inc.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.sina.weibo.agent.eclipse.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.handlers.HandlerUtil;

import com.sina.weibo.agent.eclipse.Activator;
import com.sina.weibo.agent.eclipse.core.ExtensionHostManager;
import com.sina.weibo.agent.eclipse.core.ExtensionProcessManager;
import com.sina.weibo.agent.eclipse.core.ExtensionSocketServer;
import com.sina.weibo.agent.eclipse.core.PluginContext;

/**
 * Handler for checking extension status.
 */
public class CheckStatusHandler extends AbstractHandler {

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        Activator.logInfo("Check status handler triggered");

        PluginContext context = PluginContext.getInstance();
        
        StringBuilder status = new StringBuilder();
        status.append("RunVSAgent Status Report\n");
        status.append("========================\n\n");

        // Plugin status
        status.append("Plugin Status:\n");
        status.append("  Version: ").append(Activator.VERSION).append("\n");
        status.append("  Initialized: ").append(context.isInitialized()).append("\n");
        status.append("  Debug Mode: ").append(context.isDebugMode()).append("\n");
        status.append("  Current Extension: ").append(context.getCurrentExtensionId()).append("\n\n");

        // Socket server status
        ExtensionSocketServer socketServer = context.getExtensionSocketServer();
        status.append("Socket Server:\n");
        if (socketServer != null) {
            status.append("  Running: ").append(socketServer.isRunning()).append("\n");
            status.append("  Port: ").append(socketServer.getPort()).append("\n");
        } else {
            status.append("  Not initialized\n");
        }
        status.append("\n");

        // Process manager status
        ExtensionProcessManager processManager = context.getExtensionProcessManager();
        status.append("Extension Process:\n");
        if (processManager != null) {
            status.append("  Running: ").append(processManager.isRunning()).append("\n");
            status.append("  Node Path: ").append(processManager.getNodePath() != null ? 
                    processManager.getNodePath() : "Not found").append("\n");
        } else {
            status.append("  Not initialized\n");
        }
        status.append("\n");

        // Extension host status
        ExtensionHostManager hostManager = context.getExtensionHostManager();
        status.append("Extension Host:\n");
        if (hostManager != null) {
            status.append("  Ready: ").append(hostManager.isReady()).append("\n");
            status.append("  Initialized: ").append(hostManager.isInitialized()).append("\n");
            status.append("  State: ").append(hostManager.getResponsiveState()).append("\n");
        } else {
            status.append("  Not connected\n");
        }
        status.append("\n");

        // System info
        status.append("System Information:\n");
        status.append("  OS: ").append(System.getProperty("os.name")).append(" ")
                .append(System.getProperty("os.version")).append("\n");
        status.append("  Architecture: ").append(System.getProperty("os.arch")).append("\n");
        status.append("  Java: ").append(System.getProperty("java.version")).append("\n");

        // Show dialog
        MessageDialog.openInformation(
                HandlerUtil.getActiveShell(event),
                "RunVSAgent Status",
                status.toString()
        );

        return null;
    }
}
