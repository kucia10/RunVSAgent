/*
 * SPDX-FileCopyrightText: 2025 Weibo, Inc.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.sina.weibo.agent.eclipse.ipc;

import java.util.function.Consumer;

/**
 * Interface for socket operations.
 * Provides abstraction over different socket implementations.
 */
public interface ISocket {

    /**
     * Interface for disposable resources.
     */
    interface Disposable {
        void dispose();
    }

    /**
     * Data listener interface.
     */
    interface DataListener {
        void onData(byte[] data);
    }

    /**
     * Close listener interface.
     */
    interface CloseListener {
        void onClose(SocketCloseEvent event);
    }

    /**
     * Start receiving data from the socket.
     */
    void startReceiving();

    /**
     * Register a data listener.
     *
     * @param listener the data listener
     * @return a disposable to unregister the listener
     */
    Disposable onData(DataListener listener);

    /**
     * Register a close listener.
     *
     * @param listener the close listener
     * @return a disposable to unregister the listener
     */
    Disposable onClose(CloseListener listener);

    /**
     * Register an end listener.
     *
     * @param listener the end listener
     * @return a disposable to unregister the listener
     */
    Disposable onEnd(Runnable listener);

    /**
     * Write data to the socket.
     *
     * @param buffer the data to write
     */
    void write(byte[] buffer);

    /**
     * End the socket connection gracefully.
     */
    void end();

    /**
     * Drain the write buffer.
     *
     * @throws InterruptedException if interrupted while draining
     */
    void drain() throws InterruptedException;

    /**
     * Trace socket event for diagnostics.
     *
     * @param type the event type
     * @param data additional data
     */
    void traceSocketEvent(SocketDiagnosticsEventType type, Object data);

    /**
     * Dispose the socket and release all resources.
     */
    void dispose();

    /**
     * Check if the socket is closed.
     *
     * @return true if closed
     */
    boolean isClosed();

    /**
     * Check if the input stream is closed.
     *
     * @return true if input is closed
     */
    boolean isInputClosed();

    /**
     * Check if the output stream is closed.
     *
     * @return true if output is closed
     */
    boolean isOutputClosed();
}
