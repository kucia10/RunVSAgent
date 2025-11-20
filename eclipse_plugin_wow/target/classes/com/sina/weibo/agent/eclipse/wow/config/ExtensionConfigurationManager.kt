package com.sina.weibo.agent.eclipse.wow.config

import com.sina.weibo.agent.eclipse.wow.plugin.Activator
import org.eclipse.jface.preference.IPreferenceStore

object ExtensionConfigurationManager {

    private const val CURRENT_EXTENSION_ID_KEY = "currentExtensionId"

    private val preferenceStore: IPreferenceStore by lazy {
        Activator.plugin!!.preferenceStore
    }

    fun getCurrentExtensionId(): String? {
        return preferenceStore.getString(CURRENT_EXTENSION_ID_KEY)
    }

    fun setCurrentExtensionId(extensionId: String) {
        preferenceStore.setValue(CURRENT_EXTENSION_ID_KEY, extensionId)
    }

    fun isConfigurationValid(): Boolean {
        return !getCurrentExtensionId().isNullOrBlank()
    }
}
