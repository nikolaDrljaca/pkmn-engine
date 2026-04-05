package com.drbrosdev.battle.turn

import com.drbrosdev.battle.move.MoveContext
import com.drbrosdev.battle.move.MoveId
import com.drbrosdev.battle.pokemon.Pokemon
import com.drbrosdev.battle.pokemon.PokemonId

data class Turn(
    val selection1: Pair<Pokemon, TurnAction>,
    val selection2: Pair<Pokemon, TurnAction>
)

sealed interface TurnAction {

    data class MoveSelected(val move: MoveId): TurnAction

    data class Switch(val incoming: PokemonId): TurnAction

}

data class ActionContext(
    val user: Pokemon,
    val target: Pokemon,
    val action: TurnAction
)

fun ActionContext.toMoveContext(): MoveContext = when (action) {
    is TurnAction.MoveSelected -> MoveContext(
        userId = user.id,
        targetId = target.id,
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

fun TurnValidity.valid(): Boolean = this is TurnValidity.Valid

