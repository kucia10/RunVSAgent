// SPDX-FileCopyrightText: 2025 Weibo, Inc.
//
// SPDX-License-Identifier: Apache-2.0

package com.sina.weibo.agent.actions

import org.eclipse.jface.action.Action
import org.eclipse.core.runtime.Platform
import org.osgi.framework.Bundle
import org.osgi.framework.FrameworkUtil

/**
 * Executes a VSCode command with the given command ID.
 * This function uses the RPC protocol to communicate with the extension host.
 *
 * @param commandId The identifier of the command to execute
 * @param project The current project context
 */
fun executeCommand(commandId: String, vararg args: Any?, hasArgs: Boolean? = true) {
    val bundle = FrameworkUtil.getBundle(VSCodeCommandActions::class.java)
    val logger = Platform.getLog(bundle)
    logger.info("🔍 executeCommand called with commandId: $commandId")

    try {
        // TODO: Implement service lookup in Eclipse.
        val pluginContext = null
        if (pluginContext == null) {
            logger.warn("❌ PluginContext not found")
            return
        }

        val rpcProtocol = pluginContext.getRPCProtocol()
        if (rpcProtocol == null) {
            logger.warn("❌ RPC Protocol not found")
            return
        }

        val proxy = rpcProtocol.getProxy(com.sina.weibo.agent.core.ServiceProxyRegistry.ExtHostContext.ExtHostCommands)
        if (proxy == null) {
            logger.warn("❌ ExtHostCommands proxy not found")
            return
        }

        logger.info("🔍 Executing command via RPC: $commandId, argsCount=${args.size}")
        if (hasArgs == true) {
            proxy.executeContributedCommand(commandId, args)
        } else {
            proxy.executeContributedCommand(commandId)
        }

        logger.info("✅ Command sent to Extension Host: $commandId")

    } catch (e: Exception) {
        logger.error("❌ Error executing command: $commandId", e)
    }
}

/**
 * Action that opens developer tools for the WebView.
 * Takes a function that provides the current WebView instance.
 *
 * @property getWebViewInstance Function that returns the current WebView instance or null if not available
 */
class OpenDevToolsAction(private val getWebViewInstance: () -> com.sina.weibo.agent.webview.WebViewInstance?) :
    Action("Open Developer Tools") {
    private val bundle = FrameworkUtil.getBundle(OpenDevToolsAction::class.java)
    private val logger = Platform.getLog(bundle)

    /**
     * Performs the action to open developer tools for the WebView.
     */
    override fun run() {
        val webView = getWebViewInstance()
        if (webView != null) {
            webView.openDevTools()
        } else {
            logger.warn("No WebView instance available, cannot open developer tools")
        }
    }
}
