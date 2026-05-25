package com.drbrosdev

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.plugins.di.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.util.logging.*
import io.ktor.websocket.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.Serializable
import java.util.concurrent.ConcurrentHashMap

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


fun Routing.gameWebsocket(
    log: io.ktor.util.logging.Logger,
    engine: BattleEngine
) {
// Shared state: battleId -> (teamId -> pending action)
    val battleSessions = ConcurrentHashMap<String, ConcurrentHashMap<String, String>>()
// battleId -> (teamId -> WebSocket session)
    val battleConnections = ConcurrentHashMap<String, ConcurrentHashMap<String, DefaultWebSocketServerSession>>()
    val battleSessionsMutex = ConcurrentHashMap<String, Mutex>()

    webSocket(path = "/battle/{battleId}/{teamId}") {
        val battleId = call.parameters["battleId"] ?: throw MissingRequestParameterException("battleId")
        val teamId = call.parameters["teamId"] ?: throw MissingRequestParameterException("teamId")

        // Register connection, reject if battle already has 2 teams
        val connections = battleConnections.getOrPut(battleId) { ConcurrentHashMap() }
        val mutex = battleSessionsMutex.getOrPut(battleId) { Mutex() }
        val actions = battleSessions.getOrPut(battleId) { ConcurrentHashMap() }

        if (connections.size >= 2 && connections.containsKey(teamId)) {
            send("Battle $battleId is already full.")
            close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "Battle session full"))
            return@webSocket
        }
        connections[teamId] = this
        send("Connected to battle $battleId as team $teamId. Waiting for opponent...")

        if (connections.size == 2) {
            connections.values.forEach { it.send("Both teams connected. Battle $battleId begins!") }
        }

        try {
            for (frame in incoming) {
                frame as? Frame.Text ?: continue
                val receivedText = frame.readText().trim()

                when {
                    // e.g. "move 3" or "switch 2"
                    receivedText.startsWith("move") || receivedText.startsWith("switch") -> {
                        if (connections.size < 2) {
                            send("Waiting for opponent to connect before actions can be submitted.")
                            continue
                        }

                        actions[teamId] = receivedText
                        send("Action registered: $receivedText. Waiting for opponent...")

                        // Check if both teams have submitted their action
                        mutex.withLock {
                            val teamIds = connections.keys.toList()

                            if (teamIds.size == 2 && teamIds.all { actions.containsKey(it) }) {
                                val (team1, team2) = teamIds
                                val action1 = actions[team1]!!
                                val action2 = actions[team2]!!

                                // Build the combined turn command
                                val turnCommand = "turn $battleId $team1 $action1 ; $team2 $action2"
                                val result = engine.execute(turnCommand)

                                // Broadcast result to both players
                                connections.values.forEach { session ->
                                    result.lines().forEach { line ->
                                        session.send(Frame.Text(line))
                                    }
                                }

                                // Clear actions for next turn
                                actions.clear()
                            }
                        }
                    }

                    receivedText.startsWith("show") -> {
                        val command = "$battleId $teamId $receivedText"
                        val result = engine.execute(command)
                        result.lines().forEach { line -> send(Frame.Text(line)) }
                    }

                    else -> send("Unknown command: $receivedText")
                }
            }
        } catch (ex: kotlinx.coroutines.CancellationException) {
            throw ex
        } catch (t: Throwable) {
            log.error(t)
        } finally {
            // Clean up on disconnect
            connections.remove(teamId)
            actions.remove(teamId)
            if (connections.isEmpty()) {
                battleSessions.remove(battleId)
                battleConnections.remove(battleId)
                battleSessionsMutex.remove(battleId)
            } else {
                connections.values.forEach { it.send("Opponent disconnected from battle $battleId.") }
            }
        }
    }
}

fun Application.configureRouting() {
    install(WebSockets)

    val engine: BattleEngine by dependencies

    routing {
        get("/") {
            call.respondText("PKMN Battle Engine Service")
        }

        // create team with PKMN showdown text paste
        post("/team") {
            val content = call.receiveText()
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
            val battleId = engine.execute(
                """
                create ${request.teamId1} ${request.teamId2}
            """.trimIndent()
            )
            call.respond(status = HttpStatusCode.Created, message = CreateBattleResponse(battleId))
        }

        gameWebsocket(log = log, engine = engine)
        /*
        /*
        TODO: @drljacan manage a session with two teamIds in one battle
        the sessionId is the battle Id and the websocket session can only hold 2 teams
        when creating a turn command the socket must wait for both players to input.
        command is:
        turn {battleId} {teamId-1} move {moveId} ; {teamId-2} switch {pokemonId}
         */
        webSocket(path = "/battle/{battleId}/{teamId}") {
            val battleId = call.parameters["battleId"] ?: throw MissingRequestParameterException("battleId")
            val teamId = call.parameters["teamId"] ?: throw MissingRequestParameterException("teamId")
            val engine = dependencies.resolve<BattleEngine>()

            send("Connected to $battleId with $teamId.")
            for (frame in incoming) {
                frame as? Frame.Text ?: continue
                val receivedText = frame.readText()
                val command = when {
                    receivedText.startsWith("turn") -> ""
                    receivedText.startsWith("show") -> "$battleId $teamId $receivedText"
                    else -> ""
                }
                val result = engine.execute(command)
                result.lines().forEach { line ->
                    send(Frame.Text(line))
                }
            }
        }
         */
    }
}
