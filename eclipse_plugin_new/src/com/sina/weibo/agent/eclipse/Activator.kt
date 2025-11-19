package com.sina.weibo.agent.eclipse

import org.osgi.framework.BundleActivator
import org.osgi.framework.BundleContext
import com.sina.weibo.agent.extensions.ui.contextmenu.DynamicContextMenuManager

class Activator : BundleActivator {

    override fun start(context: BundleContext) {
        INSTANCE = this
        DynamicContextMenuManager.getInstance().initialize()
    }

    override fun stop(context: BundleContext) {
        DynamicContextMenuManager.getInstance().dispose()
        INSTANCE = null
    }

    companion object {
        var INSTANCE: Activator? = null
            private set
    }
}
