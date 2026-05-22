package com.drbrosdev

import com.drbrosdev.battle.*
import com.drbrosdev.battle.move.MoveId
import com.drbrosdev.battle.pokemon.PokemonId
import com.drbrosdev.battle.presentation.TextBattlePresenter
import com.drbrosdev.battle.presentation.TextMovePresenter
import com.drbrosdev.battle.presentation.TextPokemonPresenter
import com.drbrosdev.battle.presentation.TextTeamPresenter
import com.drbrosdev.battle.turn.Turn
import com.drbrosdev.battle.turn.TurnAction
import com.drbrosdev.battle.turn.resolveTurn
import com.drbrosdev.parser.command.Command
import com.drbrosdev.parser.command.TextCommandParser
import com.drbrosdev.parser.command.TurnSelection


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
            val battleId = RandomGen.nextInt(from = 100, until = 200)
                .let { BattleId(it.toString()) }
            val team1 = Team(emptyMap())
            val team2 = Team(emptyMap())
            val battle = Battle(
                team1 = team1,
                team2 =  team2,
                active1 = team1.members.values.first().id,
                active2 = team2.members.values.first().id
            )
            sessions[battleId] = battle
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

            // determine the actual pokemonId based of the slug prefix!
            val mapper : (Pair<TeamId, TurnSelection>) -> Pair<PokemonId, TurnAction> = { (teamId, selection) ->
                val activeMon = battle[teamId].id
                when (selection) {
                    is TurnSelection.MoveSelected -> activeMon to TurnAction.MoveSelected(MoveId(selection.move))
                    is TurnSelection.Switch -> {
                        val resolved = battle.team(teamId).findMember(selection.incoming)
                        activeMon to TurnAction.Switch(resolved.id)
                    }
                }
            }

            val turn = Turn(
                selection1 = command.action1.let { mapper(it) },
                selection2 = command.action2.let { mapper(it) }
            )

            val afterTurn = battle.resolveTurn(turn)
            when (afterTurn.state) {
                BattleState.InProgress -> sessions[battleId] = afterTurn
                is BattleState.Concluded -> sessions.remove(battleId)
            }
            afterTurn.turnLog
                .joinToString(separator = "\n") { it }
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
            val (battleId, teamId) = command.session
            val battle = sessions.getValue(BattleId(battleId))
            val resolved = battle.team(TeamId(teamId)).findMember(command.pokemonId)
            val pokemon = battle[resolved.id]
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
