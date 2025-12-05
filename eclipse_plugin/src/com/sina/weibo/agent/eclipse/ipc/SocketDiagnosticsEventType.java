/*
 * SPDX-FileCopyrightText: 2025 Weibo, Inc.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.sina.weibo.agent.eclipse.ipc;

/**
 * Enum for socket diagnostics event types.
 */
public enum SocketDiagnosticsEventType {
    /** Socket created */
    CREATED,
    /** Socket connected */
    CONNECTED,
    /** Socket disconnected */
    DISCONNECTED,
    /** Data received */
    DATA_RECEIVED,
    /** Data sent */
    DATA_SENT,
    /** Error occurred */
    ERROR,
    /** Socket closed */
    CLOSED,
    /** Read operation */
    READ,
    /** Write operation */
    WRITE,
    /** Half open (one direction closed) */
    HALF_OPEN,
    /** Begin reconnect */
    BEGIN_RECONNECT,
    /** End reconnect */
    END_RECONNECT,
    /** Socket timeout */
    TIMEOUT,
    /** Keep alive sent */
    KEEP_ALIVE_SENT,
    /** Keep alive received */
    KEEP_ALIVE_RECEIVED,
    /** Acknowledge sent */
    ACK_SENT,
    /** Acknowledge received */
    ACK_RECEIVED,
    /** Pause */
    PAUSE,
    /** Resume */
    RESUME
}
