package com.sina.weibo.agent.eclipse.neo.ui.actions

import org.eclipse.core.commands.AbstractHandler
import org.eclipse.core.commands.ExecutionEvent
import org.eclipse.core.commands.IHandler
import org.eclipse.ui.handlers.HandlerUtil

class ExtensionSwitcherAction : AbstractHandler(), IHandler {

    override fun execute(event: ExecutionEvent): Any? {
        val window = HandlerUtil.getActiveWorkbenchWindow(event)
        println("Switch Extension Provider action executed!")
        // Here we can open a dialog or a view to switch extensions.
        return null
    }
}
