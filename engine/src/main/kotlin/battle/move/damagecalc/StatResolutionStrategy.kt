package com.drbrosdev.battle.move.damagecalc

import com.drbrosdev.battle.Battle
import com.drbrosdev.battle.move.MoveContext
import com.drbrosdev.battle.move.MoveType
import com.drbrosdev.battle.pokemon.statModification
import com.drbrosdev.battle.pokemon.statModifications
import com.drbrosdev.battle.pokemon.stats.Stat
import com.drbrosdev.battle.pokemon.stats.StatModificationContext
import com.drbrosdev.battle.pokemon.stats.isNegativeStage
import com.drbrosdev.battle.pokemon.stats.isPositiveStage
import com.drbrosdev.battle.pokemon.stats.resolve

fun interface StatResolutionStrategy {
    /**
     * Computes attacker (special) attack and defender (special) defence
     * for damage calculation purposes.
     */
    fun MoveContext.resolve(battle: Battle): Pair<Stat, Stat>
}

val NormalStatResolution = StatResolutionStrategy { battle ->
    val user = battle[userId]
    val target = battle[targetId]
    val move = user[moveId]

    val userStats = user.allStatModifications
        .map { it.compute(StatModificationContext(user, battle)) }
        .fold(user.effectiveStats) { stats, mod -> stats.resolve(mod) }
    // NOTE: Sandstorm boosts defenders SpDef by 50% if rock type
    val targetStats = (target.allStatModifications + battle.weather.statModification)
        .map { it.compute(StatModificationContext(target, battle)) }
        .fold(target.effectiveStats) { stats, mod -> stats.resolve(mod) }

    when (move.type) {
        MoveType.PHYSICAL -> userStats.attack to targetStats.defence
        MoveType.SPECIAL -> userStats.specialAttack to targetStats.specialDefence
        MoveType.STATUS -> error("Cannot apply stat resolution for STATUS moves!")
    }
}

val CritStatResolution = StatResolutionStrategy { battle ->
    val user = battle[userId]
    val target = battle[targetId]
    val move = user[moveId]

    val userStats = user.allStatModifications
        .map { it.compute(StatModificationContext(user, battle)) }
        // ignore negative stage changes on user
        .filter { mod -> mod.modifiers.values.none { it.isNegativeStage() } }
        .fold(user.effectiveStats) { stats, mod -> stats.resolve(mod) }
    val targetStats = target.allStatModifications
        .map { it.compute(StatModificationContext(target, battle)) }
        // ignore positive stage changes on target
        .filter { mod -> mod.modifiers.values.none { it.isPositiveStage() } }
        .fold(target.effectiveStats) { stats, mod -> stats.resolve(mod) }

    when (move.type) {
        MoveType.PHYSICAL -> userStats.attack to targetStats.defence
        MoveType.SPECIAL -> userStats.specialAttack to targetStats.specialDefence
        MoveType.STATUS -> error("Cannot apply stat resolution for STATUS moves!")
    }
}

val ConfusionDamageStatResolution = StatResolutionStrategy { battle ->
    val user = battle[userId]
    val statMods = buildList {
        add(user.nature.statModification)
        addAll(user.ability.statModifications)
        add(user.majorStatus.statModifications())
        addAll(user.statModifications)
        // NOTE: ignore choice-band boost
        if (user.item.id.value != "choice-band") {
            add(user.item.statModification)
        }
    }
    val stats = statMods
        .map { it.compute(StatModificationContext(user, battle)) }
        .fold(user.effectiveStats) { stats, mods -> stats.resolve(mods) }
    stats.attack to stats.defence
}
val PsyshockStatResolution = StatResolutionStrategy { battle ->
    // TODO: (stat-resolution) implement
    // Moves like foul play etc.
    TODO()
}