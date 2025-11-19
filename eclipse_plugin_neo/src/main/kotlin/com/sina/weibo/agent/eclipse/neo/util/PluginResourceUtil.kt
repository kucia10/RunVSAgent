package com.sina.weibo.agent.eclipse.neo.util

import com.sina.weibo.agent.eclipse.neo.Activator
import org.eclipse.core.runtime.FileLocator
import org.eclipse.core.runtime.Path
import java.io.File
import java.io.IOException

object PluginResourceUtil {

    fun getResourcePath(bundleId: String, resourceRelativePath: String): String? {
        val bundle = Activator.INSTANCE?.bundle ?: return null
        val resourceUrl = FileLocator.find(bundle, Path(resourceRelativePath), null)
            ?: return null

        try {
            val fileUrl = FileLocator.toFileURL(resourceUrl)
            return File(fileUrl.toURI()).absolutePath
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        }
    }
}
