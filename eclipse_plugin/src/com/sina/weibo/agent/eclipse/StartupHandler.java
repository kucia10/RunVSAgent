/*
 * SPDX-FileCopyrightText: 2025 Weibo, Inc.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.sina.weibo.agent.eclipse;

import org.eclipse.ui.IStartup;

import com.sina.weibo.agent.eclipse.core.PluginContext;

/**
 * Startup handler for early plugin initialization.
 * This class is called during Eclipse startup to initialize
 * the RunVSAgent plugin components.
 */
public class StartupHandler implements IStartup {

    /**
     * Called during early startup of the workbench.
     * This method is called in a background thread.
     */
    @Override
    public void earlyStartup() {
        Activator.logInfo("RunVSAgent earlyStartup called");

        try {
            // Ensure plugin context is initialized
            PluginContext context = PluginContext.getInstance();
            
            if (!context.isInitialized()) {
                Activator.logWarning("PluginContext not yet initialized during earlyStartup");
                return;
            }

            // Log startup completion
            Activator.logInfo("RunVSAgent earlyStartup completed");

            // Additional initialization can be done here
            // Note: UI components should not be created here as workbench may not be ready
            
        } catch (Exception e) {
            Activator.logError("Error during earlyStartup", e);
        }
    }
}
