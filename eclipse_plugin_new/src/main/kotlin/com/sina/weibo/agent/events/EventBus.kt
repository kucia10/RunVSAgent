// SPDX-FileCopyrightText: 2025 Weibo, Inc.
//
// SPDX-License-Identifier: Apache-2.0

package com.sina.weibo.agent.events

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import org.eclipse.core.runtime.ILog
import org.eclipse.core.runtime.Platform
import org.eclipse.swt.widgets.Display
import java.util.concurrent.ConcurrentHashMap


open class EventBus {
    private val logger: ILog = Platform.getLog(Platform.getBundle("com.sina.weibo.agent"))

    // All events are dispatched through this flow
    private val _events = MutableSharedFlow<Event<*>>(extraBufferCapacity = 64)
    val events: SharedFlow<Event<*>> = _events.asSharedFlow()

    // Event listener mapping, key is event type, value is listener list
    private val listeners = ConcurrentHashMap<EventType<*>, MutableList<(Any) -> Unit>>()

    /**
     * Send event
     */
    suspend fun <T : Any> emit(eventType: EventType<T>, data: T) {
        _events.emit(Event(eventType, data))

        // Also notify regular listeners
        @Suppress("UNCHECKED_CAST")
        listeners[eventType]?.forEach { listener ->
            try {
                listener(data)
            } catch (e: Exception) {
                logger.error("Event handling exception", e)
            }
        }
    }

    /**
     * Send event in specified coroutine scope
     */
    fun <T : Any> emitIn(scope: CoroutineScope, eventType: EventType<T>, data: T) {
        scope.launch {
            emit(eventType, data)
        }
    }

    /**
     * Send event in UI thread
     */
    fun <T : Any> emitInUI(eventType: EventType<T>, data: T) {
        Display.getDefault().asyncExec {
            listeners[eventType]?.forEach { listener ->
                @Suppress("UNCHECKED_CAST")
                try {
                    listener(data)
                } catch (e: Exception) {
                    logger.error("Event processing exception", e)
                }
            }
        }
    }

    /**
     * Subscribe to specific event type in specified coroutine scope
     */
    inline fun <reified T : Any> on(
        scope: CoroutineScope,
        eventType: EventType<T>,
        crossinline handler: suspend (T) -> Unit
    ) {
        scope.launch {
            events
                .filter { it.type == eventType }
                .collect { event ->
                    @Suppress("UNCHECKED_CAST")
                    handler(event.data as T)
                }
        }
    }

    /**
     * Add event listener (no coroutines required)
     */
    @Suppress("UNCHECKED_CAST")
    fun <T : Any> addListener(eventType: EventType<T>, handler: (T) -> Unit) {
        listeners.getOrPut(eventType) { mutableListOf() }.add(handler as (Any) -> Unit)
    }

    /**
     * Remove event listener
     */
    fun <T : Any> removeListener(eventType: EventType<T>, handler: (Any) -> Unit) {
        listeners[eventType]?.remove(handler)
    }

    /**
     * Remove all listeners for specific event type
     */
    fun <T : Any> removeAllListeners(eventType: EventType<T>) {
        listeners.remove(eventType)
    }
}

/**
 * Event type marker interface
 */
interface EventType<T : Any>

/**
 * Event data class
 */
data class Event<T : Any>(
    val type: EventType<T>,
    val data: T
)
