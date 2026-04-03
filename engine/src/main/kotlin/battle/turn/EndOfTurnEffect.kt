package com.drbrosdev.battle.turn

import com.drbrosdev.battle.pokemon.*
import com.drbrosdev.battle.pokemon.stats.Stat

fun interface EndOfTurnEffect {
    fun apply(pokemon: Pokemon): Pokemon
}

val BurnEndOfTurnEffect = EndOfTurnEffect { pokemon ->
    when (pokemon.majorStatus) {
        is MajorStatus.Burned -> {
            val damage = (pokemon.effectiveStats.hp.value / 16).coerceAtLeast(1)
            val newHp = (pokemon.inBattleHp.value - damage)
                    // cannot go below 0
                .coerceAtLeast(0)
            pokemon.copy(
                inBattleHp = Stat(newHp)
            )
        }

        else -> pokemon
    }
}

val PoisonEndOfTurnEffect = EndOfTurnEffect { pokemon ->
    when (pokemon.majorStatus) {
        is MajorStatus.Poisoned -> {
            val damage = (pokemon.effectiveStats.hp.value / 8).coerceAtLeast(1)
            val newHp = (pokemon.inBattleHp.value - damage)
                    // cannot go below 0
                .coerceAtLeast(0)
            pokemon.copy(
                inBattleHp = Stat(newHp)
            )
        }

        else -> pokemon
    }
}

val BadPoisonEndOfTurnEffect = EndOfTurnEffect { pokemon ->
    when (pokemon.majorStatus) {
        is MajorStatus.BadlyPoisoned -> {
            val counter = (pokemon.majorStatus.counter + 1).coerceAtMost(15)
            val toxicDamage = (pokemon.effectiveStats.hp.value * counter / 16).coerceAtLeast(1)
            val newHp = (pokemon.inBattleHp.value - toxicDamage)
                    // cannot go below 0
                .coerceAtLeast(0)
            pokemon.copy(
                inBattleHp = Stat(newHp),
                majorStatus = MajorStatus.BadlyPoisoned(counter)
            )
        }

        else -> pokemon
    }
}

/*
TODO
Later on these belong in the Item subsystem
NOTE: It should probably be modeled similar to abilities
 */
val LeftoversEndOfTurnEffect = EndOfTurnEffect { pokemon ->
    // healing is calculated of maxHp
    val healing = (pokemon.effectiveStats.hp.value / 8).coerceAtLeast(1)
    val newHp = (pokemon.inBattleHp.value + healing)
            // cannot over-heal
        .coerceAtMost(pokemon.effectiveStats.hp.value)
    pokemon.copy(
        inBattleHp = Stat(newHp)
    )
}

val BlackSludgeEndOfTurnEffect = EndOfTurnEffect { pokemon ->
    when {
        pokemon.elements.values.contains(Element.POISON) -> {
            val healing = (pokemon.effectiveStats.hp.value / 16).coerceAtLeast(1)
            val newHp = (pokemon.inBattleHp.value + healing)
                    // cannot over-heal
                .coerceAtMost(pokemon.effectiveStats.hp.value)
            pokemon.copy(
                inBattleHp = Stat(newHp)
            )
        }
        // take 1/16 damage
        else -> {
            val damage = (pokemon.effectiveStats.hp.value / 8).coerceAtLeast(1)
            val newHp = (pokemon.inBattleHp.value - damage)
                    // hp cannot go below 0
                .coerceAtLeast(0)
            pokemon.copy(
                inBattleHp = Stat(newHp)
            )
        }
    }
}

val SandstormEndOfTurnEffect = EndOfTurnEffect { pokemon ->
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

    when {
        pokemon.elements.hasAnyOf(immuneElements) -> pokemon

        immuneAbilities.contains(pokemon.ability) -> pokemon

        else -> {
            val damage = (pokemon.effectiveStats.hp.value / 16).coerceAtLeast(1)
            val newHp = (pokemon.inBattleHp.value - damage)
                    // cannot go below 0
                .coerceAtLeast(0)
            pokemon.copy(
                inBattleHp = Stat(newHp)
            )
        }
    }
}

val HailEndOfTurnEffect = EndOfTurnEffect { pokemon ->
    val immuneAbilities = listOf(
        Overcoat,
        MagicGuard,
        SnowCloak,
        IceBody
    )

    when {
        pokemon.elements.hasAnyOf(Element.ICE) -> pokemon

        immuneAbilities.contains(pokemon.ability) -> pokemon

        else -> {
            val damage = (pokemon.effectiveStats.hp.value / 16).coerceAtLeast(1)
            val newHp = (pokemon.inBattleHp.value - damage)
                    // cannot go below 0
                .coerceAtLeast(0)
            pokemon.copy(
                inBattleHp = Stat(newHp)
            )
        }
    }
}
