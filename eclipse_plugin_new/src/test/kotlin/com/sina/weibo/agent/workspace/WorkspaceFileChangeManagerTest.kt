// SPDX-FileCopyrightText: 2025 Weibo, Inc.
//
// SPDX-License-Identifier: Apache-2.0

package com.sina.weibo.agent.workspace

import org.eclipse.core.resources.IResourceChangeEvent
import org.eclipse.core.resources.IResourceDelta
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito

class WorkspaceFileChangeManagerTest {

    @Test
    fun testResourceChanged() {
        val manager = WorkspaceFileChangeManager()
        val event = Mockito.mock(IResourceChangeEvent::class.java)
        val delta = Mockito.mock(IResourceDelta::class.java)

        Mockito.`when`(event.delta).thenReturn(delta)

        manager.resourceChanged(event)

        Mockito.verify(delta).accept(Mockito.any())
    }
}
