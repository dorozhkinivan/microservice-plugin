package ru.itmo.ivandor.plugin.auth

import com.intellij.collaboration.auth.OAuthCallbackHandlerBase
import com.intellij.collaboration.auth.services.OAuthService

internal class MSAuthCallbackHandler : OAuthCallbackHandlerBase() {
    override fun oauthService(): OAuthService<*> = MSAuthService.instance

    override fun handleOAuthResult(oAuthResult: OAuthService.OAuthResult<*>): AcceptCodeHandleResult {
        val redirectUrl = if (oAuthResult.isAccepted) {
            MSAuthService.SERVICE_URL.resolve("complete")
        }
        else {
            MSAuthService.SERVICE_URL.resolve("error")
        }
        return AcceptCodeHandleResult.Redirect(redirectUrl)
    }
}
