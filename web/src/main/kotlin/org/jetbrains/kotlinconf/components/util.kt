package org.jetbrains.kotlinconf.components

import org.jetbrains.kotlinconf.util.*
import react.*
import react.dom.*

inline fun <T : RState> React.Component<*, T>.setState(action: T.() -> Unit) {
    setState(jsObject(action))
}

inline fun <T : RState> React.Component<*, T>.updateState(action: T.() -> Unit) {
    setState(clone(state).apply(action))
}

inline fun <T> RBuilder.loading(value: T?, action: (T) -> Unit) {
    if (value == null) {
        div(classes = "loading") {
            +"Loading data..."
        }
    } else {
        action(value)
    }
}

fun RBuilder.dateRange(range: Pair<String?, String?>) =
        dateRange(range.first?.let { parseDate(it) } to range.second?.let { parseDate(it) })

fun RBuilder.dateRange(range: Pair<Date?, Date?>) {
    val (startsAt, endsAt) = range
    div(classes = "session-time") {
        if (startsAt != null) {
            +if (endsAt != null) {
                (startsAt to endsAt).toReadableString()
            } else {
                startsAt.toReadableDateTimeString()
            }
        } else {
            span(classes = "session-time-unknown") { +"Time unknown" }
        }
    }
}


@Suppress("NOTHING_TO_INLINE")
inline fun Double.toFixed(precision: Int): String = asDynamic().toFixed(precision)

inline fun <T : Any> jsObject(builder: T.() -> Unit): T {
    val obj: T = js("({})")
    return obj.apply {
        builder()
    }
}

inline fun js(builder: dynamic.() -> Unit): dynamic = jsObject(builder)

fun <T : Any> clone(obj: T) = assign(jsObject<T> {}, obj)
inline fun <T : Any> assign(obj: T, builder: T.() -> Unit) = clone(obj).apply(builder)

external fun <T, R : T> assign(dest: R, src: T): R

fun toPlainObjectStripNull(obj: Any) = js {
    for (key in Object.keys(obj)) {
        val value = obj.asDynamic()[key]
        if (value != null) this[key] = value
    }
}
