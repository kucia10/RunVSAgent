/*
 * SPDX-FileCopyrightText: 2025 Weibo, Inc.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.sina.weibo.agent.eclipse.ipc;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import com.sina.weibo.agent.eclipse.Activator;

/**
 * TCP Socket implementation using Java NIO.
 * Handles communication with the extension host process.
 */
public class NodeSocket implements ISocket {

    /** The underlying socket */
    private Socket socket;

    /** The socket channel (for NIO) */
    private SocketChannel channel;

    /** Debug label for logging */
    private final String debugLabel;

    /** Input stream */
    private InputStream inputStream;

    /** Output stream */
    private OutputStream outputStream;

    /** Data listeners */
    private final List<DataListener> dataListeners = new ArrayList<>();

    /** Close listeners */
    private final List<CloseListener> closeListeners = new ArrayList<>();

    /** End listeners */
    private final List<Runnable> endListeners = new ArrayList<>();

    /** Socket closed flag */
    private final AtomicBoolean closed = new AtomicBoolean(false);

    /** Input closed flag */
    private final AtomicBoolean inputClosed = new AtomicBoolean(false);

    /** Output closed flag */
    private final AtomicBoolean outputClosed = new AtomicBoolean(false);

    /** Disposed flag */
    private final AtomicBoolean disposed = new AtomicBoolean(false);

    /** Receiving flag */
    private final AtomicBoolean receiving = new AtomicBoolean(false);

    /** Executor for background operations */
    private final ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "NodeSocket-" + debugLabel);
        t.setDaemon(true);
        return t;
    });

    /** Buffer size for reading */
    private static final int BUFFER_SIZE = 64 * 1024; // 64KB

    /**
     * Create a NodeSocket from a regular Socket.
     *
     * @param socket the socket
     * @param debugLabel debug label for logging
     */
    public NodeSocket(Socket socket, String debugLabel) {
        this.socket = socket;
        this.channel = null;
        this.debugLabel = debugLabel != null ? debugLabel : "NodeSocket";

        try {
            this.inputStream = socket.getInputStream();
            this.outputStream = socket.getOutputStream();
        } catch (IOException e) {
            Activator.logError("Failed to get socket streams", e);
        }

        traceSocketEvent(SocketDiagnosticsEventType.CREATED, null);
    }

    /**
     * Create a NodeSocket from a SocketChannel.
     *
     * @param channel the socket channel
     * @param debugLabel debug label for logging
     */
    public NodeSocket(SocketChannel channel, String debugLabel) {
        this.socket = channel.socket();
        this.channel = channel;
        this.debugLabel = debugLabel != null ? debugLabel : "NodeSocket";

        try {
            this.inputStream = socket.getInputStream();
            this.outputStream = socket.getOutputStream();
        } catch (IOException e) {
            Activator.logError("Failed to get socket streams", e);
        }

        traceSocketEvent(SocketDiagnosticsEventType.CREATED, null);
    }

    @Override
    public void startReceiving() {
        if (receiving.getAndSet(true)) {
            return; // Already receiving
        }

        executor.submit(() -> {
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead;

            try {
                while (!closed.get() && !disposed.get()) {
                    if (channel != null) {
                        // Use NIO channel
                        ByteBuffer byteBuffer = ByteBuffer.allocate(BUFFER_SIZE);
                        bytesRead = channel.read(byteBuffer);

                        if (bytesRead == -1) {
                            onEndReceived();
                            break;
                        }

                        if (bytesRead > 0) {
                            byteBuffer.flip();
                            byte[] data = new byte[bytesRead];
                            byteBuffer.get(data);
                            notifyDataListeners(data);
                        }
                    } else {
                        // Use regular socket
                        bytesRead = inputStream.read(buffer);

                        if (bytesRead == -1) {
                            onEndReceived();
                            break;
                        }

                        if (bytesRead > 0) {
                            byte[] data = new byte[bytesRead];
                            System.arraycopy(buffer, 0, data, 0, bytesRead);
                            notifyDataListeners(data);
                        }
                    }
                }
            } catch (IOException e) {
                if (!closed.get() && !disposed.get()) {
                    handleSocketError(e);
                }
            }
        });

        traceSocketEvent(SocketDiagnosticsEventType.CONNECTED, null);
    }

    /**
     * Handle end of stream received.
     */
    private void onEndReceived() {
        inputClosed.set(true);
        traceSocketEvent(SocketDiagnosticsEventType.HALF_OPEN, "input closed");

        // Notify end listeners
        for (Runnable listener : endListeners) {
            try {
                listener.run();
            } catch (Exception e) {
                Activator.logError("Error in end listener", e);
            }
        }

        // If both ends are closed, close the socket
        if (outputClosed.get()) {
            closeSocket(false);
        }
    }

    /**
     * Handle socket error.
     *
     * @param error the error
     */
    private void handleSocketError(Exception error) {
        String errorCode;
        if (error instanceof SocketException) {
            errorCode = "SOCKET_ERROR";
        } else if (error instanceof IOException) {
            errorCode = "IO_ERROR";
        } else {
            errorCode = "UNKNOWN_ERROR";
        }

        Activator.logError("Socket error: " + errorCode + " - " + error.getMessage(), error);
        traceSocketEvent(SocketDiagnosticsEventType.ERROR, error.getMessage());
        closeSocket(true);
    }

    /**
     * Close the socket.
     *
     * @param hadError whether there was an error
     */
    private void closeSocket(boolean hadError) {
        if (closed.getAndSet(true)) {
            return; // Already closed
        }

        traceSocketEvent(SocketDiagnosticsEventType.CLOSED, hadError ? "with error" : "normal");

        // Close streams
        try {
            if (inputStream != null) {
                inputStream.close();
            }
        } catch (IOException e) {
            // Ignore
        }

        try {
            if (outputStream != null) {
                outputStream.close();
            }
        } catch (IOException e) {
            // Ignore
        }

        // Close socket/channel
        try {
            if (channel != null) {
                channel.close();
            } else if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            // Ignore
        }

        // Notify close listeners
        SocketCloseEvent event = hadError ?
                SocketCloseEvent.errorClose(SocketCloseEvent.Type.NODE_SOCKET_CLOSE, "ERROR", "Socket closed with error") :
                SocketCloseEvent.normalClose(SocketCloseEvent.Type.NODE_SOCKET_CLOSE);

        for (CloseListener listener : closeListeners) {
            try {
                listener.onClose(event);
            } catch (Exception e) {
                Activator.logError("Error in close listener", e);
            }
        }
    }

    /**
     * Notify all data listeners.
     *
     * @param data the data received
     */
    private void notifyDataListeners(byte[] data) {
        traceSocketEvent(SocketDiagnosticsEventType.DATA_RECEIVED, data.length);

        for (DataListener listener : dataListeners) {
            try {
                listener.onData(data);
            } catch (Exception e) {
                Activator.logError("Error in data listener", e);
            }
        }
    }

    @Override
    public Disposable onData(DataListener listener) {
        dataListeners.add(listener);
        return () -> dataListeners.remove(listener);
    }

    @Override
    public Disposable onClose(CloseListener listener) {
        closeListeners.add(listener);
        return () -> closeListeners.remove(listener);
    }

    @Override
    public Disposable onEnd(Runnable listener) {
        endListeners.add(listener);
        return () -> endListeners.remove(listener);
    }

    @Override
    public void write(byte[] buffer) {
        if (closed.get() || disposed.get() || outputClosed.get()) {
            Activator.logWarning("Cannot write to closed socket");
            return;
        }

        try {
            if (channel != null) {
                ByteBuffer byteBuffer = ByteBuffer.wrap(buffer);
                while (byteBuffer.hasRemaining()) {
                    channel.write(byteBuffer);
                }
            } else {
                outputStream.write(buffer);
                outputStream.flush();
            }
            traceSocketEvent(SocketDiagnosticsEventType.DATA_SENT, buffer.length);
        } catch (IOException e) {
            Activator.logError("Error writing to socket", e);
            handleSocketError(e);
        }
    }

    @Override
    public void end() {
        if (outputClosed.getAndSet(true)) {
            return; // Already ended
        }

        traceSocketEvent(SocketDiagnosticsEventType.HALF_OPEN, "output closed");

        try {
            if (socket != null && !socket.isOutputShutdown()) {
                socket.shutdownOutput();
            }
        } catch (IOException e) {
            Activator.logError("Error shutting down output", e);
        }

        // If both ends are closed, close the socket
        if (inputClosed.get()) {
            closeSocket(false);
        }
    }

    @Override
    public void drain() throws InterruptedException {
        // Wait for write buffer to be flushed
        // In this simple implementation, writes are synchronous
        // so there's nothing to drain
    }

    @Override
    public void traceSocketEvent(SocketDiagnosticsEventType type, Object data) {
        Activator.logDebug("[" + debugLabel + "] Socket event: " + type + 
                (data != null ? " - " + data : ""));
    }

    @Override
    public void dispose() {
        if (disposed.getAndSet(true)) {
            return; // Already disposed
        }

        closeSocket(false);

        // Shutdown executor
        executor.shutdown();

        // Clear listeners
        dataListeners.clear();
        closeListeners.clear();
        endListeners.clear();

        traceSocketEvent(SocketDiagnosticsEventType.DISCONNECTED, null);
    }

    @Override
    public boolean isClosed() {
        return closed.get();
    }

    @Override
    public boolean isInputClosed() {
        return inputClosed.get();
    }

    @Override
    public boolean isOutputClosed() {
        return outputClosed.get();
    }

    /**
     * Get the underlying socket.
     *
     * @return the socket
     */
    public Socket getSocket() {
        return socket;
    }

    /**
     * Get the socket channel.
     *
     * @return the socket channel or null
     */
    public SocketChannel getChannel() {
        return channel;
    }
}
