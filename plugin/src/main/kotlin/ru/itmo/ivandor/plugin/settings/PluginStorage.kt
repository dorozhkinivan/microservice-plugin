package ru.itmo.ivandor.plugin.settings

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.SettingsCategory
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.Url
import com.intellij.util.Urls.newFromEncoded

@Service(Service.Level.APP)
@State(
    name = "PluginStorage",
    storages = [Storage(value = "microservices_plugin.xml")],
    category = SettingsCategory.TOOLS
)
class PluginStorage : PersistentStateComponent<PluginStorage.State> {
    private var myState = State()

    override fun getState(): State {
        return myState
    }

    override fun loadState(state: State) {
        myState = state
    }

    data class State (
        var jwtToken: String = ""
    )

    companion object {
        val SERVICE_URL: Url = newFromEncoded("https://microservice-plugin.ru")
        val HOST: String = "$SERVICE_URL/api"

        val instance: PluginStorage
            get() = ApplicationManager.getApplication().getService(PluginStorage::class.java)
    }
}