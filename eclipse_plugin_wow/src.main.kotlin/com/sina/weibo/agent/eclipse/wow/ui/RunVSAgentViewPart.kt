package com.sina.weibo.agent.eclipse.wow.ui

import org.eclipse.swt.SWT
import org.eclipse.swt.browser.Browser
import org.eclipse.swt.layout.FillLayout
import org.eclipse.swt.widgets.Composite
import org.eclipse.ui.part.ViewPart

class RunVSAgentViewPart : ViewPart() {

    private var browser: Browser? = null

    override fun createPartControl(parent: Composite) {
        parent.layout = FillLayout()

        // For now, we'll create a simple browser instance.
        // In a real scenario, we would show a system info page first,
        // then load the main web view.
        try {
            browser = Browser(parent, SWT.NONE)
            // For now, load a blank page.
            browser?.text = "<html><body><h1>RunVSAgent-wow</h1><p>Initializing...</p></body></html>"
        } catch (e: Exception) {
            // Handle browser creation failure (e.g., missing dependencies)
            val label = org.eclipse.swt.widgets.Label(parent, SWT.NONE)
            label.text = "Error: Could not create the browser widget. Please check your Eclipse installation."
            e.printStackTrace()
        }
    }

    override fun setFocus() {
        browser?.setFocus()
    }

    fun getBrowser(): Browser? {
        return browser
    }
}
