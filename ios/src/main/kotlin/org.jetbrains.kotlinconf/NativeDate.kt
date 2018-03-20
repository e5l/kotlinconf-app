package org.jetbrains.kotlinconf

import platform.Foundation.*

actual class Date constructor(val origin: NSDate) {

    actual constructor() : this(NSDate.dateWithTimeIntervalSinceReferenceDate(0.0))

    actual fun getDate(): Int = 0
    actual fun getMonth(): Int = 0
    actual fun getFullYear(): Int = 0
    actual fun getHours(): Int = 0
    actual fun getMinutes(): Int = 0
    actual fun getTime(): Number = 0

    actual companion object
}

actual fun parseDate(dateString: String): Date = Date()
actual fun Date.toReadableDateString(): String = WEEKDAY_TIME_FORMATTER.stringFromDate(origin)
actual fun Date.toReadableTimeString(): String = ONLY_TIME_FORMATTER.stringFromDate(origin)

actual operator fun Date.compareTo(otherDate: Date): Int {
    val diff = origin.timeIntervalSince1970 - otherDate.origin.timeIntervalSince1970
    return when {
        diff < 0 -> -1
        diff > 0 -> 1
        else -> 0
    }
}

private fun createDateFormatter(format: String) = NSDateFormatter().apply {
    dateFormat = format
    timeZone = NSTimeZone.timeZoneWithAbbreviation("UTC")!!
}

private val FULL_DATE_FORMATTER = createDateFormatter("EEEE, MMMM d h:mm a")
private val ONLY_TIME_FORMATTER = createDateFormatter("h:mm a")
private val WEEKDAY_TIME_FORMATTER = createDateFormatter("EEEE h:mm a")

private val IEEE_DATE_PARSER = createDateFormatter("yyyy-MM-dd'T'HH:mm:ss")

fun renderDate(date: Date): String = FULL_DATE_FORMATTER.stringFromDate(date.origin)
fun renderWeekdayTime(date: Date): String = WEEKDAY_TIME_FORMATTER.stringFromDate(date.origin)
fun renderInterval(start: Date, end: Date): String =
        FULL_DATE_FORMATTER.stringFromDate(start.origin) + " – " + ONLY_TIME_FORMATTER.stringFromDate(end.origin)
