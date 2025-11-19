// SPDX-FileCopyrightText: 2025 Weibo, Inc.
//
// SPDX-License-Identifier: Apache-2.0

package com.sina.weibo.agent.util

import org.eclipse.core.runtime.Platform
import org.osgi.framework.FrameworkUtil
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

/**
 * Node version utility class
 * Get local Node.js version and perform version checks
 */
object NodeVersionUtil {
    private val LOG = Platform.getLog(FrameworkUtil.getBundle(NodeVersionUtil::class.java))

    /**
     * Get local Node.js version
     *
     * @return Node.js version string, or null if not found
     */
    fun getLocalNodeVersion(): String? {
        val nodeExecutable = findNodeExecutable()
        if (nodeExecutable == null) {
            LOG.warn("Node.js executable not found")
            return null
        }

        return try {
            val process = ProcessBuilder(nodeExecutable.absolutePath, "--version").start()
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val version = reader.readLine()
            process.waitFor()
            version
        } catch (e: Exception) {
            LOG.error("Failed to get Node.js version", e)
            null
        }
    }

    /**
     * Find Node.js executable
     * Search in common installation locations and PATH environment variable
     */
    private fun findNodeExecutable(): File? {
        // Search in PATH environment variable
        val pathDirs = System.getenv("PATH").split(File.pathSeparator)
        for (dir in pathDirs) {
            val nodeFile = File(dir, if (System.getProperty("os.name").startsWith("Windows")) "node.exe" else "node")
            if (nodeFile.exists() && nodeFile.canExecute()) {
                return nodeFile
            }
        }

        // Search in common installation locations
        val commonPaths = arrayOf(
            "/usr/local/bin/node",
            "/usr/bin/node",
            "C:\\Program Files\\nodejs\\node.exe"
        )
        for (path in commonPaths) {
            val nodeFile = File(path)
            if (nodeFile.exists() && nodeFile.canExecute()) {
                return nodeFile
            }
        }

        return null
    }

    /**
     * Check if Node.js version meets minimum requirement
     *
     * @param requiredVersion Minimum required version (e.g., "14.0.0")
     * @return True if version requirement is met, false otherwise
     */
    fun isNodeVersionSufficient(requiredVersion: String): Boolean {
        val localVersion = getLocalNodeVersion()
        if (localVersion == null) {
            LOG.warn("Cannot check Node.js version because it is not installed or not found")
            return false
        }

        return try {
            val local = Version(localVersion)
            val required = Version(requiredVersion)
            local >= required
        } catch (e: Exception) {
            LOG.error("Failed to compare Node.js versions", e)
            false
        }
    }

    /**
     * Simple version comparison class
     */
    private data class Version(val version: String) : Comparable<Version> {
        private val parts = version.removePrefix("v").split(".").map { it.toInt() }

        override fun compareTo(other: Version): Int {
            val size = maxOf(parts.size, other.parts.size)
            for (i in 0 until size) {
                val p1 = parts.getOrElse(i) { 0 }
                val p2 = other.parts.getOrElse(i) { 0 }
                if (p1 != p2) {
                    return p1 - p2
                }
            }
            return 0
        }
    }
}
