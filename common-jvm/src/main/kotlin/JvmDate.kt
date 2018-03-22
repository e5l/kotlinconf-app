package org.jetbrains.kotlinconf.util

import java.text.*
import java.util.*
import java.util.Calendar.*

actual class Date {
    private val calendar: Calendar

    actual constructor() {
        calendar = Calendar.getInstance()
    }

    constructor(date: java.util.Date) {
        calendar = Calendar.getInstance().apply {
            time = date
        }
    }

    val date: java.util.Date get() = calendar.time

    actual fun getDate() = calendar[DAY_OF_MONTH]
    actual fun getMonth() = calendar[MONTH]
    actual fun getFullYear() = calendar[YEAR]
    actual fun getHours() = calendar[HOUR_OF_DAY]
    actual fun getMinutes() = calendar[MINUTE]
    actual fun getTime(): Number = calendar.timeInMillis

    override fun equals(other: Any?): Boolean = other is org.jetbrains.kotlinconf.util.Date && other.calendar.time == calendar.time
}

actual operator fun org.jetbrains.kotlinconf.util.Date.compareTo(otherDate: org.jetbrains.kotlinconf.util.Date): Int = date.compareTo(otherDate.date)

val apiDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
val readableDateFormat = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault())
val readableTimeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

actual fun parseDate(dateString: String): org.jetbrains.kotlinconf.util.Date = Date(apiDateFormat.parse(dateString))

actual fun org.jetbrains.kotlinconf.util.Date.toReadableDateString(): String = readableDateFormat.format(date)
actual fun org.jetbrains.kotlinconf.util.Date.toReadableTimeString(): String = readableTimeFormat.format(date)
