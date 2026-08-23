package com.drbrosdev.battle.turn

import com.drbrosdev.battle.move.Move
import com.drbrosdev.battle.move.MoveStatus
import com.drbrosdev.battle.move.isStatusMove
import com.drbrosdev.battle.pokemon.Pokemon
import com.drbrosdev.battle.pokemon.VolatileStatus
import com.drbrosdev.battle.pokemon.hasFainted

sealed class TurnAction {
    abstract val activePokemon: Pokemon

    class MoveSelected private constructor(
        val move: Move,
        override val activePokemon: Pokemon
    ) : TurnAction() {
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

            // NOTE: Only used in tests
            fun of(move: Move, active: Pokemon) =
                MoveSelected(move, active)
        }
    }

    class Switch private constructor(
        val incomingPokemon: Pokemon,
        override val activePokemon: Pokemon
    ) : TurnAction() {
        init {
            require(activePokemon.id != incomingPokemon.id) {
                "Cannot switch into the same pokemon!"
            }
        }

        companion object {
            operator fun invoke(incoming: Pokemon, active: Pokemon) = runCatching {
                Switch(incoming, active)
            }

            // NOTE: Only used in tests
            fun of(incoming: Pokemon, active: Pokemon) =
                Switch(incoming, active)
        }
    }

    companion object {
        operator fun invoke(move: Move, active: Pokemon) = MoveSelected(move, active)

        operator fun invoke(incoming: Pokemon, active: Pokemon) = Switch(incoming, active)
    }
}
