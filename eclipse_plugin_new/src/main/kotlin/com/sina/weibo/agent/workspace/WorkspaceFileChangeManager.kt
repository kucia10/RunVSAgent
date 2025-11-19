// SPDX-FileCopyrightText: 2025 Weibo, Inc.
//
// SPDX-License-Identifier: Apache-2.0

package com.sina.weibo.agent.workspace

import com.sina.weibo.agent.events.*
import org.eclipse.core.resources.*
import org.eclipse.core.runtime.ILog
import org.eclipse.core.runtime.Platform
import java.util.concurrent.ConcurrentHashMap


/**
 * Workspace file change manager
 * Listens for creation, modification, deletion, and other changes of files in the workspace, and sends corresponding events
 */
class WorkspaceFileChangeManager : IResourceChangeListener {
    private val logger: ILog = Platform.getLog(Platform.getBundle("com.sina.weibo.agent"))
    private val projectWorkspacePaths = ConcurrentHashMap<IProject, String>()
    private val eventBus = EventBus()


    init {
        logger.info("Initialize workspace file change manager")
        ResourcesPlugin.getWorkspace().addResourceChangeListener(this, IResourceChangeEvent.POST_CHANGE)
    }

    override fun resourceChanged(event: IResourceChangeEvent) {
        try {
            event.delta?.accept(object : IResourceDeltaVisitor {
                override fun visit(delta: IResourceDelta): Boolean {
                    val resource = delta.resource
                    val project = resource.project
                    val changeType = when (delta.kind) {
                        IResourceDelta.ADDED -> FileChangeType.CREATED
                        IResourceDelta.REMOVED -> FileChangeType.DELETED
                        IResourceDelta.CHANGED -> FileChangeType.UPDATED
                        else -> return true
                    }

                    if (isRelevantFileSystemItem(resource, project)) {
                        val changeData = WorkspaceFileChangeData(resource, changeType)
                        if (resource.type == IResource.FILE) {
                            triggerFileChangeEvent(changeData)
                        } else if (resource.type == IResource.FOLDER) {
                            triggerDirectoryChangeEvent(changeData)
                        }
                    }
                    return true
                }
            })
        } catch (e: Exception) {
            logger.error("Error processing resource change", e)
        }
    }

    private fun triggerFileChangeEvent(fileChangeData: WorkspaceFileChangeData) {
        logger.info("File changed: ${fileChangeData.resource.fullPath}, type: ${fileChangeData.changeType}")
        eventBus.emitInUI(WorkspaceFileChangeEvent, fileChangeData)
    }

    private fun triggerDirectoryChangeEvent(directoryChangeData: WorkspaceFileChangeData) {
        logger.info("Directory changed: ${directoryChangeData.resource.fullPath}, type: ${directoryChangeData.changeType}")
        eventBus.emitInUI(WorkspaceDirectoryChangeEvent, directoryChangeData)
    }

    private fun isRelevantFileSystemItem(resource: IResource, project: IProject): Boolean {
        // Ignore hidden files and directories
        if (resource.name.startsWith(".") || resource.fullPath.toOSString().contains("/.")) {
            return false
        }

        // For files, ignore temporary files
        if (resource.type == IResource.FILE && (resource.name.endsWith("~") || resource.name.endsWith(".tmp"))) {
            return false
        }

        return true
    }

    fun dispose() {
        logger.info("Release workspace file change manager resources")
        ResourcesPlugin.getWorkspace().removeResourceChangeListener(this)
    }
}
