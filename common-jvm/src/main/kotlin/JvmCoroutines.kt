package org.jetbrains.kotlinconf.util

import kotlinx.coroutines.experimental.*


actual class KonfPromise<T>(private val result: Deferred<T>) {
    var onError: (cause: Throwable) -> Unit = {}

    actual fun then(block: (T) -> Unit): KonfPromise<T> {
        result.invokeOnCompletion {
            it?.let { onError(it) } ?: block(result.getCompleted())
        }

        return this
    }

    actual fun catch(block: (cause: Throwable) -> Unit): Unit {
        onError = block
    }
}

actual fun <T> konfAsync(block: suspend () -> T): KonfPromise<T> = async { block() }.let { KonfPromise(it) }