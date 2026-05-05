package com.drbrosdev.battle.turn

import com.drbrosdev.battle.pokemon.*
import com.drbrosdev.battle.pokemon.stats.Stat
import java.util.logging.Logger

fun interface EndOfTurnEffect {
    fun apply(pokemon: Pokemon): EndOfTurnEffectResult

    companion object {
        val NoEffect = EndOfTurnEffect { EndOfTurnEffectResult(it) }
    }
}

data class EndOfTurnEffectResult(
    val pokemon: Pokemon,
    val narrativeMessage: String = ""
)

// Extension is for scoping
fun EndOfTurnEffect.result(pokemon: Pokemon, message: () -> String = { "" }): EndOfTurnEffectResult =
    EndOfTurnEffectResult(pokemon, message())

val BurnEndOfTurnEffect = object : EndOfTurnEffect {
    override fun apply(pokemon: Pokemon): EndOfTurnEffectResult = when (pokemon.majorStatus) {
        is MajorStatus.Burned -> {
            val damage = (pokemon.effectiveStats.hp.value / 16).coerceAtLeast(1)
            val newHp = (pokemon.inBattleHp.value - damage)
                // cannot go below 0
                .coerceAtLeast(0)
            result(pokemon.copy(inBattleHp = Stat(newHp))) {
                "${pokemon.name} is burned for $damage!"
            }
        }

        else -> result(pokemon)
    }
}

val PoisonEndOfTurnEffect = object : EndOfTurnEffect {
    override fun apply(pokemon: Pokemon): EndOfTurnEffectResult = when (pokemon.majorStatus) {
        is MajorStatus.Poisoned -> {
            val damage = (pokemon.effectiveStats.hp.value / 8).coerceAtLeast(1)
            val newHp = (pokemon.inBattleHp.value - damage)
                // cannot go below 0
                .coerceAtLeast(0)
            result(pokemon.copy(inBattleHp = Stat(newHp))) {
                "${pokemon.name} is poisoned for $damage!"
            }
        }

        else -> result(pokemon)
    }
}

val BadPoisonEndOfTurnEffect = object : EndOfTurnEffect {
    override fun apply(pokemon: Pokemon): EndOfTurnEffectResult = when (pokemon.majorStatus) {
        is MajorStatus.BadlyPoisoned -> {
            val counter = (pokemon.majorStatus.counter + 1).coerceAtMost(15)
            val toxicDamage = (pokemon.effectiveStats.hp.value * counter / 16).coerceAtLeast(1)
            val newHp = (pokemon.inBattleHp.value - toxicDamage)
                // cannot go below 0
                .coerceAtLeast(0)
            val updated = pokemon.copy(
                inBattleHp = Stat(newHp),
                majorStatus = MajorStatus.BadlyPoisoned(counter)
            )
            result(updated) {
                "${pokemon.name} is badly poisoned for $toxicDamage!"
            }
        }

        else -> result(pokemon)
    }
}

val SandstormEndOfTurnEffect = object : EndOfTurnEffect {
    override fun apply(pokemon: Pokemon): EndOfTurnEffectResult {
        val immuneElements = listOf(
            Element.ROCK,
            Element.GROUND,
            Element.STEEL,
        )
        val immuneAbilities = listOf(
            SandForce,
            SandVeil,
            SandRush,
            MagicGuard,
            Overcoat
        )

        return when {
            pokemon.elements.hasAnyOf(immuneElements) -> result(pokemon)

            immuneAbilities.contains(pokemon.ability) -> result(pokemon)

            else -> {
                val damage = (pokemon.effectiveStats.hp.value / 16).coerceAtLeast(1)
                val newHp = (pokemon.inBattleHp.value - damage)
                    // cannot go below 0
                    .coerceAtLeast(0)
                result(pokemon.copy(inBattleHp = Stat(newHp))) {
                    "${pokemon.name} is buffeted for $damage by the Sandstorm."
                }
            }
        }
    }
}

val HailEndOfTurnEffect = object : EndOfTurnEffect {
    override fun apply(pokemon: Pokemon): EndOfTurnEffectResult {
        val immuneAbilities = listOf(
            Overcoat,
            MagicGuard,
            SnowCloak,
            IceBody
        )

        return when {
            pokemon.elements.hasAnyOf(Element.ICE) -> result(pokemon)

            immuneAbilities.contains(pokemon.ability) -> result(pokemon)

            else -> {
                val damage = (pokemon.effectiveStats.hp.value / 16).coerceAtLeast(1)
                val newHp = (pokemon.inBattleHp.value - damage)
                    // cannot go below 0
                    .coerceAtLeast(0)
                result(pokemon.copy(inBattleHp = Stat(newHp))) {
                    "${pokemon.name} is pelted for $damage by Hail."
                }
            }
        }
    }
}
