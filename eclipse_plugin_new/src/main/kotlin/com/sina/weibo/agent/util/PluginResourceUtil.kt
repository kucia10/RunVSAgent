// SPDX-FileCopyrightText: 2025 Weibo, Inc.
//
// SPDX-License-Identifier: Apache-2.0

package com.sina.weibo.agent.util

import org.eclipse.core.runtime.FileLocator
import org.eclipse.core.runtime.Platform
import org.osgi.framework.FrameworkUtil
import java.io.File
import java.io.FileOutputStream
import java.net.URL

/**
 * Plugin resource utility class
 * Used to obtain resource file paths in the plugin
 */
object PluginResourceUtil {
    private val LOG = Platform.getLog(FrameworkUtil.getBundle(PluginResourceUtil::class.java))

    /**
     * Get resource path
     *
     * @param pluginId Plugin ID
     * @param resourceName Resource name
     * @return Resource path, or null if failed to get
     */
    fun getResourcePath(pluginId: String, resourceName: String): String? {
        return try {
            val bundle = Platform.getBundle(pluginId)
            val resourceUrl = bundle.getResource(resourceName)
            if (resourceUrl != null) {
                FileLocator.toFileURL(resourceUrl).path
            } else {
                null
            }
        } catch (e: Exception) {
            LOG.error("Failed to get plugin resource path: $resourceName", e)
            null
        }
    }

    /**
     * Extract resource from URL to temporary file
     *
     * @param resourceUrl Resource URL
     * @param filename File name
     * @return Temporary file path, or null if extraction fails
     */
    fun extractResourceToTempFile(resourceUrl: URL, filename: String): String? {
        return try {
            val tempFile = File.createTempFile("weibo-agent-", "-$filename")
            tempFile.deleteOnExit()

            resourceUrl.openStream().use { input ->
                FileOutputStream(tempFile).use { output ->
                    input.copyTo(output)
                }
            }

            LOG.info("Resource extracted to temporary file: ${tempFile.absolutePath}")
            tempFile.absolutePath
        } catch (e: Exception) {
            LOG.error("Failed to extract resource to temporary file: $filename", e)
            null
        }
    }
}
