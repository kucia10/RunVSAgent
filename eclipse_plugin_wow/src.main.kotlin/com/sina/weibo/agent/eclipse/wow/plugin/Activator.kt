package com.sina.weibo.agent.eclipse.wow.plugin

import org.eclipse.ui.plugin.AbstractUIPlugin
import org.osgi.framework.BundleContext

class Activator : AbstractUIPlugin() {

    override fun start(context: BundleContext) {
        super.start(context)
        plugin = this
        println("RunVSAgent-wow plugin started")
    }

    override fun stop(context: BundleContext) {
        plugin = null
        super.stop(context)
    }

    companion object {
        @JvmStatic
        var plugin: Activator? = null
            private set
    }
}
