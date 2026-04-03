package com.drbrosdev.battle.turn

import com.drbrosdev.battle.pokemon.*
import com.drbrosdev.battle.pokemon.stats.Stat

fun interface EndOfTurnEffect {
    fun apply(pokemon: Pokemon): Pokemon
}

val BurnEndOfTurnEffect = EndOfTurnEffect { pokemon ->
    when (pokemon.majorStatus) {
        is MajorStatus.Burned -> {
            val damage = (pokemon.baseStats.hp.value / 16).coerceAtLeast(1)
            pokemon.copy(
                inBattleHp = pokemon.inBattleHp - Stat(damage)
            )
        }

        else -> pokemon
    }
}

val PoisonEndOfTurnEffect = EndOfTurnEffect { pokemon ->
    when (pokemon.majorStatus) {
        is MajorStatus.Poisoned -> {
            val damage = (pokemon.baseStats.hp.value / 8).coerceAtLeast(1)
            pokemon.copy(
                inBattleHp = pokemon.inBattleHp - Stat(damage)
            )
        }

        else -> pokemon
    }
}

val BadPoisonEndOfTurnEffect = EndOfTurnEffect { pokemon ->
    when (pokemon.majorStatus) {
        is MajorStatus.BadlyPoisoned -> {
            val counter = (pokemon.majorStatus.counter + 1).coerceAtMost(15)
            val toxicDamage = (pokemon.baseStats.hp.value * counter / 16).coerceAtLeast(1)
            pokemon.copy(
                inBattleHp = pokemon.inBattleHp - Stat(toxicDamage),
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
    val healing = (pokemon.baseStats.hp.value / 8).coerceAtLeast(1)
    pokemon.copy(
        inBattleHp = pokemon.inBattleHp + Stat(healing)
    )
}

val BlackSludgeEndOfTurnEffect = EndOfTurnEffect { pokemon ->
    when {
        pokemon.elements.values.contains(Element.POISON) -> {
            val damage = (pokemon.baseStats.hp.value / 16).coerceAtLeast(1)
            pokemon.copy(
                inBattleHp = pokemon.inBattleHp - Stat(damage)
            )
        }
        // take 1/16 damage
        else -> {
            val damage = (pokemon.baseStats.hp.value / 8).coerceAtLeast(1)
            pokemon.copy(
                inBattleHp = pokemon.inBattleHp - Stat(damage)
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
            val damage = (pokemon.baseStats.hp.value / 16).coerceAtLeast(1)
            pokemon.copy(
                inBattleHp = pokemon.inBattleHp - Stat(damage)
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
            val damage = (pokemon.baseStats.hp.value / 16).coerceAtLeast(1)
            pokemon.copy(
                inBattleHp = pokemon.inBattleHp - Stat(damage)
            )
        }
    }
}
