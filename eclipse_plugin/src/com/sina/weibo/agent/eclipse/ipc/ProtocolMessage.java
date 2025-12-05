/*
 * SPDX-FileCopyrightText: 2025 Weibo, Inc.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.sina.weibo.agent.eclipse.ipc;

import java.util.Arrays;

/**
 * Represents a protocol message for IPC communication.
 */
public class ProtocolMessage {

    /** Message type */
    private final ProtocolMessageType type;

    /** Message ID */
    private final int id;

    /** Acknowledgment count */
    private final int ack;

    /** Message data */
    private final byte[] data;

    /**
     * Create a protocol message.
     *
     * @param type message type
     * @param id message ID
     * @param ack acknowledgment count
     * @param data message data
     */
    public ProtocolMessage(ProtocolMessageType type, int id, int ack, byte[] data) {
        this.type = type;
        this.id = id;
        this.ack = ack;
        this.data = data;
    }

    /**
     * Get the message type.
     *
     * @return the message type
     */
    public ProtocolMessageType getType() {
        return type;
    }

    /**
     * Get the message ID.
     *
     * @return the message ID
     */
    public int getId() {
        return id;
    }

    /**
     * Get the acknowledgment count.
     *
     * @return the acknowledgment count
     */
    public int getAck() {
        return ack;
    }

    /**
     * Get the message data.
     *
     * @return the message data
     */
    public byte[] getData() {
        return data;
    }

    /**
     * Get the data length.
     *
     * @return the data length
     */
    public int getDataLength() {
        return data != null ? data.length : 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProtocolMessage that = (ProtocolMessage) o;
        return id == that.id &&
                ack == that.ack &&
                type == that.type &&
                Arrays.equals(data, that.data);
    }

    @Override
    public int hashCode() {
        int result = type.hashCode();
        result = 31 * result + id;
        result = 31 * result + ack;
        result = 31 * result + Arrays.hashCode(data);
        return result;
    }

    @Override
    public String toString() {
        return "ProtocolMessage{" +
                "type=" + type +
                ", id=" + id +
                ", ack=" + ack +
                ", dataLength=" + getDataLength() +
                '}';
    }

    /**
     * Create a regular message.
     *
     * @param id message ID
     * @param data message data
     * @return the protocol message
     */
    public static ProtocolMessage regular(int id, byte[] data) {
        return new ProtocolMessage(ProtocolMessageType.REGULAR, id, 0, data);
    }

    /**
     * Create a control message.
     *
     * @param data message data
     * @return the protocol message
     */
    public static ProtocolMessage control(byte[] data) {
        return new ProtocolMessage(ProtocolMessageType.CONTROL, 0, 0, data);
    }

    /**
     * Create an acknowledge message.
     *
     * @param id message ID
     * @return the protocol message
     */
    public static ProtocolMessage ack(int id) {
        return new ProtocolMessage(ProtocolMessageType.ACK, id, 0, null);
    }

    /**
     * Create a disconnect message.
     *
     * @return the protocol message
     */
    public static ProtocolMessage disconnect() {
        return new ProtocolMessage(ProtocolMessageType.DISCONNECT, 0, 0, null);
    }

    /**
     * Create a reconnect message.
     *
     * @return the protocol message
     */
    public static ProtocolMessage reconnect() {
        return new ProtocolMessage(ProtocolMessageType.RECONNECT, 0, 0, null);
    }

    /**
     * Create a pause message.
     *
     * @return the protocol message
     */
    public static ProtocolMessage pause() {
        return new ProtocolMessage(ProtocolMessageType.PAUSE, 0, 0, null);
    }

    /**
     * Create a resume message.
     *
     * @return the protocol message
     */
    public static ProtocolMessage resume() {
        return new ProtocolMessage(ProtocolMessageType.RESUME, 0, 0, null);
    }

    /**
     * Create a keep-alive message.
     *
     * @return the protocol message
     */
    public static ProtocolMessage keepAlive() {
        return new ProtocolMessage(ProtocolMessageType.KEEP_ALIVE, 0, 0, null);
    }
}
