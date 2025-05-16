package ru.itmo.ivandor.plugin.service.auth

import com.intellij.collaboration.auth.OAuthCallbackHandlerBase
import com.intellij.collaboration.auth.services.OAuthService
import ru.itmo.ivandor.plugin.settings.PluginStorage.Companion.SERVICE_URL

internal class MSAuthCallbackHandler : OAuthCallbackHandlerBase() {
    override fun oauthService(): OAuthService<*> = PluginOAuthService.instance

    override fun handleOAuthResult(oAuthResult: OAuthService.OAuthResult<*>): AcceptCodeHandleResult {
        val redirectUrl = if (oAuthResult.isAccepted) {
            SERVICE_URL.resolve("oauth_success")
        }
        else {
            SERVICE_URL.resolve("oauth_error")
        }
        return AcceptCodeHandleResult.Redirect(redirectUrl)
    }
}
