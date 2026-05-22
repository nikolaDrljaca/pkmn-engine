package com.drbrosdev.parser.command

import com.drbrosdev.battle.BattleSession
import com.drbrosdev.battle.TeamId
import com.drbrosdev.parser.team.TextTeamParser

object TextCommandParser : CommandParser {
    override fun parse(command: String): Command {
        // handle action commands: create / turn
        if (command.isBlank()) {
            return Command.Unknown(command)
        }
        val tokens = command.trim().split(" ")
        val firstToken = tokens.first()

        return when {
            firstToken == "team" -> parseTeamAction(command)
            firstToken == "create" -> parseAction(command)
            firstToken == "turn" -> parseAction(command)
            tokens.contains("show") -> parseShowCommand(command)
            else -> Command.Unknown(command)
        }
    }

    private fun parseTeamAction(input: String): Command {
        val teamContent = input.removePrefix("team create")
        val team = TextTeamParser.parse(teamContent)
        return Command.CreateTeam(team)
    }

    private fun parseShowCommand(input: String): Command {
        val tokens = input.trim().split(" ")
        if (tokens.size < 3) return Command.Unknown(input)

        val battleId = tokens[0]
        val teamId = TeamId(tokens[1])
        if (tokens[2] != "show") return Command.Unknown(input)

        val session = BattleSession(battleId, teamId.id)
        val rest = tokens.drop(3).joinToString(" ")

        return when {
            rest == "team op" -> Command.ShowOpponentTeam(session)
            rest == "active op" -> Command.ShowOpponentActive(session)
            rest == "team" -> Command.ShowTeam(session)
            rest == "battle" -> Command.ShowBattle(session)
            rest == "active" -> Command.ShowActive(session)
            rest.startsWith("move") -> Command.ShowMove(session, tokens.last())
            else -> Command.Unknown(input)
        }
    }

    private fun parseAction(command: String): Command {
        val tokens = command.trim().split(" ")
        return when {
            tokens[0] == "create" -> {
                if (tokens.size == 3) {
                    Command.CreateBattle(TeamId.Companion(tokens[1]), TeamId.Companion(tokens[2]))
                } else
                    Command.Unknown(command)
            }

            tokens[0] == "turn" -> {
                val parts = command.split(" ")
                    .drop(2)
                    .joinToString(separator = " ") { it }
                    .split(";")
                    .map { it.trim() }
                Command.ResolveTurn(
                    battleId = tokens[1],
                    action1 = parseTurnCommand(parts[0]),
                    action2 = parseTurnCommand(parts[1])
                )
            }

            else -> Command.Unknown(command)
        }
    }

    private fun parseTurnCommand(input: String): Pair<TeamId, TurnSelection> {
        val tokens = input.split(" ")
        // team-1 move flamethrower
        // OR
        // team-2 switch garchomp
        require(tokens.size == 3)
        val teamId = TeamId.Companion(tokens[0])
        return teamId to when (tokens[1]) {
            "move" -> TurnSelection.MoveSelected(tokens[2])
            "switch" -> TurnSelection.Switch(tokens[2])
            else -> error("Unknown turn command: ${tokens[1]}")
        }
    }
}