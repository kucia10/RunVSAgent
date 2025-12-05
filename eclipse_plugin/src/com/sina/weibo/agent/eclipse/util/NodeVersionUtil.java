/*
 * SPDX-FileCopyrightText: 2025 Weibo, Inc.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.sina.weibo.agent.eclipse.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import com.sina.weibo.agent.eclipse.Activator;

/**
 * Utility class for Node.js version checking.
 */
public class NodeVersionUtil {

    /**
     * Check if Node.js version satisfies the minimum requirement.
     *
     * @param nodePath the path to Node.js executable
     * @param minVersion the minimum required version (e.g., "18.0.0")
     * @return true if version is satisfied
     */
    public static boolean isVersionSatisfied(String nodePath, String minVersion) {
        String currentVersion = getNodeVersion(nodePath);
        if (currentVersion == null) {
            return false;
        }
        return compareVersions(currentVersion, minVersion) >= 0;
    }

    /**
     * Get Node.js version.
     *
     * @param nodePath the path to Node.js executable
     * @return the version string or null
     */
    public static String getNodeVersion(String nodePath) {
        try {
            ProcessBuilder builder = new ProcessBuilder(nodePath, "--version");
            builder.redirectErrorStream(true);
            Process process = builder.start();

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                String line = reader.readLine();
                if (line != null && line.startsWith("v")) {
                    return line.substring(1).trim();
                }
            }

            process.waitFor();
        } catch (Exception e) {
            Activator.logError("Failed to get Node.js version", e);
        }
        return null;
    }

    /**
     * Compare two version strings.
     *
     * @param version1 first version
     * @param version2 second version
     * @return negative if version1 < version2, zero if equal, positive if version1 > version2
     */
    public static int compareVersions(String version1, String version2) {
        String[] parts1 = version1.split("\\.");
        String[] parts2 = version2.split("\\.");

        int maxLength = Math.max(parts1.length, parts2.length);

        for (int i = 0; i < maxLength; i++) {
            int v1 = i < parts1.length ? parseVersionPart(parts1[i]) : 0;
            int v2 = i < parts2.length ? parseVersionPart(parts2[i]) : 0;

            if (v1 != v2) {
                return v1 - v2;
            }
        }

        return 0;
    }

    /**
     * Parse a version part (handle non-numeric suffixes).
     *
     * @param part the version part
     * @return the numeric value
     */
    private static int parseVersionPart(String part) {
        // Remove any non-numeric suffix (e.g., "18.12.0-rc.1" -> "18")
        StringBuilder numStr = new StringBuilder();
        for (char c : part.toCharArray()) {
            if (Character.isDigit(c)) {
                numStr.append(c);
            } else {
                break;
            }
        }
        
        if (numStr.length() == 0) {
            return 0;
        }
        
        try {
            return Integer.parseInt(numStr.toString());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
