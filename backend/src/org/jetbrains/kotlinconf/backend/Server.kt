package org.jetbrains.kotlinconf.backend

import io.ktor.server.engine.*
import io.ktor.server.netty.*
import kotlinx.coroutines.experimental.*
import org.jetbrains.kotlinconf.api.*

fun main(args: Array<String>) {
//    runBlocking {
//        val response = KotlinConfApi.createUser("ololo")
//        println(response)
//    }
    val server = embeddedServer(Netty, commandLineEnvironment(args))
    server.start()
}
