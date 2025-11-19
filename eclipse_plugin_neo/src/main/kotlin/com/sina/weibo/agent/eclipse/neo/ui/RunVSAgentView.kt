package com.sina.weibo.agent.eclipse.neo.ui

import org.eclipse.swt.SWT
import org.eclipse.swt.browser.Browser
import org.eclipse.swt.widgets.Composite
import org.eclipse.ui.part.ViewPart

class RunVSAgentView : ViewPart() {

    private var browser: Browser? = null

    override fun createPartControl(parent: Composite) {
        browser = Browser(parent, SWT.NONE)
        // For now, let's load a simple placeholder URL.
        // Later, we will load the actual UI from the plugin's resources.
        browser?.url = "https://www.eclipse.org"
    }

    override fun setFocus() {
        browser?.setFocus()
    }
}
