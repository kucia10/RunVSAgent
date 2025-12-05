/*
 * SPDX-FileCopyrightText: 2025 Weibo, Inc.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.sina.weibo.agent.eclipse.core;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.core.resources.IProject;

import com.sina.weibo.agent.eclipse.Activator;
import com.sina.weibo.agent.eclipse.ipc.proxy.IRPCProtocol;
import com.sina.weibo.agent.eclipse.ipc.proxy.ProxyIdentifier;

/**
 * Manages RPC protocol initialization and service registration.
 * Coordinates between Eclipse services and extension host.
 */
public class RPCManager {

    /** RPC Protocol */
    private final IRPCProtocol rpcProtocol;

    /** Project */
    private final IProject project;

    /** Initialized flag */
    private final AtomicBoolean initialized = new AtomicBoolean(false);

    /** Disposed flag */
    private final AtomicBoolean disposed = new AtomicBoolean(false);

    /** Registered services */
    private final Map<String, Object> registeredServices = new HashMap<>();

    /**
     * Create a new RPCManager.
     *
     * @param rpcProtocol the RPC protocol
     * @param project the Eclipse project
     */
    public RPCManager(IRPCProtocol rpcProtocol, IProject project) {
        this.rpcProtocol = rpcProtocol;
        this.project = project;
    }

    /**
     * Start initialization of RPC services.
     */
    public void startInitialize() {
        if (initialized.get()) {
            Activator.logWarning("RPCManager already initialized");
            return;
        }

        Activator.logInfo("Initializing RPCManager...");

        try {
            // Setup default protocols
            setupDefaultProtocols();

            // Setup extension-required protocols
            setupExtensionRequiredProtocols();

            // Setup WebView protocols
            setupWebviewProtocols();

            initialized.set(true);
            Activator.logInfo("RPCManager initialized successfully");

        } catch (Exception e) {
            Activator.logError("Failed to initialize RPCManager", e);
        }
    }

    /**
     * Setup default protocols.
     */
    private void setupDefaultProtocols() {
        Activator.logDebug("Setting up default protocols...");

        // Configuration service
        // Note: Actual implementation would register real service instances
        // rpcProtocol.set(MainContext.MainThreadConfiguration, new MainThreadConfiguration());
    }

    /**
     * Setup extension-required protocols.
     */
    private void setupExtensionRequiredProtocols() {
        Activator.logDebug("Setting up extension-required protocols...");

        // These are the core services required by VSCode extensions
        // They would be implemented as MainThread actors
    }

    /**
     * Setup WebView protocols.
     */
    private void setupWebviewProtocols() {
        Activator.logDebug("Setting up webview protocols...");

        // WebView-related services
        // rpcProtocol.set(MainContext.MainThreadWebviews, new MainThreadWebviews(project));
        // rpcProtocol.set(MainContext.MainThreadWebviewViews, new MainThreadWebviewViews(project));
    }

    /**
     * Register a service with the RPC protocol.
     *
     * @param <T> the service type
     * @param identifier the proxy identifier
     * @param service the service instance
     */
    public <T> void registerService(ProxyIdentifier<T> identifier, T service) {
        if (rpcProtocol == null) {
            Activator.logError("Cannot register service - RPC protocol not available");
            return;
        }

        rpcProtocol.set(identifier, service);
        registeredServices.put(identifier.getIdentifier(), service);
        Activator.logDebug("Registered service: " + identifier.getIdentifier());
    }

    /**
     * Get a proxy for a remote service.
     *
     * @param <T> the service type
     * @param identifier the proxy identifier
     * @return the proxy instance
     */
    public <T> T getProxy(ProxyIdentifier<T> identifier) {
        if (rpcProtocol == null) {
            return null;
        }
        return rpcProtocol.getProxy(identifier);
    }

    /**
     * Get the RPC protocol.
     *
     * @return the RPC protocol
     */
    public IRPCProtocol getRPCProtocol() {
        return rpcProtocol;
    }

    /**
     * Get the project.
     *
     * @return the project
     */
    public IProject getProject() {
        return project;
    }

    /**
     * Check if initialized.
     *
     * @return true if initialized
     */
    public boolean isInitialized() {
        return initialized.get();
    }

    /**
     * Dispose and release resources.
     */
    public void dispose() {
        if (disposed.getAndSet(true)) {
            return;
        }

        Activator.logInfo("Disposing RPCManager...");

        // Dispose registered services that are disposable
        for (Object service : registeredServices.values()) {
            if (service instanceof AutoCloseable) {
                try {
                    ((AutoCloseable) service).close();
                } catch (Exception e) {
                    Activator.logError("Error disposing service", e);
                }
            }
        }
        registeredServices.clear();

        initialized.set(false);
        Activator.logInfo("RPCManager disposed");
    }
}
