package com.drbrosdev.battle.pokemon.stats

import com.drbrosdev.battle.pokemon.Level

data class EffectiveStats(
    val stats: Map<StatKey, Stat> = StatKey.entries.associateWith { Stat() },
    // evasion and accuracy are stage based and start at the initial stage
    val accuracy: StatModifier.Stage = StatModifier.initialStage(),
    val evasion: StatModifier.Stage = StatModifier.initialStage(),
    val criticalHit: StatModifier.Stage = StatModifier.initialStage()
) {
    init {
        // all stats are present and have a value
        require(StatKey.entries.all { stats.containsKey(it) && stats[it] != null })
    }

    operator fun get(key: StatKey) = getStat(key)

    val hp get() = getStat(StatKey.HP)
    val attack get() = getStat(StatKey.ATTACK)
    val defence get() = getStat(StatKey.DEFENCE)
    val specialAttack get() = getStat(StatKey.SPECIAL_ATTACK)
    val specialDefence get() = getStat(StatKey.SPECIAL_DEFENCE)
    val speed get() = getStat(StatKey.SPEED)

    private fun getStat(statKey: StatKey): Stat = requireNotNull(stats[statKey]) {
        "Effective stat $statKey cannot be null!"
    }

    companion object {
        fun from(
            baseStats: BaseStats,
            individualValues: IndividualValues,
            effortValues: EffortValues,
            level: Level
        ): EffectiveStats {
            val currentLevel = level.value
            val computedStats = StatKey.entries.associateWith { key ->
                val base = baseStats[key].value
                val iv = individualValues[key].value
                val ev = effortValues[key].value

                val computed = when (key) {
                    StatKey.HP -> ((2 * base + iv + ev.floorDiv(4)) * currentLevel).floorDiv(100) + currentLevel + 10

                    else -> ((2 * base + iv + ev.floorDiv(4)) * currentLevel).floorDiv(100) + 5
                }

                Stat(computed)
            }
            return EffectiveStats(stats = computedStats)
        }
    }
}

fun EffectiveStats.resolve(
    flat: StatModifiers,
): EffectiveStats {
    val computed = StatKey.entries.associateWith { key ->
        val baseStat = this[key]
        val afterFlat = flat.modifiers[key]
            ?.let { baseStat.modify(it) }
            ?: baseStat
        afterFlat
    }
    return copy(stats = computed)
}