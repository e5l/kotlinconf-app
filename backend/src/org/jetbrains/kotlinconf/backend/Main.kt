package org.jetbrains.kotlinconf.backend

import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.content.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.util.*
import io.ktor.websocket.*
import kotlinx.serialization.json.*

val kjson = JSON(nonstrict = true)

fun Application.main() {
    val config = environment.config.config("service")
    val mode = config.property("environment").getString()
    log.info("Environment: $mode")
    val production = mode == "production"

    if (!production) {
        install(CallLogging)
    }

    install(DefaultHeaders)
    install(ConditionalHeaders)
    install(Compression)
    install(PartialContentSupport)
    install(AutoHeadResponse)
    install(WebSockets)
    install(XForwardedHeadersSupport)
    install(StatusPages) {
        exception<Throwable> { cause ->
            environment.log.error(cause)
            call.respond(HttpStatusCode.InternalServerError)
        }
    }

    install(ContentNegotiation) {
        register(ContentType.Application.Json, SerialContentConverter(JSON(indented = true)))
    }

    install(CORS) {
        anyHost()
        header(HttpHeaders.Authorization)
        allowCredentials = true
        listOf(HttpMethod.Put, HttpMethod.Delete, HttpMethod.Options).forEach { method(it) }
    }

    val database = Database(this)
    install(Routing) {
        authenticate()
        static {
            default("static/index.html")
            files("static")
        }
        api(database, production)
    }

    launchSyncJob()
}

fun Route.authenticate() {
    val bearer = "Bearer "
    intercept(ApplicationCallPipeline.Infrastructure) {
        val authorization = call.request.header(HttpHeaders.Authorization) ?: return@intercept
        if (!authorization.startsWith(bearer)) return@intercept
        val token = authorization.removePrefix(bearer).trim()
        call.authentication.principal(KotlinConfPrincipal(token))
    }
}

class KotlinConfPrincipal(val token: String) : Principal
