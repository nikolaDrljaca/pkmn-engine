package com.drbrosdev

import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*

fun main(args: Array<String>) {
    io.ktor.server.cio.EngineMain.main(args)
}

suspend fun Application.module() {
    install(ContentNegotiation) {
        json()
    }
    configureFrameworks()
    configureStatusPages()
    configureHTTP()
    configureRouting()
}