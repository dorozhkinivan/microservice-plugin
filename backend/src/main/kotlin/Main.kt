package ru.itmo.ivandor

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.callloging.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import org.slf4j.event.Level
import ru.itmo.ivandor.handlers.completeRoute
import ru.itmo.ivandor.handlers.newJwt
import ru.itmo.ivandor.handlers.processRoute
import ru.itmo.ivandor.service.YandexGPTService
import ru.itmo.ivandor.service.YandexGPTServiceImpl

fun main() {
    embeddedServer(Netty, port = 8080) {

        install(Koin) {
            modules(mainModule)
        }
        install(io.ktor.server.plugins.contentnegotiation.ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
            })
        }



        install(CallLogging){
            level = Level.INFO
        }

        install(Authentication) {
            jwt("auth-jwt") {
                realm = "aboba"
            }
        }

        routing {
            completeRoute()
            processRoute()
            newJwt()
        }
    }.start(wait = true)
}

val mainModule = module {
    single {
        HttpClient(CIO) {
            install(HttpTimeout) {
                this.requestTimeoutMillis = 60000
            }
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                })
            }
        }
    }

    single<YandexGPTService> {
        YandexGPTServiceImpl(get())
    }
    // Other dependencies can be defined here
}