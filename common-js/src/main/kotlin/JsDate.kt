package org.jetbrains.kotlinconf.util

actual external class Date {
    actual constructor()
    constructor(value: Number)

    actual fun getDate(): Int
    actual fun getMonth(): Int
    actual fun getFullYear(): Int
    actual fun getHours(): Int
    actual fun getMinutes(): Int

    actual fun getTime(): Number

    companion object {
        fun parse(string: String): Number
    }
}

actual operator fun org.jetbrains.kotlinconf.util.Date.compareTo(otherDate: org.jetbrains.kotlinconf.util.Date) = getTime().toLong().compareTo(otherDate.getTime().toLong())

actual fun parseDate(dateString: String): org.jetbrains.kotlinconf.util.Date = Date(Date.parse(dateString))

actual fun org.jetbrains.kotlinconf.util.Date.toReadableDateString(): String {
    return "${monthAsString()} ${getDate()}, ${getFullYear()}"
}

actual fun org.jetbrains.kotlinconf.util.Date.toReadableTimeString(): String = "${readableHours()}:${getMinutes().asMinutesString()} ${timeSuffix()}"

private fun org.jetbrains.kotlinconf.util.Date.monthAsString(): String = months[getMonth()]

private val months = arrayOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
