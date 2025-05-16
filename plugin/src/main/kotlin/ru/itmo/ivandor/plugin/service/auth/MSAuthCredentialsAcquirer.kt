package ru.itmo.ivandor.plugin.service.auth

import com.intellij.collaboration.auth.credentials.Credentials
import com.intellij.collaboration.auth.credentials.SimpleCredentials
import com.intellij.collaboration.auth.services.OAuthCredentialsAcquirer
import com.intellij.collaboration.auth.services.OAuthCredentialsAcquirerHttp
import com.intellij.util.Urls.newFromEncoded
import ru.itmo.ivandor.plugin.settings.PluginStorage

class MSAuthCredentialsAcquirer : OAuthCredentialsAcquirer<Credentials> {
    override fun acquireCredentials(code: String): OAuthCredentialsAcquirer.AcquireCredentialsResult<Credentials> {
        val tokenUrl = newFromEncoded("${PluginStorage.HOST}/jwt?code=${code}")

        return OAuthCredentialsAcquirerHttp.requestToken(tokenUrl) { body, _ ->
            SimpleCredentials(body)
        }
    }
}