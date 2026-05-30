package com.drbrosdev

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.di.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.util.logging.Logger
import kotlinx.serialization.Serializable

@Serializable
data class CreateTeamResponse(
    val teamId: String
)

@Serializable
data class CreateBattleRequest(
    val teamId1: String,
    val teamId2: String
)

@Serializable
data class CreateBattleResponse(
    val battleId: String
)


fun Routing.battleRoutes(
    logger: Logger,
    engine: BattleEngine
) {
    post("/team") {
        val content = call.receiveText()
        logger.info("POST /team called to create a team.")
        val teamId = engine.execute(
            """
                team create
                $content
            """.trimIndent()
        )
        call.respond(status = HttpStatusCode.Created, message = CreateTeamResponse(teamId))
    }

    post("/battle") {
        val request = call.receive<CreateBattleRequest>()
        logger.info("POST /battle called with $request.")
        val battleId = engine.execute(
            """
                create ${request.teamId1} ${request.teamId2}
            """.trimIndent()
        )
        call.respond(status = HttpStatusCode.Created, message = CreateBattleResponse(battleId))
    }
}

fun Application.configureRouting() {
    install(WebSockets)

    val engine: BattleEngine by dependencies

    routing {
        get("/") {
            call.respondText("PKMN Battle Engine Service")
        }
        get("/health") {
            call.respond(HttpStatusCode.OK)
        }

        // create team with PKMN showdown text paste
        battleRoutes(logger = log, engine = engine)

        gameWebsocket(log = log, engine = engine)
    }
}
