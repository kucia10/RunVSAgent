/*
 * SPDX-FileCopyrightText: 2025 Weibo, Inc.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.sina.weibo.agent.eclipse.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.handlers.HandlerUtil;

import com.sina.weibo.agent.eclipse.Activator;
import com.sina.weibo.agent.eclipse.ui.views.RunVSAgentView;

/**
 * Handler for opening the RunVSAgent view.
 */
public class OpenViewHandler extends AbstractHandler {

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
        IWorkbenchPage page = window.getActivePage();
        
        if (page != null) {
            try {
                page.showView(RunVSAgentView.ID);
                Activator.logInfo("RunVSAgent view opened");
            } catch (PartInitException e) {
                Activator.logError("Failed to open RunVSAgent view", e);
                throw new ExecutionException("Failed to open RunVSAgent view", e);
            }
        }
        
        return null;
    }
}
