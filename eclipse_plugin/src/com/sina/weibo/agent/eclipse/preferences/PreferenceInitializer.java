/*
 * SPDX-FileCopyrightText: 2025 Weibo, Inc.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.sina.weibo.agent.eclipse.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import com.sina.weibo.agent.eclipse.Activator;

/**
 * Initializes default preference values.
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {

    @Override
    public void initializeDefaultPreferences() {
        IPreferenceStore store = Activator.getDefault().getPreferenceStore();

        // Default extension provider
        store.setDefault(PreferenceConstants.P_EXTENSION_PROVIDER, "roo-code");

        // Node.js path (empty = auto-detect)
        store.setDefault(PreferenceConstants.P_NODE_PATH, "");

        // Custom extension path
        store.setDefault(PreferenceConstants.P_CUSTOM_EXTENSION_PATH, "");

        // Debug mode (default off)
        store.setDefault(PreferenceConstants.P_DEBUG_MODE, false);

        // Auto-start (default on)
        store.setDefault(PreferenceConstants.P_AUTO_START, true);

        // Server port (0 = auto)
        store.setDefault(PreferenceConstants.P_SERVER_PORT, "0");
    }
}
