package ru.itmo.ivandor.plugin.remote

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.ui.Messages
import com.intellij.util.io.HttpRequests
import java.net.HttpURLConnection
import javax.swing.SwingWorker
import ru.itmo.ivandor.plugin.actions.BusinessLogicHolder
import ru.itmo.ivandor.plugin.actions.MicroservicesConfigVirtualFile
import ru.itmo.ivandor.plugin.service.auth.PluginOAuthService
import ru.itmo.ivandor.plugin.dto.ClassDto
import ru.itmo.ivandor.plugin.dto.MicroserviceDto
import ru.itmo.ivandor.plugin.dto.RequestDto
import ru.itmo.ivandor.plugin.dto.ResponseDto
import ru.itmo.ivandor.plugin.settings.PluginStorage

class HttpProcessRequestWorker(
    private val file: MicroservicesConfigVirtualFile,
    private val businessLogicHolder: BusinessLogicHolder,
    private val block: (List<MicroserviceDto>) -> Unit,
) : SwingWorker<List<MicroserviceDto>, Unit>() {


    private fun getMicroservicesOrNullOn401(jwt: String) : List<MicroserviceDto>? {
        val classes = file.classes.mapNotNull {
            val methods = it.filteredMethods
            if (methods.isEmpty()) null
            else ClassDto(name = it.name ?: "?", methods = methods)
        }
        val jsonBody = GsonBuilder().create().toJson(RequestDto(classes))
        return HttpRequests.post("${PluginStorage.HOST}/process", "application/json")
            .tuner {
                it.setRequestProperty("Authorization", "Bearer $jwt")
            }.throwStatusCodeException(false)
            .connect { request -> request.write(jsonBody)
                val responseCode = (request.connection as HttpURLConnection).responseCode
                when(responseCode){
                    HttpURLConnection.HTTP_OK -> Gson().fromJson(request.readString(), ResponseDto::class.java).let {
                        businessLogicHolder.requestId = it.requestId
                        it.microservices
                    }
                    HttpURLConnection.HTTP_UNAUTHORIZED -> null
                    else -> throw RuntimeException()
                }
            }
    }

    override fun doInBackground(): List<MicroserviceDto>? {

        val notification = Notification(
            "Microservices",
            "Microservices plugin enabled",
            "Files of the selected directories are being analyzed",
            NotificationType.INFORMATION
        )
        Notifications.Bus.notify(notification)

        val jwtFromSettings = PluginStorage.instance.state.jwtToken

        val res = if (jwtFromSettings.isEmpty()) null else getMicroservicesOrNullOn401(jwtFromSettings)

        return res
    }

    override fun done() {
        try {
            val result = get() ?: when(Messages.showYesNoCancelDialog(businessLogicHolder.project, "Authorize with github? Otherwise you can configure facades without LLM.", "Authorization", Messages.getQuestionIcon())){
                Messages.YES -> {
                    val token = PluginOAuthService.instance.authorize().get().accessToken
                    PluginStorage.instance.loadState(PluginStorage.instance.state.apply { this.jwtToken = token })
                    getMicroservicesOrNullOn401(token)!!
                }
                Messages.NO -> {
                    emptyList()
                }
                else -> {
                    FileEditorManager.getInstance(businessLogicHolder.project).closeFile(file)
                    emptyList()
                }
            }
            block(result)
        } catch (e: Exception) {
            e.printStackTrace()
            val notification = Notification(
                "Microservices",
                "Backend error",
                "Can not get facades from backend. You can configure custom facades🙃",
                NotificationType.WARNING
            )
            Notifications.Bus.notify(notification)
            block(listOf())
        }
    }
}