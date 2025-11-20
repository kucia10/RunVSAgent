package com.sina.weibo.agent.eclipse.wow.editor

import com.sina.weibo.agent.eclipse.wow.plugin.Activator
import org.eclipse.core.runtime.ILog
import org.eclipse.core.runtime.Platform
import org.eclipse.ui.IEditorPart
import org.eclipse.ui.IWorkbenchPartReference
import org.eclipse.ui.IPartListener2

class EditorListener : IPartListener2 {

    companion object {
        private val LOG: ILog = Platform.getLog(Activator.plugin?.bundle)
    }

    override fun partOpened(partRef: IWorkbenchPartReference) {
        val part = partRef.getPart(false)
        if (part is IEditorPart) {
            LOG.info("Editor opened: " + part.title)
        }
    }

    override fun partClosed(partRef: IWorkbenchPartReference) {
        val part = partRef.getPart(false)
        if (part is IEditorPart) {
            LOG.info("Editor closed: " + part.title)
        }
    }

    // We don't need to implement the other methods for now
    override fun partActivated(partRef: IWorkbenchPartReference) {}
    override fun partBroughtToTop(partRef: IWorkbenchPartReference) {}
    override fun partDeactivated(partRef: IWorkbenchPartReference) {}
    override fun partHidden(partRef: IWorkbenchPartReference) {}
    override fun partVisible(partRef: IWorkbenchPartReference) {}
    override fun partInputChanged(partRef: IWorkbenchPartReference) {}
}
