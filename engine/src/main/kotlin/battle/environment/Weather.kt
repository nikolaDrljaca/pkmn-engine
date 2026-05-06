package com.drbrosdev.battle.environment

import com.drbrosdev.battle.pokemon.Element
import com.drbrosdev.battle.pokemon.hasAnyOf
import com.drbrosdev.battle.pokemon.stats.StatKey
import com.drbrosdev.battle.pokemon.stats.StatModification
import com.drbrosdev.battle.pokemon.stats.StatModifier
import com.drbrosdev.battle.pokemon.stats.StatModifiers
import com.drbrosdev.battle.turn.EndOfTurnEffect
import com.drbrosdev.battle.turn.HailEndOfTurnEffect
import com.drbrosdev.battle.turn.SandstormEndOfTurnEffect


sealed interface Weather {
    // NOTE: null allows permanent weather
    val expiresOnTurn: Int?

    val endOfTurnEffect: EndOfTurnEffect
    val statModification: StatModification

    data object None : Weather {
        override val expiresOnTurn: Int?
            get() = null

        override val endOfTurnEffect: EndOfTurnEffect
            get() = EndOfTurnEffect.NoEffect

        override val statModification: StatModification
            get() = StatModification.NoModification
    }

    data class HarshSun(override val expiresOnTurn: Int?) : Weather {
        override val endOfTurnEffect: EndOfTurnEffect
            get() = EndOfTurnEffect.NoEffect

        override val statModification: StatModification
            get() = StatModification.NoModification
    }

    data class Rain(override val expiresOnTurn: Int) : Weather {
        override val endOfTurnEffect: EndOfTurnEffect
            get() = EndOfTurnEffect.NoEffect

        override val statModification: StatModification
            get() = StatModification.NoModification
    }

    data class Hail(override val expiresOnTurn: Int) : Weather {
        override val endOfTurnEffect: EndOfTurnEffect
            get() = HailEndOfTurnEffect

        override val statModification: StatModification
            get() = StatModification.NoModification
    }

    data class Sandstorm(override val expiresOnTurn: Int) : Weather {
        override val endOfTurnEffect: EndOfTurnEffect
            get() = SandstormEndOfTurnEffect

        override val statModification: StatModification
            get() = { context ->
                when {
                    context.pokemon.elements.hasAnyOf(Element.ROCK) -> StatModifiers(
                        modifiers = mapOf(StatKey.SPECIAL_DEFENCE to StatModifier.Percent(150))
                    )

                    else -> StatModifiers()
                }
            }
    }

    companion object {
        fun computeExpiry(currentTurn: Int, shouldExtend: Boolean = false) = when {
            shouldExtend -> currentTurn + 8
            else -> currentTurn + 5
        }

        fun harshSun(currentTurn: Int, shouldExtend: Boolean = false) =
            HarshSun(computeExpiry(currentTurn, shouldExtend))

        fun rain(currentTurn: Int, shouldExtend: Boolean = false) =
            Rain(computeExpiry(currentTurn, shouldExtend))

        fun hail(currentTurn: Int, shouldExtend: Boolean = false) =
            Hail(computeExpiry(currentTurn, shouldExtend))

        fun sandstorm(currentTurn: Int, shouldExtend: Boolean = false) =
            Sandstorm(computeExpiry(currentTurn, shouldExtend))
    }
}

val Weather.enterNarrativeMessage: String
    get() = when (this) {
        Weather.None -> ""
        is Weather.HarshSun -> "The sunlight turned harsh!"
        is Weather.Rain -> "It started to rain!"
        is Weather.Hail -> "It started to hail!"
        is Weather.Sandstorm -> "A sandstorm kicked up!"
    }

val Weather.exitNarrativeMessage: String
    get() = when (this) {
        Weather.None -> ""
        is Weather.HarshSun -> "The sunlight faded."
        is Weather.Rain -> "The rain stopped."
        is Weather.Hail -> "The hail stopped."
        is Weather.Sandstorm -> "The sandstorm subsided."
    }
