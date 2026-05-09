package com.drbrosdev

import com.drbrosdev.battle.Battle
import com.drbrosdev.battle.BattleId
import com.drbrosdev.battle.BattleState
import com.drbrosdev.battle.TeamId
import com.drbrosdev.battle.pokemon.PokemonId
import com.drbrosdev.battle.presentation.TextBattlePresenter
import com.drbrosdev.battle.presentation.TextMovePresenter
import com.drbrosdev.battle.presentation.TextPokemonPresenter
import com.drbrosdev.battle.presentation.TextTeamPresenter
import com.drbrosdev.battle.turn.Turn
import com.drbrosdev.battle.turn.resolveTurn
import com.drbrosdev.parser.Command
import com.drbrosdev.parser.TextCommandParser
import java.util.UUID


class BattleEngine {
    private val sessions = mutableMapOf<BattleId, Battle>()

    private val commandParser = TextCommandParser

    fun execute(command: String): String {
        val parsed = commandParser.parse(command)
        return resolveCommand(parsed)
    }

    private fun resolveCommand(command: Command): String = when (command) {
        is Command.CreateBattle -> {
            // TODO: @drljacan battleId generator
            val battleId = UUID.randomUUID().toString().take(8)
                .let { BattleId(it) }
            // using teamId resolve to team
            /*
            val battle = Battle(
                team1 = TODO(),
                team2 = TODO(),
                active1 = TODO(),
                active2 = TODO()
            )
            // manage in sessions
            sessions[battleId] = battle
             */
            battleId.id
        }

        is Command.ResolveTurn -> {
            val battleId = BattleId(command.battleId)
            val battle = sessions.getValue(battleId)
            val turn = Turn(
                selection1 = command.action1.let { (teamId, action) ->
                    battle[teamId].id to action
                },
                selection2 = command.action2.let { (teamId, action) ->
                    battle[teamId].id to action
                }
            )
            val afterTurn = battle.resolveTurn(turn)
            when (afterTurn.state) {
                BattleState.InProgress -> sessions[battleId] = afterTurn
                is BattleState.Concluded -> sessions.remove(battleId)
            }
            afterTurn.turnLog
                .joinToString { it }
        }

        is Command.ShowActive -> {
            val (battleId, teamId) = command.session
            val battle = sessions.getValue(BattleId(battleId))
            val active = battle[TeamId(teamId)]
            TextPokemonPresenter.present(active.id, battle)
        }

        is Command.ShowBattle -> {
            val (battleId, _) = command.session
            val battle = sessions.getValue(BattleId(battleId))
            TextBattlePresenter.present(battle)
        }

        is Command.ShowMove -> {
            val (battleId, _) = command.session
            val battle = sessions.getValue(BattleId(battleId))
            val pokemon = battle[PokemonId(command.pokemonId)]
            TextMovePresenter.present(pokemon)
        }

        is Command.ShowOpponentActive -> {
            val (battleId, teamId) = command.session
            val battle = sessions.getValue(BattleId(battleId))
            val active = battle[TeamId(teamId)]
            val opponent = when {
                battle.active1 == active.id -> battle.active2
                battle.active2 == active.id -> battle.active1
                else -> error("")
            }
            TextPokemonPresenter.present(
                pokemonId = opponent,
                battle = battle,
                censor = true
            )
        }

        is Command.ShowTeam -> {
            val (battleId, teamId) = command.session
            val battle = sessions.getValue(BattleId(battleId))
            TextTeamPresenter.present(battle, TeamId(teamId))
        }

        is Command.ShowOpponentTeam -> {
            val (battleId, teamId) = command.session
            val battle = sessions.getValue(BattleId(battleId))
            val team = when {
                battle.team1.id.id == teamId -> battle.team2
                battle.team2.id.id == teamId -> battle.team1
                else -> error("")
            }
            TextTeamPresenter.present(battle, team.id, true)
        }

        is Command.Unknown -> "Cannot execute command: ${command.command}"
    }

}
