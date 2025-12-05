/*
 * SPDX-FileCopyrightText: 2025 Weibo, Inc.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.sina.weibo.agent.eclipse.ipc;

/**
 * Enum representing protocol message types.
 */
public enum ProtocolMessageType {
    /** No message */
    NONE(0),
    /** Regular data message */
    REGULAR(1),
    /** Control message */
    CONTROL(2),
    /** Acknowledgment message */
    ACK(3),
    /** Reconnect message */
    RECONNECT(4),
    /** Replay request message */
    REPLAY_REQUEST(5),
    /** Pause message */
    PAUSE(6),
    /** Resume message */
    RESUME(7),
    /** Keep-alive message */
    KEEP_ALIVE(8),
    /** Disconnect message */
    DISCONNECT(9);

    private final int value;

    ProtocolMessageType(int value) {
        this.value = value;
    }

    /**
     * Get the numeric value of this message type.
     *
     * @return the numeric value
     */
    public int getValue() {
        return value;
    }

    /**
     * Convert to string representation.
     *
     * @return string representation
     */
    public String toTypeString() {
        switch (this) {
            case NONE:
                return "None";
            case REGULAR:
                return "Regular";
            case CONTROL:
                return "Control";
            case ACK:
                return "Ack";
            case RECONNECT:
                return "Reconnect";
            case REPLAY_REQUEST:
                return "ReplayRequest";
            case PAUSE:
                return "Pause";
            case RESUME:
                return "Resume";
            case KEEP_ALIVE:
                return "KeepAlive";
            case DISCONNECT:
                return "Disconnect";
            default:
                return "Unknown";
        }
    }

    /**
     * Get message type from numeric value.
     *
     * @param value the numeric value
     * @return the message type or NONE if not found
     */
    public static ProtocolMessageType fromValue(int value) {
        for (ProtocolMessageType type : values()) {
            if (type.value == value) {
                return type;
            }
        }
        return NONE;
    }
}
