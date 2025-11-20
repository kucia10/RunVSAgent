package com.sina.weibo.agent.eclipse.wow.actions

import com.sina.weibo.agent.eclipse.wow.plugin.Activator
import org.eclipse.core.commands.AbstractHandler
import org.eclipse.core.commands.ExecutionEvent
import org.eclipse.core.runtime.ILog
import org.eclipse.core.runtime.Platform
import org.eclipse.ui.handlers.HandlerUtil
import org.eclipse.jface.dialogs.MessageDialog

class ExtensionSwitcherAction : AbstractHandler() {

    companion object {
        private val LOG: ILog = Platform.getLog(Activator.plugin?.bundle)
    }

    override fun execute(event: ExecutionEvent): Any? {
        LOG.info("Executing ExtensionSwitcherAction")
        val window = HandlerUtil.getActiveWorkbenchWindow(event)
        MessageDialog.openInformation(
            window.shell,
            "RunVSAgent-wow",
            "Switch Extension Provider action executed"
        )
        return null
    }
}
