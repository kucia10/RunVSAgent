package com.sina.weibo.agent.eclipse.neo

import org.eclipse.ui.plugin.AbstractUIPlugin
import org.osgi.framework.BundleContext

class Activator : AbstractUIPlugin() {

    private var pluginService: PluginService? = null

    override fun start(context: BundleContext) {
        super.start(context)
        INSTANCE = this
        pluginService = PluginService()
        println("Hello, World from RunVSAgent Eclipse Plugin (Neo)!")
    }

    override fun stop(context: BundleContext) {
        pluginService?.dispose()
        pluginService = null
        INSTANCE = null
        super.stop(context)
    }

    fun getService(): PluginService? {
        return pluginService
    }

    companion object {
        var INSTANCE: Activator? = null
            private set
    }
}
