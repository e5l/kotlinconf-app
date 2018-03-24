package org.jetbrains.kotlinconf.util

expect class KonfPromise<T> {
    fun then(block: T.() -> Unit): KonfPromise<T>
    fun catch(block: (cause: Throwable) -> Unit): Unit
}

expect fun <T> konfAsync(block: suspend () -> T): KonfPromise<T>
