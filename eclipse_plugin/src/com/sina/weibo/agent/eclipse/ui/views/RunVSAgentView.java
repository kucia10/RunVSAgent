/*
 * SPDX-FileCopyrightText: 2025 Weibo, Inc.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.sina.weibo.agent.eclipse.ui.views;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.BrowserFunction;
import org.eclipse.swt.browser.ProgressEvent;
import org.eclipse.swt.browser.ProgressListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.part.ViewPart;

import com.sina.weibo.agent.eclipse.Activator;
import com.sina.weibo.agent.eclipse.core.PluginContext;
import com.sina.weibo.agent.eclipse.webview.WebViewManager;

/**
 * Main view for RunVSAgent Eclipse plugin.
 * This view displays the WebView that hosts the VSCode extension UI.
 */
public class RunVSAgentView extends ViewPart {

    /** View ID */
    public static final String ID = "com.sina.weibo.agent.eclipse.views.RunVSAgentView";

    /** Parent composite */
    private Composite parent;

    /** Main content composite */
    private Composite contentComposite;

    /** SWT Browser widget */
    private Browser browser;

    /** Placeholder composite shown during initialization */
    private Composite placeholderComposite;

    /** Status label */
    private Label statusLabel;

    /** Browser ready flag */
    private boolean browserReady = false;

    /** Page loaded flag */
    private boolean pageLoaded = false;

    /** Refresh action */
    private Action refreshAction;

    /** Switch extension action */
    private Action switchExtensionAction;

    /** Status check action */
    private Action statusCheckAction;

    @Override
    public void createPartControl(Composite parent) {
        this.parent = parent;
        parent.setLayout(new FillLayout());

        Activator.logInfo("Creating RunVSAgentView...");

        // Create main content composite
        contentComposite = new Composite(parent, SWT.NONE);
        contentComposite.setLayout(new GridLayout(1, false));

        // Initially show placeholder
        showPlaceholder();

        // Setup toolbar and menu actions
        createActions();
        contributeToActionBars();

        // Initialize WebView manager and browser
        initializeBrowser();

        Activator.logInfo("RunVSAgentView created");
    }

    /**
     * Show placeholder content during initialization.
     */
    private void showPlaceholder() {
        if (placeholderComposite != null && !placeholderComposite.isDisposed()) {
            return;
        }

        placeholderComposite = new Composite(contentComposite, SWT.NONE);
        placeholderComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        placeholderComposite.setLayout(new GridLayout(1, false));

        // Title
        Label titleLabel = new Label(placeholderComposite, SWT.CENTER);
        titleLabel.setText("🚀 RunVSAgent");
        titleLabel.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false));

        // Subtitle
        Label subtitleLabel = new Label(placeholderComposite, SWT.CENTER);
        subtitleLabel.setText("Initializing...");
        subtitleLabel.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false));

        // System info
        Label infoLabel = new Label(placeholderComposite, SWT.CENTER | SWT.WRAP);
        infoLabel.setText(getSystemInfoText());
        infoLabel.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true));

        // Status label
        statusLabel = new Label(placeholderComposite, SWT.CENTER);
        statusLabel.setText("Starting extension host...");
        statusLabel.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false));

        contentComposite.layout(true, true);
    }

    /**
     * Get system information text.
     *
     * @return system info text
     */
    private String getSystemInfoText() {
        StringBuilder sb = new StringBuilder();
        sb.append("System Information\n");
        sb.append("==================\n\n");
        sb.append("OS: ").append(System.getProperty("os.name")).append(" ")
                .append(System.getProperty("os.version")).append("\n");
        sb.append("Architecture: ").append(System.getProperty("os.arch")).append("\n");
        sb.append("Java Version: ").append(System.getProperty("java.version")).append("\n");
        sb.append("Eclipse Platform: ").append(Platform.getProduct() != null ? 
                Platform.getProduct().getName() : "Unknown").append("\n");
        sb.append("Plugin Version: ").append(Activator.VERSION).append("\n");
        return sb.toString();
    }

    /**
     * Hide placeholder and show browser.
     */
    private void hidePlaceholder() {
        Display.getDefault().asyncExec(() -> {
            if (placeholderComposite != null && !placeholderComposite.isDisposed()) {
                placeholderComposite.dispose();
                placeholderComposite = null;
            }

            if (browser != null && !browser.isDisposed()) {
                browser.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
                browser.setVisible(true);
            }

            contentComposite.layout(true, true);
        });
    }

    /**
     * Update status text.
     *
     * @param status the status message
     */
    public void updateStatus(String status) {
        Display.getDefault().asyncExec(() -> {
            if (statusLabel != null && !statusLabel.isDisposed()) {
                statusLabel.setText(status);
            }
        });
    }

    /**
     * Initialize the SWT Browser widget.
     */
    private void initializeBrowser() {
        try {
            // Create browser with WebKit style if available
            int style = SWT.NONE;
            
            // On Windows, try to use Edge WebView2
            if (Platform.getOS().equals(Platform.OS_WIN32)) {
                // SWT.EDGE style for Edge WebView2 (requires SWT 4.21+)
                try {
                    style = SWT.class.getField("EDGE").getInt(null);
                } catch (Exception e) {
                    // Fall back to default
                    style = SWT.NONE;
                }
            }

            browser = new Browser(contentComposite, style);
            browser.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
            browser.setVisible(false); // Hidden until page loads

            // Setup JavaScript bridge
            setupJavaScriptBridge();

            // Add progress listener
            browser.addProgressListener(new ProgressListener() {
                @Override
                public void completed(ProgressEvent event) {
                    Activator.logInfo("Browser page loaded");
                    pageLoaded = true;
                    onPageLoaded();
                }

                @Override
                public void changed(ProgressEvent event) {
                    // Progress update
                }
            });

            browserReady = true;
            Activator.logInfo("Browser initialized successfully");

            // Register with WebView manager
            WebViewManager webViewManager = PluginContext.getInstance().getWebViewManager();
            webViewManager.registerBrowser(ID, browser);

            // Load initial content
            loadInitialContent();

        } catch (Exception e) {
            Activator.logError("Failed to initialize browser", e);
            showBrowserError(e);
        }
    }

    /**
     * Setup JavaScript bridge for communication between browser and plugin.
     */
    private void setupJavaScriptBridge() {
        // Function for posting messages from JavaScript to Java
        new BrowserFunction(browser, "postMessageToHost") {
            @Override
            public Object function(Object[] arguments) {
                if (arguments.length > 0) {
                    String message = arguments[0].toString();
                    handleMessageFromWebView(message);
                }
                return null;
            }
        };

        // Function for logging from JavaScript
        new BrowserFunction(browser, "logFromWebView") {
            @Override
            public Object function(Object[] arguments) {
                if (arguments.length > 0) {
                    String level = arguments.length > 1 ? arguments[0].toString() : "info";
                    String message = arguments.length > 1 ? arguments[1].toString() : arguments[0].toString();
                    
                    switch (level.toLowerCase()) {
                        case "error":
                            Activator.logError("[WebView] " + message);
                            break;
                        case "warn":
                        case "warning":
                            Activator.logWarning("[WebView] " + message);
                            break;
                        default:
                            Activator.logInfo("[WebView] " + message);
                            break;
                    }
                }
                return null;
            }
        };

        // Function for getting theme info
        new BrowserFunction(browser, "getThemeInfo") {
            @Override
            public Object function(Object[] arguments) {
                return isDarkTheme() ? "dark" : "light";
            }
        };

        Activator.logInfo("JavaScript bridge setup complete");
    }

    /**
     * Handle message received from WebView.
     *
     * @param message the message
     */
    private void handleMessageFromWebView(String message) {
        Activator.logDebug("Message from WebView: " + message);
        
        // Forward to WebView manager for processing
        WebViewManager webViewManager = PluginContext.getInstance().getWebViewManager();
        webViewManager.handleMessage(ID, message);
    }

    /**
     * Post a message to the WebView.
     *
     * @param message the message
     */
    public void postMessageToWebView(String message) {
        if (browser == null || browser.isDisposed()) {
            Activator.logWarning("Cannot post message - browser not available");
            return;
        }

        Display.getDefault().asyncExec(() -> {
            if (browser != null && !browser.isDisposed()) {
                String escaped = message.replace("\\", "\\\\")
                        .replace("'", "\\'")
                        .replace("\n", "\\n")
                        .replace("\r", "\\r");
                String script = "window.postMessage('" + escaped + "', '*');";
                browser.execute(script);
            }
        });
    }

    /**
     * Load initial HTML content into browser.
     */
    private void loadInitialContent() {
        String html = generateInitialHtml();
        browser.setText(html, true);
    }

    /**
     * Generate initial HTML content.
     *
     * @return the HTML content
     */
    private String generateInitialHtml() {
        boolean isDark = isDarkTheme();
        
        return "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                "    <title>RunVSAgent</title>\n" +
                "    <style>\n" +
                "        body {\n" +
                "            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;\n" +
                "            margin: 0;\n" +
                "            padding: 20px;\n" +
                "            background: " + (isDark ? "#1e1e1e" : "#ffffff") + ";\n" +
                "            color: " + (isDark ? "#cccccc" : "#333333") + ";\n" +
                "            height: 100vh;\n" +
                "            box-sizing: border-box;\n" +
                "            display: flex;\n" +
                "            flex-direction: column;\n" +
                "            align-items: center;\n" +
                "            justify-content: center;\n" +
                "        }\n" +
                "        .container {\n" +
                "            text-align: center;\n" +
                "            max-width: 400px;\n" +
                "        }\n" +
                "        .title {\n" +
                "            font-size: 24px;\n" +
                "            margin-bottom: 10px;\n" +
                "        }\n" +
                "        .subtitle {\n" +
                "            font-size: 14px;\n" +
                "            opacity: 0.8;\n" +
                "            margin-bottom: 20px;\n" +
                "        }\n" +
                "        .loading {\n" +
                "            display: inline-block;\n" +
                "            width: 20px;\n" +
                "            height: 20px;\n" +
                "            border: 2px solid " + (isDark ? "#333" : "#ddd") + ";\n" +
                "            border-top-color: " + (isDark ? "#007acc" : "#0066cc") + ";\n" +
                "            border-radius: 50%;\n" +
                "            animation: spin 1s linear infinite;\n" +
                "        }\n" +
                "        @keyframes spin {\n" +
                "            to { transform: rotate(360deg); }\n" +
                "        }\n" +
                "        .status {\n" +
                "            margin-top: 10px;\n" +
                "            font-size: 12px;\n" +
                "            opacity: 0.7;\n" +
                "        }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <div class=\"container\">\n" +
                "        <div class=\"title\">🚀 RunVSAgent</div>\n" +
                "        <div class=\"subtitle\">Run VSCode Extensions in Eclipse</div>\n" +
                "        <div class=\"loading\"></div>\n" +
                "        <div class=\"status\" id=\"status\">Connecting to extension host...</div>\n" +
                "    </div>\n" +
                "    <script>\n" +
                "        // Setup message handling\n" +
                "        window.addEventListener('message', function(event) {\n" +
                "            try {\n" +
                "                var data = JSON.parse(event.data);\n" +
                "                handleMessage(data);\n" +
                "            } catch (e) {\n" +
                "                console.error('Error handling message:', e);\n" +
                "            }\n" +
                "        });\n" +
                "\n" +
                "        function handleMessage(data) {\n" +
                "            if (data.type === 'updateStatus') {\n" +
                "                document.getElementById('status').textContent = data.status;\n" +
                "            }\n" +
                "        }\n" +
                "\n" +
                "        // Notify host that page is ready\n" +
                "        if (typeof postMessageToHost === 'function') {\n" +
                "            postMessageToHost(JSON.stringify({type: 'ready'}));\n" +
                "        }\n" +
                "    </script>\n" +
                "</body>\n" +
                "</html>";
    }

    /**
     * Called when the page has finished loading.
     */
    private void onPageLoaded() {
        // Start extension host initialization
        PluginContext context = PluginContext.getInstance();
        if (context.isInitialized()) {
            // Hide placeholder and show browser
            hidePlaceholder();
        }
    }

    /**
     * Load HTML content into the browser.
     *
     * @param html the HTML content
     */
    public void loadHtml(String html) {
        if (browser == null || browser.isDisposed()) {
            return;
        }

        Display.getDefault().asyncExec(() -> {
            if (browser != null && !browser.isDisposed()) {
                browser.setText(html, true);
            }
        });
    }

    /**
     * Load a URL into the browser.
     *
     * @param url the URL to load
     */
    public void loadUrl(String url) {
        if (browser == null || browser.isDisposed()) {
            return;
        }

        Display.getDefault().asyncExec(() -> {
            if (browser != null && !browser.isDisposed()) {
                browser.setUrl(url);
            }
        });
    }

    /**
     * Execute JavaScript in the browser.
     *
     * @param script the JavaScript to execute
     * @return true if successful
     */
    public boolean executeJavaScript(String script) {
        if (browser == null || browser.isDisposed()) {
            return false;
        }

        return browser.execute(script);
    }

    /**
     * Show browser error message.
     *
     * @param e the exception
     */
    private void showBrowserError(Exception e) {
        Display.getDefault().asyncExec(() -> {
            if (statusLabel != null && !statusLabel.isDisposed()) {
                statusLabel.setText("Browser initialization failed: " + e.getMessage());
            }
        });
    }

    /**
     * Check if Eclipse is using a dark theme.
     *
     * @return true if dark theme
     */
    private boolean isDarkTheme() {
        Display display = Display.getDefault();
        org.eclipse.swt.graphics.Color bg = display.getSystemColor(SWT.COLOR_WIDGET_BACKGROUND);
        double brightness = (0.299 * bg.getRed() + 0.587 * bg.getGreen() + 0.114 * bg.getBlue()) / 255.0;
        return brightness < 0.5;
    }

    /**
     * Create toolbar and menu actions.
     */
    private void createActions() {
        // Refresh action
        refreshAction = new Action("Refresh") {
            @Override
            public void run() {
                if (browser != null && !browser.isDisposed()) {
                    browser.refresh();
                }
            }
        };
        refreshAction.setToolTipText("Refresh the view");
        refreshAction.setImageDescriptor(Activator.getImageDescriptor("icons/refresh-16.png"));

        // Switch extension action
        switchExtensionAction = new Action("Switch Extension") {
            @Override
            public void run() {
                // TODO: Implement extension switching
                Activator.logInfo("Switch extension action triggered");
            }
        };
        switchExtensionAction.setToolTipText("Switch to a different extension");
        switchExtensionAction.setImageDescriptor(Activator.getImageDescriptor("icons/switch-16.png"));

        // Status check action
        statusCheckAction = new Action("Check Status") {
            @Override
            public void run() {
                // TODO: Implement status check
                Activator.logInfo("Status check action triggered");
            }
        };
        statusCheckAction.setToolTipText("Check extension status");
        statusCheckAction.setImageDescriptor(Activator.getImageDescriptor("icons/status-16.png"));
    }

    /**
     * Contribute to action bars.
     */
    private void contributeToActionBars() {
        IActionBars bars = getViewSite().getActionBars();
        fillLocalPullDown(bars.getMenuManager());
        fillLocalToolBar(bars.getToolBarManager());
    }

    /**
     * Fill local pull-down menu.
     *
     * @param manager the menu manager
     */
    private void fillLocalPullDown(IMenuManager manager) {
        manager.add(refreshAction);
        manager.add(new Separator());
        manager.add(switchExtensionAction);
        manager.add(statusCheckAction);
    }

    /**
     * Fill local toolbar.
     *
     * @param manager the toolbar manager
     */
    private void fillLocalToolBar(IToolBarManager manager) {
        manager.add(refreshAction);
        manager.add(new Separator());
        manager.add(switchExtensionAction);
        manager.add(statusCheckAction);
    }

    @Override
    public void setFocus() {
        if (browser != null && !browser.isDisposed()) {
            browser.setFocus();
        } else if (contentComposite != null && !contentComposite.isDisposed()) {
            contentComposite.setFocus();
        }
    }

    @Override
    public void dispose() {
        Activator.logInfo("Disposing RunVSAgentView...");

        // Unregister from WebView manager
        WebViewManager webViewManager = PluginContext.getInstance().getWebViewManager();
        if (webViewManager != null) {
            webViewManager.unregisterBrowser(ID);
        }

        // Dispose browser
        if (browser != null && !browser.isDisposed()) {
            browser.dispose();
            browser = null;
        }

        super.dispose();
        Activator.logInfo("RunVSAgentView disposed");
    }

    /**
     * Get the browser widget.
     *
     * @return the browser
     */
    public Browser getBrowser() {
        return browser;
    }

    /**
     * Check if browser is ready.
     *
     * @return true if ready
     */
    public boolean isBrowserReady() {
        return browserReady;
    }

    /**
     * Check if page is loaded.
     *
     * @return true if loaded
     */
    public boolean isPageLoaded() {
        return pageLoaded;
    }
}
