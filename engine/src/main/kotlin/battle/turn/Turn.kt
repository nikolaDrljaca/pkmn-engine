package com.drbrosdev.battle.turn

import com.drbrosdev.battle.Battle
import com.drbrosdev.battle.TeamId
import com.drbrosdev.battle.move.Move
import com.drbrosdev.battle.move.MoveContext
import com.drbrosdev.battle.move.MoveId
import com.drbrosdev.battle.move.MoveStatus
import com.drbrosdev.battle.move.isStatusMove
import com.drbrosdev.battle.pokemon.Pokemon
import com.drbrosdev.battle.pokemon.PokemonId
import com.drbrosdev.battle.pokemon.VolatileStatus
import com.drbrosdev.battle.pokemon.hasFainted

/*
data class Turn(
    val selection1: Pair<PokemonId, TurnAction>,
    val selection2: Pair<PokemonId, TurnAction>
)

sealed interface TurnAction {

    data class MoveSelected(val move: MoveId): TurnAction

    data class Switch(val incoming: PokemonId): TurnAction

}
 */

data class ActionContext(
    val user: TeamId,
    val target: TeamId,
    val action: TurnAction
)

fun ActionContext.toMoveContext(battle: Battle): MoveContext = when (action) {
    is TurnAction.MoveSelected -> MoveContext(
        userId = battle[user].id,
        targetId = battle[target].id,
        moveId = action.move.id
    )
    else ->  error("Cannot create MoveContext when action is ${action.javaClass.simpleName}!")
}

/*
A turn is a list of turn actions which need to be executed in a battle.
This enables support for 1v1 and 2v2 battles.
 */
data class Turn(
    val actions: List<TurnAction>
)

sealed class TurnAction {
    abstract val activePokemon: Pokemon

    class MoveSelected private constructor(
        val move: Move,
        override val activePokemon: Pokemon
    ): TurnAction() {
        init {
            // must not be fainted
            require(activePokemon.hasFainted().not()) {
                "${activePokemon.name} has fainted!"
            }
            require(move.powerPoints > 0) {
                "${move.name} has no power points left!"
            }
            require(move.status != MoveStatus.DISABLED) {
                "${move.name} is disabled!"
            }
            val isTaunted = activePokemon.volatileStatus
                .filterIsInstance<VolatileStatus.Taunt>()
                .firstOrNull() != null
            // status moves cannot be used when taunted
            require((isTaunted && move.isStatusMove()).not()) {
                "${move.name} cannot be used while taunted!"
            }
        }

        companion object {
            operator fun invoke(move: Move, active: Pokemon) = runCatching {
                MoveSelected(move, active)
            }
        }
    }

    class Switch private constructor(
        val incomingPokemon: Pokemon,
        override val activePokemon: Pokemon
    ): TurnAction() {
        init {
            require(activePokemon.id != incomingPokemon.id) {
                "Cannot switch into the same pokemon!"
            }
        }

        companion object {
            operator fun invoke(incoming: Pokemon, active: Pokemon) = runCatching {
                Switch(incoming, active)
            }
        }
    }

    companion object {
        operator fun invoke(move: Move, active: Pokemon) = MoveSelected(move, active)

        operator fun invoke(incoming: Pokemon, active: Pokemon) = Switch(incoming, active)
    }
}

