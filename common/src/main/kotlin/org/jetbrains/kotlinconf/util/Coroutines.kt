package org.jetbrains.kotlinconf.util

import kotlin.coroutines.experimental.*

expect class KonfPromise<T> {
    fun then(block: (T) -> Unit): KonfPromise<T>
    fun catch(block: (cause: Throwable) -> Unit): Unit
}

expect fun <T> konfAsync(block: suspend () -> T): KonfPromise<T>

suspend fun <T> KonfPromise<T>.get(): T = suspendCoroutine<T> { continuation ->
    then {
        continuation.resume(it)
    }.catch {
        continuation.resumeWithException(it)
    }
}
