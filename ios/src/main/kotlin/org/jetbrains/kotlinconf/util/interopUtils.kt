package org.jetbrains.kotlinconf.util

import kotlinx.cinterop.*
import platform.CoreData.*
import platform.Foundation.*
import platform.darwin.*

class NSErrorException(val error: NSError) : Exception()

interface DispatchQueue {
    companion object {
        val main: DispatchQueue = object : DispatchQueue {
            override fun async(block: () -> Unit) {
                dispatch_async(dispatch_get_main_queue()) { block() }
            }

            override fun asyncAfter(ms: Long, block: () -> Unit) {
                dispatch_after(dispatch_time(DISPATCH_TIME_NOW, ms * NSEC_PER_MSEC), dispatch_get_main_queue()) {
                    block()
                }
            }
        }
    }

    fun async(block: () -> Unit)
    fun asyncAfter(ms: Long, block: () -> Unit)
}

operator fun NSArray.get(index: Int): Any? {
    return this.objectAtIndex(index.toLong())
}

operator fun NSArray.get(index: Long): Any? {
    return this.objectAtIndex(index)
}

fun <T> NSArray?.toList(): kotlin.collections.List<T> {
    if (this == null) return listOf()
    return objectEnumerator().toList()
}

fun <T> NSSet?.toList(): kotlin.collections.List<T> {
    if (this == null) return listOf()
    return objectEnumerator().toList()
}

fun <T> NSEnumerator.toList(): List<T> {
    val items = mutableListOf<T>()
    var obj = nextObject()
    while (obj != null) {
        items += obj.uncheckedCast<T>()
        obj = nextObject()
    }
    return items
}

fun String.toNSString(): NSString {
    return interpretObjCPointer<NSString>(CreateNSStringFromKString(this))
}
