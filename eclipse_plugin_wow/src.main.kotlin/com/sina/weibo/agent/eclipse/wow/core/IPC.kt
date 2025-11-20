package com.sina.weibo.agent.eclipse.wow.core

import com.sina.weibo.agent.eclipse.wow.plugin.Activator
import org.eclipse.core.runtime.ILog
import org.eclipse.core.runtime.Platform

interface ISocketServer {
    fun start(projectPath: String): String
    fun stop()
}

class ExtensionSocketServer : ISocketServer {
    companion object {
        private val LOG: ILog = Platform.getLog(Activator.plugin?.bundle)
    }
    override fun start(projectPath: String): String {
        LOG.info("Starting ExtensionSocketServer...")
        // Placeholder
        return "51234"
    }
    override fun stop() {
        LOG.info("Stopping ExtensionSocketServer...")
        // Placeholder
    }
}

class ExtensionUnixDomainSocketServer : ISocketServer {
    companion object {
        private val LOG: ILog = Platform.getLog(Activator.plugin?.bundle)
    }
    override fun start(projectPath: String): String {
        LOG.info("Starting ExtensionUnixDomainSocketServer...")
        // Placeholder
        return "/tmp/runvsagent.sock"
    }
    override fun stop() {
        LOG.info("Stopping ExtensionUnixDomainSocketServer...")
        // Placeholder
    }
}

class ExtensionProcessManager {
    companion object {
        private val LOG: ILog = Platform.getLog(Activator.plugin?.bundle)
    }
    fun start(portOrPath: String): Boolean {
        LOG.info("Starting extension process with port/path: $portOrPath")
        // Placeholder
        return true
    }
    fun stop() {
        LOG.info("Stopping extension process...")
        // Placeholder
    }
}
