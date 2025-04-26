package ru.itmo.ivandor.plugin.settings

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.SettingsCategory
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage

@Service(Service.Level.APP)
@State(
    name = "PluginSettings",
    storages = [Storage(value = "microservices_plugin.xml")],
    category = SettingsCategory.TOOLS
)
class PluginSettings : PersistentStateComponent<PluginSettings.State> {
    private var myState = State()

    override fun getState(): State {
        return myState
    }

    override fun loadState(state: State) {
        myState = state
    }

    data class State (
        var JWT_TOKEN: String = ""
    )

    companion object {
        val HOST: String = "https://microservice-plugin.ru/api"

        val instance: PluginSettings
            get() = ApplicationManager.getApplication().getService(PluginSettings::class.java)
    }
}