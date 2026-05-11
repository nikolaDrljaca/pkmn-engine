package com.drbrosdev.parser

import com.drbrosdev.battle.BattleSession
import com.drbrosdev.battle.TeamId

sealed interface Command {

    // $ create team-id team-id
    data class CreateBattle(
        val team1: TeamId,
        val team2: TeamId
    ) : Command

    // $ turn battle-1 team-1 move flamethrower ; team-2 switch garchomp
    data class ResolveTurn(
        val battleId: String,
        val action1: Pair<TeamId, TurnSelection>,
        val action2: Pair<TeamId, TurnSelection>,
    ) : Command

    // $ {battleId} {teamId} show team
    data class ShowTeam(val session: BattleSession) : Command

    // $ {battleId} {teamId} show team op
    data class ShowOpponentTeam(val session: BattleSession) : Command

    // $ {battleId} {teamId} show battle
    data class ShowBattle(val session: BattleSession) : Command

    // $ {battleId} {teamId} show active
    data class ShowActive(val session: BattleSession) : Command

    // $ {battleId} {teamId} show active op
    data class ShowOpponentActive(val session: BattleSession) : Command

    // $ {battleId} {teamId} show move {pkmnId}
    data class ShowMove(val session: BattleSession, val pokemonId: String) : Command

    data class Unknown(val command: String) : Command
}