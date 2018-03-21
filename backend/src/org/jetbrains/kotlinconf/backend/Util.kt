package org.jetbrains.kotlinconf.backend

import io.ktor.application.*
import io.ktor.cio.*
import io.ktor.content.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.pipeline.*
import io.ktor.request.*
import kotlinx.coroutines.experimental.io.*
import kotlinx.serialization.*
import kotlinx.serialization.json.*
import org.jetbrains.kotlinconf.util.*
import java.time.*
import java.time.format.*
import java.util.*
import java.util.Date

private val dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss", Locale.US)

fun Date.toLocalDateTime(format: DateTimeFormatter = dateFormat): LocalDateTime =
        LocalDateTime.parse(apiDateFormat.format(this), format)


class SerialContentConverter(val json: JSON = JSON.plain) : ContentConverter {
    override suspend fun convertForReceive(context: PipelineContext<ApplicationReceiveRequest, ApplicationCall>): Any? {
        val request = context.subject
        val channel = request.value as? ByteReadChannel ?: return null
        val input = String(channel.toByteArray(), context.call.request.contentCharset() ?: Charsets.UTF_8)
        val type = request.type
        return json.parse(serializerByClass(type), input)
    }

    override suspend fun convertForSend(
        context: PipelineContext<Any, ApplicationCall>,
        contentType: ContentType,
        value: Any
    ): Any? {
        return TextContent(
            json.stringify(serializerByValue(value), value),
            contentType.withCharset(context.call.suitableCharset())
        )
    }
}
