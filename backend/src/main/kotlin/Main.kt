package ru.itmo.ivandor

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.zaxxer.hikari.HikariDataSource
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.callloging.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json
import org.koin.dsl.module
import org.koin.dsl.onClose
import org.koin.ktor.ext.inject
import org.koin.ktor.plugin.Koin
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.event.Level
import ru.itmo.ivandor.api.*
import ru.itmo.ivandor.dao.AnalyticsDao
import ru.itmo.ivandor.dao.AnalyticsDaoImpl
import ru.itmo.ivandor.dao.createConnectionPool
import ru.itmo.ivandor.dao.warmUp
import ru.itmo.ivandor.service.*
import ru.itmo.ivandor.utils.Settings

val logger: Logger = LoggerFactory.getLogger("Main")

fun main() {
    embeddedServer(Netty, port = 8080) {

        install(Koin) {
            modules(module)
        }

        val databaseConnectionPool by inject<HikariDataSource>()
        warmUp(databaseConnectionPool)

        install(io.ktor.server.plugins.contentnegotiation.ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }

        install(CallLogging){
            level = Level.INFO
        }

        install(Authentication) {
            jwt("auth-jwt") {
                verifier(JWT.require(Algorithm.HMAC256(Settings.jwtSecret)).build())

                validate { credential ->
                    if (credential.payload.getClaim("login").asString() != "") {
                        JWTPrincipal(credential.payload)
                    } else {
                        null
                    }
                }
                challenge { _, _ ->
                    call.respond(HttpStatusCode.Unauthorized, "Token is not valid or has expired")
                }
            }
        }

        routing {
            authenticate("auth-jwt") {
                completeRoute()
                processRoute()
            }
            newJwtRoute()
            proxyForOauthCodeRoute()
        }
    }.start(wait = true)
}