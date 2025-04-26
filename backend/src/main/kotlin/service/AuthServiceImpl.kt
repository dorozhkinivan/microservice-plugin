package ru.itmo.ivandor.service

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import ru.itmo.ivandor.models.GitHubAuthResponse
import ru.itmo.ivandor.models.GitHubResponse
import ru.itmo.ivandor.utils.Settings
import java.util.Date

class AuthServiceImpl(
    private val client: HttpClient,
) : AuthService {
    override suspend fun generateJwt(oauthCode: String): String {
        val clientId = Settings.oauthClientId
        val secret = Settings.oauthSecret
        val body = client.post("https://github.com/login/oauth/access_token?client_id=$clientId&code=$oauthCode&client_secret=$secret")
        val oauthToken = body.body<GitHubAuthResponse>().accessToken
        val login = client.get("https://api.github.com/user"){
            header("Authorization", "Bearer $oauthToken")
        }.body<GitHubResponse>().login

        val token = JWT.create()
            .withClaim("login", login)
            .withExpiresAt(Date(System.currentTimeMillis() + Settings.jwtLifetimeMs))
            .sign(Algorithm.HMAC256(Settings.jwtSecret))
        return token
    }
}