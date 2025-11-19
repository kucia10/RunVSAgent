// SPDX-FileCopyrightText: 2025 Weibo, Inc.
//
// SPDX-License-Identifier: Apache-2.0

package com.sina.weibo.agent.events

import org.eclipse.core.resources.IProject
import org.eclipse.core.resources.IResource

/**
 * File change type
 */
enum class FileChangeType {
    CREATED,
    UPDATED,
    DELETED
}

/**
 * File system item type
 */
enum class FileSystemItemType {
    FILE,
    DIRECTORY
}

/**
 * Workspace file change event type
 */
object WorkspaceFileChangeEvent : EventType<WorkspaceFileChangeData>

/**
 * Workspace directory change event type
 */
object WorkspaceDirectoryChangeEvent : EventType<WorkspaceFileChangeData>

/**
 * Workspace file change data
 */
data class WorkspaceFileChangeData(
    val resource: IResource,
    val changeType: FileChangeType,
    val timestamp: Long = System.currentTimeMillis(),
    val itemType: FileSystemItemType = if (resource.type == IResource.FOLDER) FileSystemItemType.DIRECTORY else FileSystemItemType.FILE
)

/**
 * Workspace multiple files change event type
 */
object WorkspaceFilesChangeEvent : EventType<WorkspaceFilesChangeData>

/**
 * Workspace directories change event type
 */
object WorkspaceDirectoriesChangeEvent: EventType<WorkspaceFilesChangeData>

/**
 * Workspace multiple files change data
 */
data class WorkspaceFilesChangeData(
    val changes: List<WorkspaceFileChangeData>
)


/**
 * Workspace root change data class
 * @param project The changed project
 * @param oldPath Original workspace root path
 * @param newPath New workspace root path
 */
data class WorkspaceRootChangeData(
    val project: IProject,
    val oldPath: String?,
    val newPath: String
)

/**
 * Workspace root change event
 */
object WorkspaceRootChangeEvent : EventType<WorkspaceRootChangeData>
