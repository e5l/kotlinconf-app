package org.jetbrains.kotlinconf.backend

import org.jetbrains.kotlinconf.Date
import org.jetbrains.kotlinconf.apiDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

private val dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss", Locale.US)

fun Date.toLocalDateTime(format: DateTimeFormatter = dateFormat): LocalDateTime =
        LocalDateTime.parse(apiDateFormat.format(this), format)
