package ru.itmo.ivandor.api

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import ru.itmo.ivandor.models.Request
import ru.itmo.ivandor.service.JwtService
import ru.itmo.ivandor.service.YandexGPTService
import ru.itmo.ivandor.utils.SettingsImpl
import java.util.Date

fun Route.processRoute() {
    val service : YandexGPTService by inject()

    post("/process") {
        val body = call.receive<Request>()
        val login = call.getLogin()
        val a = service.getMicroservicesModules(body.classes, null)
        call.respond(HttpStatusCode.OK, a)
    }
}

fun Route.completeRoute() {
    post("/complete") {
        val login = call.getLogin()
        call.respond(HttpStatusCode.OK)
    }
}

fun Routing.newJwt() {
    val service : JwtService by inject()

    post("/jwt") {
        call.respond(HttpStatusCode.OK, service.generateJwt(call.request.queryParameters["code"]!!))
    }
}

fun Routing.proxyForOauthCode() {
    get("/code") {
        val redirectUrl = call.request.queryParameters["redirect_uri"]!!
        val clientId = SettingsImpl.oauthClientId
        call.respondRedirect("https://github.com/login/oauth/authorize?client_id=$clientId&redirect_uri=$redirectUrl",permanent = true)
    }
}

private fun ApplicationCall.getLogin() = this.principal<JWTPrincipal>()?.payload?.getClaim("login")?.asString()?.takeIf { it.isNotEmpty() } ?: throw RuntimeException("Bad auth, no login provided")