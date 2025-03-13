package ru.itmo.ivandor.handlers

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import ru.itmo.ivandor.service.YandexGPTService
import java.util.Date

private val processBody = """
            {
                "microservices" : {
                    "Auth" : [
                        "AuthService.class",
                        "JwtService.class"
                    ]
                }
            }
        """.trimIndent()

fun Routing.processRoute() {
    val service : YandexGPTService by inject()

    post("/process") {
        val a = service.getMicroservicesModules(listOf(), null)
        call.respond(HttpStatusCode.OK, a)
    }
}

fun Routing.completeRoute() {
    post("/complete") {
        call.respond(HttpStatusCode.OK)
    }
}

fun Routing.newJwt() {
    post("/jwt") {
        val secret = "sfsffjkd  nfjosnfdsf"
        val login = call.request.queryParameters["login"] ?: throw RuntimeException()

        val token = JWT.create()
            .withClaim("username", login)
            .withExpiresAt(Date(System.currentTimeMillis() + 60000))
            .sign(Algorithm.HMAC256(secret))

        call.respond(HttpStatusCode.OK, token)
    }
}