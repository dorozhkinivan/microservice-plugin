package ru.itmo.ivandor

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
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
import ru.itmo.ivandor.api.completeRoute
import ru.itmo.ivandor.api.newJwt
import ru.itmo.ivandor.api.processRoute
import ru.itmo.ivandor.api.proxyForOauthCode
import ru.itmo.ivandor.service.*
import ru.itmo.ivandor.utils.SettingsImpl
import java.sql.DriverManager

fun main() {
    try {
        // val DB_HOST
        val DB_NAME = "test_db"
        val DB_USER = "admin"
        //  val DB_PASS

        val CACERT = "/usr/local/share/ca-certificates/Yandex/RootCA.crt"

        val DB_URL =
            String.format("jdbc:clickhouse://%s:8443/%s?ssl=1&sslmode=strict&sslrootcert=%s", System.getenv("DB_HOST"), DB_NAME, CACERT)

        java.lang.Class.forName("com.clickhouse.jdbc.ClickHouseDriver")
        val conn = DriverManager.getConnection(DB_URL, DB_USER, System.getenv("DB_PASS"))
        val rs = conn.createStatement().executeQuery("SELECT version()")
        if (rs.next()) {
            println(rs.getString(1))
        }
        conn.close()
        throw RuntimeException("SUCCESS")
    }
    catch (ex: Exception) {
        ex.printStackTrace()
    }
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
                verifier(
                    JWT
                        .require(Algorithm.HMAC256(SettingsImpl.jwtSecret))
                        .build()
                )

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
            newJwt()
            proxyForOauthCode()
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

    single<YandexGPTService> { YandexGPTServiceImpl(get()) }
    single<JwtService> { JwtServiceImpl(get()) }
}