package org.jetbrains.kotlinconf.backend

import com.google.gson.GsonBuilder
import io.ktor.application.*
import io.ktor.auth.Principal
import io.ktor.auth.authentication
import io.ktor.cio.toByteArray
import io.ktor.content.TextContent
import io.ktor.content.default
import io.ktor.content.files
import io.ktor.content.static
import io.ktor.features.*
import io.ktor.gson.GsonConverter
import io.ktor.http.*
import io.ktor.pipeline.PipelineContext
import io.ktor.request.ApplicationReceiveRequest
import io.ktor.request.contentCharset
import io.ktor.request.header
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.Routing
import io.ktor.util.error
import io.ktor.websocket.WebSockets
import kotlinx.coroutines.experimental.io.ByteReadChannel
import kotlinx.coroutines.experimental.io.jvm.javaio.toInputStream
import kotlinx.serialization.json.JSON
import kotlinx.serialization.serializerByClass
import kotlinx.serialization.serializerByValue
import org.jetbrains.kotlinconf.GsonDateDeserializer

val gson = GsonBuilder().apply {
    setPrettyPrinting()
    serializeNulls()
    GsonDateDeserializer.register(this)
}.create()

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
