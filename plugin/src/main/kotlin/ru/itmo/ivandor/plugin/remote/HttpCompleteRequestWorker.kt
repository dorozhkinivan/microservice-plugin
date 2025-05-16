package ru.itmo.ivandor.plugin.remote

import com.google.gson.GsonBuilder
import com.intellij.openapi.diagnostic.logger
import com.intellij.util.io.HttpRequests
import java.net.HttpURLConnection
import javax.swing.SwingWorker
import ru.itmo.ivandor.plugin.actions.BusinessLogicHolder
import ru.itmo.ivandor.plugin.dto.ResultDto
import ru.itmo.ivandor.plugin.settings.PluginStorage

private fun BusinessLogicHolder.buildResultDto() : ResultDto {
    val originalNames = this.classesFromRemote.toSet()
    val modifiedNames = this.microservices.flatMap { ms -> ms.value.classes.mapNotNull { it.name } }.toSet()

    return ResultDto(
        requestId = this.requestId!!,
        createdFacades = modifiedNames - originalNames,
        removedFacades = originalNames - modifiedNames,
    )
}

class HttpCompleteRequestWorker(
    private val businessLogicHolder: BusinessLogicHolder,
) : SwingWorker<Unit, Unit>() {
    override fun doInBackground() {
        businessLogicHolder.requestId ?: return
        val jwtFromSettings = PluginStorage.instance.state.jwtToken

        val jsonBody = GsonBuilder().create().toJson(businessLogicHolder.buildResultDto())
        return HttpRequests.post("${PluginStorage.HOST}/complete", "application/json")
            .tuner {
                it.setRequestProperty("Authorization", "Bearer $jwtFromSettings")
            }.throwStatusCodeException(false)
            .connect { request -> request.write(jsonBody)
                val responseCode = (request.connection as HttpURLConnection).responseCode
                logger<HttpCompleteRequestWorker>()
                    .info("Got response with status $responseCode when send completion data")
            }
    }
}