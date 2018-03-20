package org.jetbrains.kotlinconf.backend

import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.cio.toByteArray
import io.ktor.content.TextContent
import io.ktor.features.ContentConverter
import io.ktor.features.suitableCharset
import io.ktor.http.ContentType
import io.ktor.http.withCharset
import io.ktor.pipeline.PipelineContext
import io.ktor.request.ApplicationReceiveRequest
import io.ktor.request.contentCharset
import kotlinx.coroutines.experimental.io.ByteReadChannel
import kotlinx.serialization.json.JSON
import kotlinx.serialization.serializerByClass
import kotlinx.serialization.serializerByValue
import org.jetbrains.kotlinconf.Date
import org.jetbrains.kotlinconf.apiDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

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
