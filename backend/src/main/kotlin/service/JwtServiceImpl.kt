package ru.itmo.ivandor.service

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.serialization.Serializable
import ru.itmo.ivandor.utils.SettingsImpl
import java.util.Date

@Serializable
data class GitHubResponse(
    val login: String,
)

@Serializable
data class GitHubAuthResponse(
    val access_token: String,
)


class JwtServiceImpl(
    private val client: HttpClient,
) : JwtService {
    override suspend fun generateJwt(oauthCode: String): String {
        val clientId = SettingsImpl.oauthClientId
        val secret = SettingsImpl.oauthSecret
        val body = client.post("https://github.com/login/oauth/access_token?client_id=$clientId&code=$oauthCode&client_secret=$secret")
        println("BODY:: $body")
        val oauthToken = body.body<GitHubAuthResponse>().access_token
        println("oauth: $oauthCode")
        val login = client.get("https://api.github.com/user"){
            header("Authorization", "Bearer $oauthToken")
        }.body<GitHubResponse>().login
        println("login: $login")
        val token = JWT.create()
            .withClaim("login", login)
            .withExpiresAt(Date(System.currentTimeMillis() + SettingsImpl.jwtLifetimeMs))
            .sign(Algorithm.HMAC256(SettingsImpl.jwtSecret))
        return token
    }
}