package com.drbrosdev.battle.move.damagecalc

import com.drbrosdev.battle.pokemon.statModification
import com.drbrosdev.battle.pokemon.statModifications
import com.drbrosdev.battle.pokemon.stats.StatModificationContext
import com.drbrosdev.battle.pokemon.stats.isNegativeStage
import com.drbrosdev.battle.pokemon.stats.isPositiveStage
import com.drbrosdev.battle.pokemon.stats.resolve
import kotlin.collections.map
import kotlin.collections.plus

fun interface StatResolution {
    /**
     * Computes attacker (special) attack and defender (special) defense
     * for damage calculation purposes based on selected stat keys.
     */
    fun resolve(
        context: DamageEffectContext,
        selection: StatSelection
    ): ResolvedStats
}

class DefaultStatResolution : StatResolution {
    override fun resolve(
        context: DamageEffectContext,
        selection: StatSelection
    ): ResolvedStats = with(context) {
        val userStats = (user.allStatModifications + battle.weather.statModification)
            .map { statMod -> statMod.compute(StatModificationContext(user, battle)) }
            // ignore negative stage changes on user if crit
            .filter { statMod -> !critical || statMod.modifiers.values.none { it.isNegativeStage() } }
            .fold(user.effectiveStats) { stats, mod -> stats.resolve(mod) }
        // NOTE: Sandstorm boosts defenders SpDef by 50% if rock type
        // so count in weather effects
        val targetStats = (target.allStatModifications + battle.weather.statModification)
            .map { statMod -> statMod.compute(StatModificationContext(target, battle)) }
            // ignore positive stage changes on target if crit
            .filter { statMod -> !critical || statMod.modifiers.values.none { it.isPositiveStage() } }
            .fold(target.effectiveStats) { stats, mod -> stats.resolve(mod) }

        ResolvedStats(
            attackStat = userStats[selection.attacker],
            defenceStat = targetStats[selection.defender]
        )
    }
}

object ConfusionDamageStatResolution : StatResolution {
    override fun resolve(
        context: DamageEffectContext,
        selection: StatSelection
    ): ResolvedStats = with(context) {
        val statMods = buildList {
            add(user.nature.statModification)
            addAll(user.ability.statModifications)
            add(user.majorStatus.statModifications())
            addAll(user.statModifications)
            // NOTE: ignore choice-band boost
            // TODO: @drljacan replace magic string/id!
            if (user.item.id.value != "choice-band") {
                add(user.item.statModification)
            }
        }
        val stats = statMods
            .map { it.compute(StatModificationContext(user, battle)) }
            .fold(user.effectiveStats) { stats, mods -> stats.resolve(mods) }
        ResolvedStats(
            attackStat = stats.attack,
            defenceStat = stats.defence
        )
    }
}