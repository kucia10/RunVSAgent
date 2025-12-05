/*
 * SPDX-FileCopyrightText: 2025 Weibo, Inc.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.sina.weibo.agent.eclipse.ipc.proxy;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Interface for RPC protocol handling.
 * Provides methods for remote procedure calls between Eclipse and extension host.
 */
public interface IRPCProtocol {

    /**
     * Disposable interface for cleanup.
     */
    interface Disposable {
        void dispose();
    }

    /**
     * Get a proxy for remote service invocation.
     *
     * @param <T> the service type
     * @param identifier the proxy identifier
     * @return the proxy instance
     */
    <T> T getProxy(ProxyIdentifier<T> identifier);

    /**
     * Set a local service implementation.
     *
     * @param <T> the service interface type
     * @param <R> the implementation type
     * @param identifier the proxy identifier
     * @param instance the service instance
     * @return the instance
     */
    <T, R extends T> R set(ProxyIdentifier<T> identifier, R instance);

    /**
     * Assert that all identifiers are registered.
     *
     * @param identifiers the identifiers to check
     */
    void assertRegistered(List<ProxyIdentifier<?>> identifiers);

    /**
     * Drain all pending messages.
     *
     * @return a future that completes when drained
     */
    CompletableFuture<Void> drain();

    /**
     * Dispose the RPC protocol and release all resources.
     */
    void dispose();

    /**
     * Check if the protocol is disposed.
     *
     * @return true if disposed
     */
    boolean isDisposed();

    /**
     * Send a raw message.
     *
     * @param buffer the message buffer
     */
    void send(byte[] buffer);

    /**
     * Register a message listener.
     *
     * @param listener the listener to register
     * @return a disposable to unregister
     */
    Disposable onMessage(MessageListener listener);

    /**
     * Message listener interface.
     */
    interface MessageListener {
        void onMessage(byte[] data);
    }
}
