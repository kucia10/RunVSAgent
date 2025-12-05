/*
 * SPDX-FileCopyrightText: 2025 Weibo, Inc.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.sina.weibo.agent.eclipse.ipc;

/**
 * Event class representing socket close events.
 */
public class SocketCloseEvent {

    /**
     * Type of socket close event.
     */
    public enum Type {
        NODE_SOCKET_CLOSE,
        WEB_SOCKET_CLOSE
    }

    private final Type type;
    private final boolean hadError;
    private final String errorCode;
    private final String errorMessage;

    /**
     * Create a socket close event.
     *
     * @param type the type of close event
     * @param hadError whether there was an error
     * @param errorCode the error code if any
     * @param errorMessage the error message if any
     */
    public SocketCloseEvent(Type type, boolean hadError, String errorCode, String errorMessage) {
        this.type = type;
        this.hadError = hadError;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    /**
     * Create a normal close event (no error).
     *
     * @param type the type of close event
     * @return the close event
     */
    public static SocketCloseEvent normalClose(Type type) {
        return new SocketCloseEvent(type, false, null, null);
    }

    /**
     * Create an error close event.
     *
     * @param type the type of close event
     * @param errorCode the error code
     * @param errorMessage the error message
     * @return the close event
     */
    public static SocketCloseEvent errorClose(Type type, String errorCode, String errorMessage) {
        return new SocketCloseEvent(type, true, errorCode, errorMessage);
    }

    /**
     * Get the event type.
     *
     * @return the event type
     */
    public Type getType() {
        return type;
    }

    /**
     * Check if there was an error.
     *
     * @return true if there was an error
     */
    public boolean hadError() {
        return hadError;
    }

    /**
     * Get the error code.
     *
     * @return the error code or null
     */
    public String getErrorCode() {
        return errorCode;
    }

    /**
     * Get the error message.
     *
     * @return the error message or null
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    @Override
    public String toString() {
        return "SocketCloseEvent{" +
                "type=" + type +
                ", hadError=" + hadError +
                ", errorCode='" + errorCode + '\'' +
                ", errorMessage='" + errorMessage + '\'' +
                '}';
    }
}
