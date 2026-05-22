package com.drbrosdev

import io.ktor.server.application.*

fun main(args: Array<String>) {
    io.ktor.server.cio.EngineMain.main(args)
}

fun Application.module() {
    configureFrameworks()
    configureSerialization()
    configureDatabases()
    configureHTTP()
    configureRouting()
}
