package com.drbrosdev

import io.ktor.server.application.*
import io.ktor.server.plugins.di.*

fun Application.configureFrameworks() {
    dependencies {
        provide {
            BattleEngine()
        }
    }
}
