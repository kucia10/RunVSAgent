package com.sina.weibo.agent.eclipse.wow.ui

import com.sina.weibo.agent.eclipse.wow.config.ExtensionConfigurationManager
import com.sina.weibo.agent.eclipse.wow.plugin.Activator
import org.eclipse.swt.SWT
import org.eclipse.swt.browser.Browser
import org.eclipse.swt.custom.StackLayout
import org.eclipse.swt.layout.FillLayout
import org.eclipse.swt.layout.GridData
import org.eclipse.swt.layout.GridLayout
import org.eclipse.swt.widgets.Composite
import org.eclipse.swt.widgets.Label
import org.eclipse.ui.part.ViewPart

class RunVSAgentViewPart : ViewPart() {

    private lateinit var parentComposite: Composite
    private lateinit var stackLayout: StackLayout
    private lateinit var browser: Browser
    private lateinit var infoComposite: Composite

    override fun createPartControl(parent: Composite) {
        parentComposite = parent
        stackLayout = StackLayout()
        parent.layout = stackLayout

        // Create the info composite
        infoComposite = Composite(parent, SWT.NONE)
        infoComposite.layout = GridLayout(1, false)
        val infoLabel = Label(infoComposite, SWT.WRAP)
        infoLabel.text = createSystemInfoText()
        infoLabel.layoutData = GridData(SWT.CENTER, SWT.CENTER, true, true)

        // Create the browser
        try {
            browser = Browser(parent, SWT.NONE)
        } catch (e: Exception) {
            // Handle browser creation failure
            val errorLabel = Label(infoComposite, SWT.NONE)
            errorLabel.text = "Error: Could not create the browser widget. Please check your Eclipse installation."
            e.printStackTrace()
        }

        // Initially show the info composite
        stackLayout.topControl = infoComposite
        parent.layout()

        // In a real scenario, we would have a mechanism to switch to the browser
        // For now, we'll just leave it on the info page
    }

    override fun setFocus() {
        parentComposite.setFocus()
    }

    fun showBrowser() {
        if (::browser.isInitialized) {
            stackLayout.topControl = browser
            parentComposite.layout()
        }
    }

    private fun createSystemInfoText(): String {
        val osName = System.getProperty("os.name")
        val osVersion = System.getProperty("os.version")
        val osArch = System.getProperty("os.arch")

        return """
            RunVSAgent-wow
            Initializing...

            System Information:
            OS: $osName $osVersion ($osArch)
            IDE: ${System.getProperty("eclipse.application")}
            Plugin Version: ${Activator.plugin?.bundle?.version}
        """.trimIndent()
    }
}
