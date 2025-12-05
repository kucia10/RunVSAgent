/*
 * SPDX-FileCopyrightText: 2025 Weibo, Inc.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.sina.weibo.agent.eclipse.ipc.proxy;

import java.util.HashMap;
import java.util.Map;

/**
 * Identifies a service proxy for RPC communication.
 * Used to register and look up services on both main thread and extension host.
 *
 * @param <T> the service interface type
 */
public class ProxyIdentifier<T> {

    /** Identifier string */
    private final String identifier;

    /** Whether this is a main thread proxy (vs extension host) */
    private final boolean isMain;

    /** Numeric ID for the proxy */
    private final int nid;

    /** Registry of all created identifiers */
    private static final Map<String, ProxyIdentifier<?>> registry = new HashMap<>();

    /** Counter for numeric IDs */
    private static int nidCounter = 0;

    /**
     * Private constructor.
     *
     * @param identifier the identifier string
     * @param isMain whether this is a main thread proxy
     * @param nid the numeric ID
     */
    private ProxyIdentifier(String identifier, boolean isMain, int nid) {
        this.identifier = identifier;
        this.isMain = isMain;
        this.nid = nid;
    }

    /**
     * Create a main thread proxy identifier.
     *
     * @param <T> the service type
     * @param identifier the identifier string
     * @return the proxy identifier
     */
    @SuppressWarnings("unchecked")
    public static <T> ProxyIdentifier<T> createMainIdentifier(String identifier) {
        synchronized (registry) {
            if (registry.containsKey(identifier)) {
                return (ProxyIdentifier<T>) registry.get(identifier);
            }
            ProxyIdentifier<T> proxy = new ProxyIdentifier<>(identifier, true, ++nidCounter);
            registry.put(identifier, proxy);
            return proxy;
        }
    }

    /**
     * Create an extension host proxy identifier.
     *
     * @param <T> the service type
     * @param identifier the identifier string
     * @return the proxy identifier
     */
    @SuppressWarnings("unchecked")
    public static <T> ProxyIdentifier<T> createExtIdentifier(String identifier) {
        synchronized (registry) {
            if (registry.containsKey(identifier)) {
                return (ProxyIdentifier<T>) registry.get(identifier);
            }
            ProxyIdentifier<T> proxy = new ProxyIdentifier<>(identifier, false, ++nidCounter);
            registry.put(identifier, proxy);
            return proxy;
        }
    }

    /**
     * Get a proxy identifier by name.
     *
     * @param <T> the service type
     * @param identifier the identifier string
     * @return the proxy identifier or null if not found
     */
    @SuppressWarnings("unchecked")
    public static <T> ProxyIdentifier<T> get(String identifier) {
        synchronized (registry) {
            return (ProxyIdentifier<T>) registry.get(identifier);
        }
    }

    /**
     * Get the identifier string.
     *
     * @return the identifier
     */
    public String getIdentifier() {
        return identifier;
    }

    /**
     * Check if this is a main thread proxy.
     *
     * @return true if main thread proxy
     */
    public boolean isMain() {
        return isMain;
    }

    /**
     * Get the numeric ID.
     *
     * @return the numeric ID
     */
    public int getNid() {
        return nid;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProxyIdentifier<?> that = (ProxyIdentifier<?>) o;
        return nid == that.nid && identifier.equals(that.identifier);
    }

    @Override
    public int hashCode() {
        return 31 * identifier.hashCode() + nid;
    }

    @Override
    public String toString() {
        return "ProxyIdentifier{" +
                "identifier='" + identifier + '\'' +
                ", isMain=" + isMain +
                ", nid=" + nid +
                '}';
    }

    /**
     * Clear all registered identifiers.
     * Used primarily for testing.
     */
    public static void clearRegistry() {
        synchronized (registry) {
            registry.clear();
            nidCounter = 0;
        }
    }
}
