package com.drbrosdev.battle.turn

import com.drbrosdev.battle.Battle
import com.drbrosdev.battle.abilities
import com.drbrosdev.battle.move.MoveStatus
import com.drbrosdev.battle.move.isStatusMove
import com.drbrosdev.battle.pokemon.Pokemon
import com.drbrosdev.battle.pokemon.VolatileStatus
import java.util.logging.Logger

private val LOG = Logger.getLogger(TurnValidator::class.java.simpleName)

fun interface TurnValidator {
    fun Turn.validate(battle: Battle): TurnValidity
}

val SameSwitchTarget = TurnValidator { battle ->
    // TODO: Impl
    // if switching, pokemon cannot switch into itself
    TurnValidity.Valid
}

val PowerPointsTurnValidator = TurnValidator { battle ->
    // verify all chosen moves have PP > 0
    val verify: (Pair<Pokemon, TurnAction>) -> TurnValidity = { (pokemon, action) ->
        when (action) {
            is TurnAction.MoveSelected -> {
                val move = battle[pokemon.id][action.move]
                when {
                    move.powerPoints > 0 -> TurnValidity.Valid
                    else -> TurnValidity.Invalid(TurnValidity.InvalidReason.NoPowerPoints)
                }
            }

            is TurnAction.Switch -> TurnValidity.Valid
        }
    }
    assertValidity(
        verify(selection1),
        verify(selection2)
    )
}

val MoveDisabledTurnValidator = TurnValidator { battle ->
    // verify chosen moves are not disabled
    val verify: (Pair<Pokemon, TurnAction>) -> TurnValidity = { (pokemon, action) ->
        when (action) {
            is TurnAction.MoveSelected -> {
                val move = battle[pokemon.id][action.move]
                when {
                    move.status == MoveStatus.DISABLED -> TurnValidity.Invalid(TurnValidity.InvalidReason.MoveDisabled)
                    else -> TurnValidity.Valid
                }
            }

            is TurnAction.Switch -> TurnValidity.Valid
        }
    }
    assertValidity(
        verify(selection1),
        verify(selection2)
    )
}

val TauntTurnValidator = TurnValidator { battle ->
    // status moves are not allowed if pokemon is taunted
    val (pokemon1, action1) = selection1
    val (pokemon2, action2) = selection2

    fun verifyTaunt(selection: Pair<Pokemon, TurnAction>): TurnValidity {
        val (pokemon, action) = selection
        val isTaunted = pokemon.volatileStatus
            .filterIsInstance<VolatileStatus.Taunt>()
            .firstOrNull() != null
        return when {
            isTaunted -> {
                when (action) {
                    is TurnAction.MoveSelected -> {
                        val move = battle[pokemon.id][action.move]
                        when {
                            move.isStatusMove() ->
                                TurnValidity.Invalid(TurnValidity.InvalidReason.PokemonTaunted)

                            else -> TurnValidity.Valid
                        }
                    }

                    else -> TurnValidity.Valid
                }
            }

            else -> TurnValidity.Valid
        }
    }

    assertValidity(
        verifyTaunt(selection1),
        verifyTaunt(selection2),
    )
}

private fun assertValidity(a1Valid: TurnValidity, a2Valid: TurnValidity): TurnValidity = when {
    a1Valid is TurnValidity.Invalid -> {
        LOG.info { "Selection 1 is invalid due to ${a1Valid.reason}" }
        a1Valid
    }

    a2Valid is TurnValidity.Invalid -> {
        LOG.info { "Selection 2 is invalid due to ${a2Valid.reason}" }
        a2Valid
    }

    else -> TurnValidity.Valid
}

private val StandardTurnValidators = sequenceOf(
    SameSwitchTarget,
    PowerPointsTurnValidator,
    MoveDisabledTurnValidator,
    TauntTurnValidator,
)

/*
Chain of Responsibility pattern.
Each TurnValidator decides to either handle (return) or pass along.
The first Invalid result ends the chain.
 */
fun Battle.validateTurn(
    turn: Turn,
): TurnValidity {
    val battle = this
    val applicableValidators = abilities().flatMap { it.turnValidators }
        .plus(StandardTurnValidators)
    val result = applicableValidators
        .map { with(it) { turn.validate(battle) } }
        .firstOrNull { it is TurnValidity.Invalid }
        ?: TurnValidity.Valid
    when (result) {
        is TurnValidity.Invalid -> LOG.info { "Turn ${battle.turnCount} invalid due to ${result.reason}" }
        is TurnValidity.Valid -> Unit
    }
    return result
}
