package ru.itmo.ivandor.api

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import ru.itmo.ivandor.models.Request
import ru.itmo.ivandor.models.CompleteDto
import ru.itmo.ivandor.service.AnalyticsService
import ru.itmo.ivandor.service.AuthService
import ru.itmo.ivandor.service.YandexGPTService
import ru.itmo.ivandor.utils.Settings

fun Route.processRoute() {
    val gptService : YandexGPTService by inject()
    post("/process") {
        val body = call.receive<Request>()
        val login = call.getLogin()
        val a = gptService.getMicroservicesModules(body.classes, login)
        call.respond(HttpStatusCode.OK, a)
    }
}

fun Route.completeRoute() {
    val analyticsService : AnalyticsService by inject()
    post("/complete") {
        val body = call.receive<CompleteDto>()
        analyticsService.saveResultData(body)
        call.respond(HttpStatusCode.OK)
    }
}

fun Routing.newJwtRoute() {
    val service : AuthService by inject()

    post("/jwt") {
        call.respond(HttpStatusCode.OK, service.generateJwt(call.request.queryParameters["code"]!!))
    }
}

fun Routing.proxyForOauthCodeRoute() {
    get("/code") {
        val redirectUrl = call.request.queryParameters["redirect_uri"]!!
        val clientId = Settings.oauthClientId
        call.respondRedirect("https://github.com/login/oauth/authorize?client_id=$clientId&redirect_uri=$redirectUrl",permanent = true)
    }
}

private fun ApplicationCall.getLogin() = this.principal<JWTPrincipal>()?.payload?.getClaim("login")?.asString()?.takeIf { it.isNotEmpty() } ?: throw RuntimeException("Bad auth, no login provided")