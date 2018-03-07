package org.jetbrains.kotlinconf.util

import org.jetbrains.kotlinconf.api.*
import kotlin.coroutines.experimental.*

actual class KonfPromise<T>() {
    internal var handler: (T) -> Unit = {}
    internal var onError: (cause: Throwable) -> Unit = {}

    actual fun then(block: (T) -> Unit): KonfPromise<T> {
        handler = block
        return this
    }

    actual fun catch(block: (cause: Throwable) -> Unit): Unit {
        onError = block
    }
}

private class UIContext<T>(private val promise: KonfPromise<T>) : Continuation<T> {
    override val context: CoroutineContext get() = EmptyCoroutineContext

    override fun resume(value: T) {
        promise.handler(value)
    }

    override fun resumeWithException(exception: Throwable) {
        promise.onError(exception)
    }
}

actual fun <T> konfAsync(block: suspend () -> T): KonfPromise<T> =
        KonfPromise<T>().also { block.startCoroutine(UIContext(it)) }
