package com.sina.weibo.agent.eclipse.neo.editor

import org.eclipse.ui.IEditorPart
import org.eclipse.ui.IPartListener2
import org.eclipse.ui.IWorkbenchPartReference

class EditorListener : IPartListener2 {

    override fun partOpened(partRef: IWorkbenchPartReference) {
        val part = partRef.getPart(false)
        if (part is IEditorPart) {
            println("Editor opened: ${part.title}")
            // Here we can add logic to handle the opened editor,
            // for example, sending information to the external process.
        }
    }

    // Other methods of IPartListener2 that we don't need to implement for now.
    override fun partActivated(partRef: IWorkbenchPartReference) {}
    override fun partBroughtToTop(partRef: IWorkbenchPartReference) {}
    override fun partClosed(partRef: IWorkbenchPartReference) {}
    override fun partDeactivated(partRef: IWorkbenchPartReference) {}
    override fun partHidden(partRef: IWorkbenchPartReference) {}
    override fun partVisible(partRef: IWorkbenchPartReference) {}
    override fun partInputChanged(partRef: IWorkbenchPartReference) {}
}
