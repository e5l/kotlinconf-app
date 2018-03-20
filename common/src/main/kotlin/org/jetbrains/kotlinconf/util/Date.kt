package org.jetbrains.kotlinconf.util

import kotlinx.serialization.KInput
import kotlinx.serialization.KOutput
import kotlinx.serialization.KSerialClassDesc
import kotlinx.serialization.KSerializer
import kotlinx.serialization.internal.SerialClassDescImpl

expect class Date() {
    fun getDate(): Int
    fun getMonth(): Int
    fun getFullYear(): Int
    fun getHours(): Int
    fun getMinutes(): Int
    fun getTime(): Number

    companion object
}

object CommonDateSerializer : KSerializer<Date> {
    override val serialClassDesc: KSerialClassDesc = SerialClassDescImpl("org.jetbrains.kotlinconf.Date")

    override fun load(input: KInput): Date {
        return parseDate(input.readStringValue())
    }

    override fun save(output: KOutput, obj: Date) {
        output.writeStringValue(obj.toReadableDateTimeString())
    }
}

val Date.Companion.serializer: KSerializer<Date>
    get() = CommonDateSerializer

expect operator fun Date.compareTo(otherDate: Date): Int

expect fun parseDate(dateString: String): Date
expect fun Date.toReadableDateString(): String
expect fun Date.toReadableTimeString(): String

fun Date.toReadableDateTimeString() = "${toReadableDateString()} ${toReadableTimeString()}"
