package com.drbrosdev

import io.ktor.server.plugins.MissingRequestParameterException
import io.ktor.server.routing.Routing
import io.ktor.server.websocket.DefaultWebSocketServerSession
import io.ktor.server.websocket.webSocket
import io.ktor.util.logging.Logger
import io.ktor.websocket.CloseReason
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import io.ktor.websocket.readText
import io.ktor.websocket.send
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.cancellation.CancellationException

class BattleSession(
    val battleId: String,
    val teamConnections: ConcurrentHashMap<String, TeamConnection>
) {
    fun verifyMaxTeams(): Boolean {
        return teamConnections.keys.size >= 2
    }

    fun allActionsPresent(): Boolean {
        return teamConnections.values.all { it.action != null }
    }
}

data class TeamConnection(
    val teamId: String,
    val session: DefaultWebSocketServerSession,
    val action: String? = null
) {
    val command: String? = when {
        action != null -> "$teamId $action"
        else -> null
    }
}

suspend fun DefaultWebSocketServerSession.sendLine(value: String) = send(value + "\n")

fun Routing.gameWebsocket(
    log: Logger,
    engine: BattleEngine
) {
    val battleSessions = ConcurrentHashMap<String, BattleSession>()

    webSocket(path = "/battle/{battleId}/{teamId}") {
        val battleId = call.parameters["battleId"] ?: throw MissingRequestParameterException("battleId")
        val teamId = call.parameters["teamId"] ?: throw MissingRequestParameterException("teamId")

        val currentSession = battleSessions.getOrPut(battleId) {
            BattleSession(battleId, ConcurrentHashMap())
        }

        if (currentSession.verifyMaxTeams()) {
            close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "Max teams in battle."))
            return@webSocket
        }
        sendLine("Connected to $battleId with $teamId.")

        // either store connection or get it
        val currentTeam = currentSession.teamConnections.getOrPut(teamId) {
            TeamConnection(teamId, this)
        }

        try {
            for (frame in incoming) {
                frame as? Frame.Text ?: continue
                val receivedText = frame.readText().trim()

                when {
                    receivedText.startsWith("move") || receivedText.startsWith("switch") -> {
                        // store the action of the current team
                        currentSession.teamConnections[teamId] = currentTeam.copy(action = receivedText)
                        // check if both actions are present
                        if (currentSession.allActionsPresent().not()) {
                            sendLine("Waiting for opponent to submit an action.")
                            continue
                        }
                        // if all present construct engine command and execute
                        val command = buildString {
                            append("turn $battleId ")
                            append(currentSession.teamConnections[teamId]?.command)
                            val secondCommand = currentSession.teamConnections
                                .keys
                                .filterNot { it == teamId }
                                .map { currentSession.teamConnections[it]?.command }
                                .firstOrNull()
                            append(" ; ")
                            append(secondCommand)
                        }
                        try {
                            // execute command
                            val result = engine.execute(command)
                            // send result to both team connections
                            currentSession.teamConnections.values
                                .forEach { it.session.sendLine(result) }
                            // clear stored actions
                            currentSession.teamConnections.keys.forEach {
                                currentSession.teamConnections.computeIfPresent(it) { _, value ->
                                    value.copy(action = null)
                                }
                            }
                        } catch (t: Throwable) {
                            log.error("Error during TURN:", t)
                            sendLine("Error: ${t.message}")
                        }
                    }

                    receivedText.startsWith("show") -> {
                        val command = "$battleId $teamId $receivedText"
                        try {
                            val result = engine.execute(command)
                            sendLine(result)
                        } catch (t: Throwable) {
                            log.error("Error during SHOW:", t)
                            sendLine("Error: ${t.message}")
                        }
                    }

                    else -> sendLine("Unknown command: $receivedText")
                }
            }
        } catch (t: Throwable) {
            if (t is CancellationException) {
                throw t
            }
            log.error("WebSocket error for battle $battleId, team $teamId", t)
            sendLine("Error: ${t.message}")
            // session cleanup can go here
        }
    }
}
