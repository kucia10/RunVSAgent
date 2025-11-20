package com.sina.weibo.agent.eclipse.wow.util

class NodeVersion(val major: Int, val minor: Int, val patch: Int, val version: String)

object NodeVersionUtil {
    fun getNodeVersion(path: String): NodeVersion {
        // Placeholder
        return NodeVersion(20, 6, 0, "20.6.0")
    }

    fun isVersionSupported(version: NodeVersion, minVersion: NodeVersion): Boolean {
        // Placeholder
        return true
    }
}
