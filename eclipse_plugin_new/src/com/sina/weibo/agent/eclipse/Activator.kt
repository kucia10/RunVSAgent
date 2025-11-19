package com.sina.weibo.agent.eclipse

import org.osgi.framework.BundleActivator
import org.osgi.framework.BundleContext
import org.eclipse.core.runtime.Platform

class Activator : BundleActivator {

    override fun start(context: BundleContext) {
        val bundle = Platform.getBundle("com.sina.weibo.agent.eclipse")
        val logger = Platform.getLog(bundle)
        logger.info("Weibo Agent Eclipse Plugin started.")
    }

    override fun stop(context: BundleContext) {
        // No action needed on stop for this minimal implementation.
    }
}
