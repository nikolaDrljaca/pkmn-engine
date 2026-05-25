package com.drbrosdev

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.MissingRequestParameterException
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respond
import kotlinx.serialization.Serializable


fun Application.configureStatusPages() {
    install(StatusPages) {
        exception<IllegalArgumentException> { call, cause ->
            val problem = Problem(
                title = cause.message ?: "",
                status = HttpStatusCode.BadRequest.value
            )
            call.respond(status = HttpStatusCode.BadRequest, message = problem)
        }

        exception<IllegalStateException> { call, cause ->
            val problem = Problem(
                title = cause.message ?: "",
                status = HttpStatusCode.InternalServerError.value
            )
            call.respond(status = HttpStatusCode.InternalServerError, message = problem)
        }

        exception<MissingRequestParameterException> { call, cause ->
            val problem = Problem(
                title = cause.message ?: "",
                status = HttpStatusCode.BadRequest.value,
                details = "Missing path parameter ${cause.parameterName}! Expected format: /battle/:battleId/:teamId"
            )
            call.respond(status = HttpStatusCode.BadRequest, message = problem)
        }

        exception<RuntimeException> { call, cause ->
            val problem = Problem(
                title = cause.message ?: "",
                status = HttpStatusCode.InternalServerError.value,
            )
            call.respond(status = HttpStatusCode.InternalServerError, message = problem)
        }
    }
}

// Content-Type application/problem+json
@Serializable
data class Problem(
    val type: String = "about:blank",
    val instance: String = "about:blank",
    val title: String, // exception message
    val status: Int,
    val details: String? = null
)
