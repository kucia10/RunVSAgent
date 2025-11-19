// SPDX-FileCopyrightText: 2025 Weibo, Inc.
//
// SPDX-License-Identifier: Apache-2.0

package com.sina.weibo.agent.util

import org.eclipse.core.net.proxy.IProxyData
import org.eclipse.core.net.proxy.IProxyService
import org.eclipse.core.runtime.Platform
import org.osgi.framework.FrameworkUtil
import java.net.URI
import java.net.URISyntaxException

/**
 * Proxy configuration utility class
 * Responsible for getting proxy configuration with priority: IDE settings > environment variables
 */
object ProxyConfigUtil {
    private val logger = Platform.getLog(FrameworkUtil.getBundle(ProxyConfigUtil::class.java))

    /**
     * Proxy configuration data class
     */
    data class ProxyConfig(
        val proxyUrl: String?,
        val proxyExceptions: String?,
        val pacUrl: String?,
        val source: String
    ) {
        val hasProxy: Boolean
            get() = !proxyUrl.isNullOrEmpty() || !pacUrl.isNullOrEmpty()
    }

    /**
     * Get proxy configuration
     * Priority: IDE settings > environment variables
     */
    fun getProxyConfig(): ProxyConfig {
        // First check IDE proxy settings
        val ideProxyConfig = getIDEProxyConfig()
        if (ideProxyConfig.hasProxy) {
            logger.info("Using IDE proxy configuration: ${ideProxyConfig.proxyUrl ?: ideProxyConfig.pacUrl}")
            return ideProxyConfig
        }

        // Then check environment variable proxy settings
        val envProxyConfig = getEnvironmentProxyConfig()
        if (envProxyConfig.hasProxy) {
            logger.info("Using environment variable proxy configuration: ${envProxyConfig.proxyUrl}")
            return envProxyConfig
        }

        // No proxy configuration
        logger.info("No proxy configuration found")
        return ProxyConfig(null, null, null, "none")
    }

    /**
     * Get IDE proxy configuration
     */
    private fun getIDEProxyConfig(): ProxyConfig {
        try {
            val proxyService = FrameworkUtil.getBundle(ProxyConfigUtil::class.java).bundleContext
                .getService(FrameworkUtil.getBundle(ProxyConfigUtil::class.java).bundleContext.getServiceReference(IProxyService::class.java))

            if (proxyService != null && proxyService.isProxiesEnabled) {
                val proxyData = proxyService.getProxyDataForHost("www.google.com", IProxyData.HTTPS_PROXY_TYPE)
                if (proxyData != null) {
                    val proxyUrl = "http://${proxyData.host}:${proxyData.port}"
                    val proxyExceptions = proxyService.nonProxiedHosts.joinToString(",")
                    return ProxyConfig(proxyUrl, proxyExceptions, null, "ide-http")
                }
            }
        } catch (e: Exception) {
            logger.warn("Failed to get IDE proxy configuration", e)
            return ProxyConfig(null, null, null, "ide-error")
        }
        return ProxyConfig(null, null, null, "ide-none")
    }

    /**
     * Get environment variable proxy configuration
     */
    private fun getEnvironmentProxyConfig(): ProxyConfig {
        return try {
            val httpProxy = System.getenv("HTTP_PROXY") ?: System.getenv("http_proxy")
            val httpsProxy = System.getenv("HTTPS_PROXY") ?: System.getenv("https_proxy")
            val noProxy = System.getenv("NO_PROXY") ?: System.getenv("no_proxy")

            // Prefer HTTPS_PROXY, then HTTP_PROXY
            val proxyUrl = when {
                !httpsProxy.isNullOrEmpty() -> normalizeProxyUrl(httpsProxy)
                !httpProxy.isNullOrEmpty() -> normalizeProxyUrl(httpProxy)
                else -> null
            }

            ProxyConfig(proxyUrl, noProxy, null, "env")
        } catch (e: Exception) {
            logger.warn("Failed to get environment proxy configuration", e)
            ProxyConfig(null, null, null, "env-error")
        }
    }

    /**
     * Normalize proxy URL
     */
    private fun normalizeProxyUrl(url: String): String {
        return try {
            val uri = URI(url)
            when {
                uri.scheme.isNullOrEmpty() -> "http://$url"
                uri.scheme == "http" || uri.scheme == "https" -> url
                else -> "http://$url"
            }
        } catch (e: URISyntaxException) {
            // If URL parsing fails, assume HTTP protocol
            "http://$url"
        }
    }

    /**
     * Get HTTP proxy configuration for initializeConfiguration
     * If using PAC, set http.proxy to pacUrl
     */
    fun getHttpProxyConfigForInitialization(): Map<String, Any>? {
        val proxyConfig = getProxyConfig()
        if (!proxyConfig.hasProxy) {
            return null
        }

        val configMap = mutableMapOf<String, Any>()

        if (!proxyConfig.pacUrl.isNullOrEmpty()) {
            // For PAC proxy, set http.proxy to pacUrl
            configMap["proxy"] = proxyConfig.pacUrl
            configMap["proxySupport"] = "on"
        } else if (!proxyConfig.proxyUrl.isNullOrEmpty()) {
            // For HTTP proxy
            configMap["proxy"] = proxyConfig.proxyUrl
            configMap["proxySupport"] = "on"
        }

        // Add noProxy configuration if proxyExceptions is not null or empty
        if (!proxyConfig.proxyExceptions.isNullOrEmpty()) {
            // Split proxyExceptions string by comma and trim each entry
            val noProxyList = proxyConfig.proxyExceptions
                .split(",")
                .map { it.trim() }
                .filter { it.isNotEmpty() }

            if (noProxyList.isNotEmpty()) {
                configMap["noProxy"] = noProxyList
            }
        }

        return if (configMap.isNotEmpty()) configMap else null
    }

    /**
     * Get proxy configuration for process startup
     * Only set environment variables, no command line arguments
     */
    fun getProxyEnvVarsForProcessStart(): Map<String, String> {
        val proxyConfig = getProxyConfig()
        val envVars = mutableMapOf<String, String>()

        if (!proxyConfig.hasProxy) {
            return emptyMap()
        }

        if (!proxyConfig.pacUrl.isNullOrEmpty()) {
            // For PAC proxy, set PROXY_PAC_URL environment variable
            envVars["PROXY_PAC_URL"] = proxyConfig.pacUrl
        } else if (!proxyConfig.proxyUrl.isNullOrEmpty()) {
            // For HTTP proxy, set HTTP_PROXY and HTTPS_PROXY environment variables
            envVars["HTTP_PROXY"] = proxyConfig.proxyUrl
            envVars["HTTPS_PROXY"] = proxyConfig.proxyUrl
        }

        // Add NO_PROXY environment variable if proxyExceptions is not null or empty
        if (!proxyConfig.proxyExceptions.isNullOrEmpty()) {
            envVars["NO_PROXY"] = proxyConfig.proxyExceptions
        }

        return envVars
    }
}
