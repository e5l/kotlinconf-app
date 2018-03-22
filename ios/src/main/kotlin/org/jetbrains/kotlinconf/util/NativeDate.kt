package org.jetbrains.kotlinconf.util

import platform.Foundation.*

actual class Date constructor(internal val origin: NSDate) {

    actual constructor() : this(NSDate.dateWithTimeIntervalSinceReferenceDate(0.0))

    actual fun getDate(): Int = origin.getCalendarComponent(NSCalendarUnitDay)
    actual fun getMonth(): Int = origin.getCalendarComponent(NSCalendarUnitMonth)
    actual fun getFullYear(): Int = origin.getCalendarComponent(NSCalendarUnitYear)
    actual fun getHours(): Int = origin.getCalendarComponent(NSCalendarUnitHour)
    actual fun getMinutes(): Int = origin.getCalendarComponent(NSCalendarUnitMinute)
    actual fun getTime(): Number = origin.timeIntervalSince1970

    override fun equals(other: Any?): Boolean {
        return other is Date && other.compareTo(this) == 0
    }

    override fun toString(): String {
        return toReadableDateTimeString()
    }

    actual companion object
}

private fun NSDate.getCalendarComponent(unit: NSCalendarUnit): Int = NSCalendar.currentCalendar.component(unit, this).toInt()

actual fun parseDate(dateString: String): Date = Date(IEEE_DATE_PARSER.dateFromString(dateString)!!)
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
