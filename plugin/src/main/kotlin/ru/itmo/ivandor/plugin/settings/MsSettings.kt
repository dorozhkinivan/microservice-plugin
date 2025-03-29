package ru.itmo.ivandor.plugin.settings

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.SettingsCategory
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage

@Service(Service.Level.APP)
@State(
    name = "MsSettings",
    storages = [Storage(value = "microservices_plugin.xml")],
    category = SettingsCategory.TOOLS
)
class MsSettings : PersistentStateComponent<MsSettings.State> {
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
        var HOST: String = "http://localhost:8080"

        val instance: MsSettings
            get() = ApplicationManager.getApplication().getService(MsSettings::class.java)
    }
}