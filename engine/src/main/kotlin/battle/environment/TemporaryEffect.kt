package com.drbrosdev.battle.environment

import com.drbrosdev.battle.pokemon.stats.StatKey
import com.drbrosdev.battle.pokemon.stats.StatModification
import com.drbrosdev.battle.pokemon.stats.StatModifier
import com.drbrosdev.battle.pokemon.stats.StatModifiers


sealed interface TemporaryEffect {
    val expiresOnTurn: Int

    data class Tailwind(override val expiresOnTurn: Int) : TemporaryEffect {
        val statModification = StatModification {
            StatModifiers(mapOf(StatKey.SPEED to StatModifier.Percent(200)))
        }

        companion object {
            const val DURATION = 4

            // tailwind lasts 4 turns always
            fun create(currentTurn: Int) = Tailwind(currentTurn + DURATION)
        }
    }

    data class Reflect(override val expiresOnTurn: Int) : TemporaryEffect

    data class LightScreen(override val expiresOnTurn: Int) : TemporaryEffect
}