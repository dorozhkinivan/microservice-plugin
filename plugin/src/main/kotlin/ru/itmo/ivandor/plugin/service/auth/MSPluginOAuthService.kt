package ru.itmo.ivandor.plugin.service.auth

import com.intellij.collaboration.auth.credentials.Credentials
import com.intellij.collaboration.auth.services.OAuthCredentialsAcquirer
import com.intellij.collaboration.auth.services.OAuthRequest
import com.intellij.collaboration.auth.services.OAuthServiceBase
import com.intellij.util.Url
import com.intellij.util.Urls.newFromEncoded
import java.util.concurrent.CompletableFuture
import org.jetbrains.ide.BuiltInServerManager
import org.jetbrains.ide.RestService
import ru.itmo.ivandor.plugin.settings.PluginStorage

class PluginOAuthService : OAuthServiceBase<Credentials>() {
    override val name = SERVICE_NAME

    fun authorize(): CompletableFuture<Credentials> = authorize(MSAuthRequest())

    override fun revokeToken(token: String) {}

    private class MSAuthRequest : OAuthRequest<Credentials> {
        private val port: Int get() = BuiltInServerManager.getInstance().port

        override val authorizationCodeUrl: Url
            get() = newFromEncoded("http://127.0.0.1:$port/${RestService.PREFIX}/$SERVICE_NAME/authorization_code")

        override val credentialsAcquirer: OAuthCredentialsAcquirer<Credentials> = MSAuthCredentialsAcquirer()

        override val authUrlWithParameters: Url = newFromEncoded("${PluginStorage.HOST}/code?redirect_uri=${authorizationCodeUrl.toExternalForm()}")
    }

    companion object {
        private const val SERVICE_NAME = "microservices"

        val instance: PluginOAuthService = PluginOAuthService()
    }
}