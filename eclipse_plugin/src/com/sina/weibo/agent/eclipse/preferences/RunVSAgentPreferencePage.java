/*
 * SPDX-FileCopyrightText: 2025 Weibo, Inc.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.sina.weibo.agent.eclipse.preferences;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.FileFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.sina.weibo.agent.eclipse.Activator;

/**
 * Preference page for RunVSAgent plugin settings.
 */
public class RunVSAgentPreferencePage extends FieldEditorPreferencePage 
        implements IWorkbenchPreferencePage {

    /**
     * Create the preference page.
     */
    public RunVSAgentPreferencePage() {
        super(GRID);
        setPreferenceStore(Activator.getDefault().getPreferenceStore());
        setDescription("RunVSAgent Plugin Settings");
    }

    @Override
    public void init(IWorkbench workbench) {
        // Initialize from workbench
    }

    @Override
    protected void createFieldEditors() {
        // Extension provider selection
        addField(new ComboFieldEditor(
                PreferenceConstants.P_EXTENSION_PROVIDER,
                "Extension Provider:",
                new String[][] {
                        {"Roo Code", "roo-code"},
                        {"Cline", "cline"},
                        {"Custom", "custom"}
                },
                getFieldEditorParent()
        ));

        // Node.js path
        addField(new FileFieldEditor(
                PreferenceConstants.P_NODE_PATH,
                "Node.js Path:",
                getFieldEditorParent()
        ));

        // Custom extension path
        addField(new DirectoryFieldEditor(
                PreferenceConstants.P_CUSTOM_EXTENSION_PATH,
                "Custom Extension Path:",
                getFieldEditorParent()
        ));

        // Debug mode
        addField(new BooleanFieldEditor(
                PreferenceConstants.P_DEBUG_MODE,
                "Enable Debug Mode",
                getFieldEditorParent()
        ));

        // Auto-start extension host
        addField(new BooleanFieldEditor(
                PreferenceConstants.P_AUTO_START,
                "Auto-start Extension Host on Startup",
                getFieldEditorParent()
        ));

        // Socket server port (0 for auto)
        addField(new StringFieldEditor(
                PreferenceConstants.P_SERVER_PORT,
                "Server Port (0 for auto):",
                getFieldEditorParent()
        ));
    }
}
