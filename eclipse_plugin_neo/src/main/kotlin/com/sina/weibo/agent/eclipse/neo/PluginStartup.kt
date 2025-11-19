package com.sina.weibo.agent.eclipse.neo

import com.sina.weibo.agent.eclipse.neo.editor.EditorListener
import org.eclipse.ui.IStartup
import org.eclipse.ui.PlatformUI

class PluginStartup : IStartup {
    override fun earlyStartup() {
        // This method is called on workbench startup
        // We will initialize our plugin's core logic here.
        println("RunVSAgent Eclipse Plugin (Neo) starting up...")

        // Get the service instance from the Activator and initialize it
        val service = Activator.INSTANCE?.getService()
        service?.initialize()

        // Register the editor listener
        PlatformUI.getWorkbench().display.asyncExec {
            val window = PlatformUI.getWorkbench().activeWorkbenchWindow
            window?.partService?.addPartListener(EditorListener())
        }
    }
}
