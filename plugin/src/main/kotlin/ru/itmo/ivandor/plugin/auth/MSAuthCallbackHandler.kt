package ru.itmo.ivandor.plugin.auth

import com.intellij.collaboration.auth.OAuthCallbackHandlerBase
import com.intellij.collaboration.auth.services.OAuthService

internal class MSAuthCallbackHandler : OAuthCallbackHandlerBase() {
    override fun oauthService(): OAuthService<*> = PluginOAuthService.instance

    override fun handleOAuthResult(oAuthResult: OAuthService.OAuthResult<*>): AcceptCodeHandleResult {
        val redirectUrl = if (oAuthResult.isAccepted) {
            PluginOAuthService.SERVICE_URL.resolve("oauth_success")
        }
        else {
            PluginOAuthService.SERVICE_URL.resolve("oauth_error")
        }
        return AcceptCodeHandleResult.Redirect(redirectUrl)
    }
}
