package com.drbrosdev.battle.turn

import com.drbrosdev.battle.Battle
import com.drbrosdev.battle.TeamId
import com.drbrosdev.battle.move.MoveContext
import com.drbrosdev.battle.move.MoveId
import com.drbrosdev.battle.pokemon.PokemonId

data class Turn(
    val selection1: Pair<PokemonId, TurnAction>,
    val selection2: Pair<PokemonId, TurnAction>
)

sealed interface TurnAction {

    data class MoveSelected(val move: MoveId): TurnAction

    data class Switch(val incoming: PokemonId): TurnAction

}

data class ActionContext(
    val user: TeamId,
    val target: TeamId,
    val action: TurnAction
)

fun ActionContext.toMoveContext(battle: Battle): MoveContext = when (action) {
    is TurnAction.MoveSelected -> MoveContext(
        userId = battle[user].id,
        targetId = battle[target].id,
        moveId = action.move
    )
    else ->  error("Cannot create MoveContext when action is ${action.javaClass.simpleName}!")
}

sealed interface TurnValidity {
    data object Valid : TurnValidity

    data class Invalid(val reason: InvalidReason) : TurnValidity

    /*
    Error messages can be introduced here
     */
    sealed interface InvalidReason {
        data object NoPowerPoints : InvalidReason
        data object MoveDisabled : InvalidReason
        data object PokemonTaunted : InvalidReason
    }
}
