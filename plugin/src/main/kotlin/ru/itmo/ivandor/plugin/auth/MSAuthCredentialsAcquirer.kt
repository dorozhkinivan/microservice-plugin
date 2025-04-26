package ru.itmo.ivandor.plugin.auth

import com.intellij.collaboration.auth.credentials.Credentials
import com.intellij.collaboration.auth.credentials.SimpleCredentials
import com.intellij.collaboration.auth.services.OAuthCredentialsAcquirer
import com.intellij.collaboration.auth.services.OAuthCredentialsAcquirerHttp
import com.intellij.util.Urls.newFromEncoded
import ru.itmo.ivandor.plugin.settings.PluginSettings

class MSAuthCredentialsAcquirer : OAuthCredentialsAcquirer<Credentials> {
    override fun acquireCredentials(code: String): OAuthCredentialsAcquirer.AcquireCredentialsResult<Credentials> {
        val tokenUrl1 = newFromEncoded("${PluginSettings.HOST}/jwt?code=${code}")

        return OAuthCredentialsAcquirerHttp.requestToken(tokenUrl1) { body, _ ->
            SimpleCredentials(body)
        }
    }
}