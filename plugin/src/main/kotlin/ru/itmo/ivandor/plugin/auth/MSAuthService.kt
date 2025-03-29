package ru.itmo.ivandor.plugin.auth

import com.intellij.collaboration.auth.credentials.Credentials
import com.intellij.collaboration.auth.services.OAuthCredentialsAcquirer
import com.intellij.collaboration.auth.services.OAuthRequest
import com.intellij.collaboration.auth.services.OAuthServiceBase
import com.intellij.collaboration.auth.services.PkceUtils
import com.intellij.util.Url
import com.intellij.util.Urls.newFromEncoded
import java.util.*
import java.util.concurrent.CompletableFuture
import org.jetbrains.ide.BuiltInServerManager
import org.jetbrains.ide.RestService
import ru.itmo.ivandor.plugin.settings.MsSettings

internal class MSAuthService : OAuthServiceBase<Credentials>() {
    override val name: String get() = SERVICE_NAME

    fun authorize(): CompletableFuture<Credentials> {
        return authorize(MSAuthRequest())
    }

    override fun revokeToken(token: String) {
        TODO("Not yet implemented")
    }

    private class MSAuthRequest : OAuthRequest<Credentials> {
        private val port: Int get() = BuiltInServerManager.getInstance().port

        override val authorizationCodeUrl: Url
            get() = newFromEncoded("http://127.0.0.1:$port/${RestService.PREFIX}/$SERVICE_NAME/authorization_code")

        override val credentialsAcquirer: OAuthCredentialsAcquirer<Credentials> = MSAuthCredentialsAcquirer()

        override val authUrlWithParameters: Url = newFromEncoded("${MsSettings.HOST}/code?redirect_uri=${authorizationCodeUrl.toExternalForm()}")
    }

    companion object {
        private const val SERVICE_NAME = "microservices"

        val instance: MSAuthService = MSAuthService()

        // FIXME
        val SERVICE_URL: Url = newFromEncoded("https://account.jetbrains.com/github/oauth/intellij")
    }
}