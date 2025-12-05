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
import com.sina.weibo.agent.eclipse.core.PluginContext;

/**
 * Handler for switching between extension providers.
 */
public class SwitchExtensionHandler extends AbstractHandler {

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        Activator.logInfo("Switch extension handler triggered");

        // Show extension selection dialog
        String[] extensions = {"roo-code", "cline", "custom"};
        String currentExtension = PluginContext.getInstance().getCurrentExtensionId();
        
        int currentIndex = 0;
        for (int i = 0; i < extensions.length; i++) {
            if (extensions[i].equals(currentExtension)) {
                currentIndex = i;
                break;
            }
        }

        // For now, just cycle through extensions
        int nextIndex = (currentIndex + 1) % extensions.length;
        String newExtension = extensions[nextIndex];

        // Update context
        PluginContext.getInstance().setCurrentExtensionId(newExtension);

        // Show notification
        MessageDialog.openInformation(
                HandlerUtil.getActiveShell(event),
                "Extension Switched",
                "Switched to extension: " + newExtension + 
                "\n\nNote: You may need to restart the extension host for changes to take effect."
        );

        Activator.logInfo("Switched to extension: " + newExtension);
        return null;
    }
}
