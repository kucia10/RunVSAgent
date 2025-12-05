/*
 * SPDX-FileCopyrightText: 2025 Weibo, Inc.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.sina.weibo.agent.eclipse.webview;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.widgets.Display;

import com.sina.weibo.agent.eclipse.Activator;

/**
 * Manages WebView instances and browser communication.
 * Handles message passing between extension host and browser views.
 */
public class WebViewManager {

    /** Registered browsers by view ID */
    private final Map<String, Browser> browsers = new ConcurrentHashMap<>();

    /** Message handlers by view ID */
    private final Map<String, Consumer<String>> messageHandlers = new ConcurrentHashMap<>();

    /** WebView creation callbacks */
    private final Map<String, Consumer<Browser>> creationCallbacks = new ConcurrentHashMap<>();

    /** HTML content cache by view ID */
    private final Map<String, String> htmlCache = new ConcurrentHashMap<>();

    /** Current theme config as JSON */
    private String themeConfigJson;

    /** Disposed flag */
    private boolean disposed = false;

    /**
     * Create a new WebViewManager.
     */
    public WebViewManager() {
        Activator.logInfo("WebViewManager created");
    }

    /**
     * Register a browser instance.
     *
     * @param viewId the view ID
     * @param browser the browser instance
     */
    public void registerBrowser(String viewId, Browser browser) {
        if (disposed) {
            Activator.logWarning("Cannot register browser - WebViewManager is disposed");
            return;
        }

        browsers.put(viewId, browser);
        Activator.logInfo("Browser registered: " + viewId);

        // Apply cached HTML if available
        String cachedHtml = htmlCache.get(viewId);
        if (cachedHtml != null) {
            setHtml(viewId, cachedHtml);
        }

        // Notify creation callback if registered
        Consumer<Browser> callback = creationCallbacks.get(viewId);
        if (callback != null) {
            callback.accept(browser);
        }
    }

    /**
     * Unregister a browser instance.
     *
     * @param viewId the view ID
     */
    public void unregisterBrowser(String viewId) {
        browsers.remove(viewId);
        messageHandlers.remove(viewId);
        htmlCache.remove(viewId);
        Activator.logInfo("Browser unregistered: " + viewId);
    }

    /**
     * Get a browser by view ID.
     *
     * @param viewId the view ID
     * @return the browser or null
     */
    public Browser getBrowser(String viewId) {
        return browsers.get(viewId);
    }

    /**
     * Check if a browser is registered.
     *
     * @param viewId the view ID
     * @return true if registered
     */
    public boolean hasBrowser(String viewId) {
        return browsers.containsKey(viewId);
    }

    /**
     * Register a message handler for a view.
     *
     * @param viewId the view ID
     * @param handler the message handler
     */
    public void registerMessageHandler(String viewId, Consumer<String> handler) {
        messageHandlers.put(viewId, handler);
    }

    /**
     * Register a browser creation callback.
     *
     * @param viewId the view ID
     * @param callback the callback
     */
    public void registerCreationCallback(String viewId, Consumer<Browser> callback) {
        creationCallbacks.put(viewId, callback);
        
        // If browser already exists, call callback immediately
        Browser browser = browsers.get(viewId);
        if (browser != null && !browser.isDisposed()) {
            callback.accept(browser);
        }
    }

    /**
     * Handle a message from a WebView.
     *
     * @param viewId the view ID
     * @param message the message
     */
    public void handleMessage(String viewId, String message) {
        Activator.logDebug("Message from view " + viewId + ": " + message);

        Consumer<String> handler = messageHandlers.get(viewId);
        if (handler != null) {
            handler.accept(message);
        }
    }

    /**
     * Post a message to a WebView.
     *
     * @param viewId the view ID
     * @param message the message
     */
    public void postMessage(String viewId, String message) {
        Browser browser = browsers.get(viewId);
        if (browser == null || browser.isDisposed()) {
            Activator.logWarning("Cannot post message - browser not found: " + viewId);
            return;
        }

        Display.getDefault().asyncExec(() -> {
            if (!browser.isDisposed()) {
                String escaped = escapeForJavaScript(message);
                String script = "window.postMessage('" + escaped + "', '*');";
                browser.execute(script);
            }
        });
    }

    /**
     * Post a message to all registered WebViews.
     *
     * @param message the message
     */
    public void postMessageToAll(String message) {
        for (String viewId : browsers.keySet()) {
            postMessage(viewId, message);
        }
    }

    /**
     * Set HTML content for a WebView.
     *
     * @param viewId the view ID
     * @param html the HTML content
     */
    public void setHtml(String viewId, String html) {
        // Cache the HTML
        htmlCache.put(viewId, html);

        Browser browser = browsers.get(viewId);
        if (browser == null || browser.isDisposed()) {
            Activator.logDebug("Browser not available, HTML cached for later: " + viewId);
            return;
        }

        Display.getDefault().asyncExec(() -> {
            if (!browser.isDisposed()) {
                browser.setText(html, true);
            }
        });
    }

    /**
     * Set options for a WebView.
     *
     * @param viewId the view ID
     * @param options the options map
     */
    public void setOptions(String viewId, Map<String, Object> options) {
        Browser browser = browsers.get(viewId);
        if (browser == null || browser.isDisposed()) {
            return;
        }

        // Handle specific options
        if (options.containsKey("enableScripts")) {
            // SWT Browser always has scripts enabled
        }

        if (options.containsKey("localResourceRoots")) {
            // Handle local resource roots
            // Note: SWT Browser doesn't have the same resource interception as JCEF
        }
    }

    /**
     * Execute JavaScript in a WebView.
     *
     * @param viewId the view ID
     * @param script the JavaScript to execute
     * @return true if executed
     */
    public boolean executeScript(String viewId, String script) {
        Browser browser = browsers.get(viewId);
        if (browser == null || browser.isDisposed()) {
            return false;
        }

        final boolean[] result = {false};
        Display.getDefault().syncExec(() -> {
            if (!browser.isDisposed()) {
                result[0] = browser.execute(script);
            }
        });
        return result[0];
    }

    /**
     * Inject theme configuration into all WebViews.
     *
     * @param themeConfigJson the theme config as JSON
     */
    public void injectTheme(String themeConfigJson) {
        this.themeConfigJson = themeConfigJson;

        String script = "if (window.applyTheme) { window.applyTheme(" + themeConfigJson + "); }";

        for (Browser browser : browsers.values()) {
            if (browser != null && !browser.isDisposed()) {
                Display.getDefault().asyncExec(() -> {
                    if (!browser.isDisposed()) {
                        browser.execute(script);
                    }
                });
            }
        }
    }

    /**
     * Get the current theme configuration.
     *
     * @return the theme config JSON or null
     */
    public String getThemeConfigJson() {
        return themeConfigJson;
    }

    /**
     * Escape a string for JavaScript.
     *
     * @param str the string to escape
     * @return the escaped string
     */
    private String escapeForJavaScript(String str) {
        if (str == null) {
            return "";
        }
        return str.replace("\\", "\\\\")
                .replace("'", "\\'")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    /**
     * Dispose the WebViewManager and release all resources.
     */
    public void dispose() {
        if (disposed) {
            return;
        }
        disposed = true;

        browsers.clear();
        messageHandlers.clear();
        creationCallbacks.clear();
        htmlCache.clear();

        Activator.logInfo("WebViewManager disposed");
    }

    /**
     * Check if disposed.
     *
     * @return true if disposed
     */
    public boolean isDisposed() {
        return disposed;
    }
}
