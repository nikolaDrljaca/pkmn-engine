package com.drbrosdev.battle.pokemon.stats

import com.drbrosdev.battle.pokemon.PokemonDsl
import kotlin.collections.get

@JvmInline
value class Stat(val value: Int = 1) {
    init {
        require(value >= 0)
    }

    operator fun minus(other: Stat): Stat =
        Stat((value - other.value).coerceAtLeast(0))

    operator fun plus(other: Stat): Stat =
        Stat((value + other.value).coerceAtLeast(0))
}

enum class StatKey {
    // base stats
    HP,
    ATTACK,
    DEFENCE,
    SPECIAL_ATTACK,
    SPECIAL_DEFENCE,
    SPEED,

}

enum class InBattleStatKey {
    ACCURACY,
    EVASION
}

val StatKey.displayValue
    get() = when (this) {
        StatKey.HP -> "Health"
        StatKey.ATTACK -> "Attack"
        StatKey.DEFENCE -> "Defence"
        StatKey.SPECIAL_ATTACK -> "Special Attack"
        StatKey.SPECIAL_DEFENCE -> "Special Defence"
        StatKey.SPEED -> "Speed"
    }

val InBattleStatKey.displayValue
    get() = when (this) {
        InBattleStatKey.ACCURACY -> "Accuracy"
        InBattleStatKey.EVASION -> "Evasion"
    }

data class BaseStats(
    val stats: Map<StatKey, Stat> = StatKey.entries.associateWith { Stat() },
    // evasion and accuracy always start at 100%
    val inBattleStats: Map<InBattleStatKey, Stat> = InBattleStatKey.entries.associateWith { Stat(100) }
) {
    init {
        // all stats are present and have a value
        require(StatKey.entries.all { stats.containsKey(it) && stats[it] != null })
        // all in-battle stats are present and have a value
        require(InBattleStatKey.entries.all { inBattleStats.containsKey(it) && inBattleStats[it] != null })
    }

    val hp get() = getStat(StatKey.HP)
    val attack get() = getStat(StatKey.ATTACK)
    val defence get() = getStat(StatKey.DEFENCE)
    val specialAttack get() = getStat(StatKey.SPECIAL_ATTACK)
    val specialDefence get() = getStat(StatKey.SPECIAL_DEFENCE)
    val speed get() = getStat(StatKey.SPEED)

    val accuracy get() = requireNotNull(inBattleStats[InBattleStatKey.ACCURACY])
    val evasion get() = requireNotNull(inBattleStats[InBattleStatKey.EVASION])

    private fun getStat(statKey: StatKey): Stat = requireNotNull(stats[statKey]) {
        "Base stat $statKey cannot be null!"
    }
}

@PokemonDsl
class BaseStatsBuilder {

    var baseHp: Int = 50
    var attack: Int = 50
    var defence: Int = 50
    var specialAttack: Int = 50
    var specialDefence: Int = 50
    var speed: Int = 50

    var accuracy: Int = 100
    var evasion: Int = 100

    fun build() = BaseStats(
        stats = mapOf(
            StatKey.HP to Stat(baseHp),
            StatKey.ATTACK to Stat(attack),
            StatKey.DEFENCE to Stat(defence),
            StatKey.SPECIAL_ATTACK to Stat(specialAttack),
            StatKey.SPECIAL_DEFENCE to Stat(specialDefence),
            StatKey.SPEED to Stat(speed),
        ),
        inBattleStats = mapOf(
            InBattleStatKey.ACCURACY to Stat(accuracy),
            InBattleStatKey.EVASION to Stat(evasion)
        )
    )
}

fun buildBaseStats(block: BaseStatsBuilder.() -> Unit): BaseStats =
    BaseStatsBuilder().apply(block).build()

fun BaseStats.resolve(
    flat: StatModifiers,
): BaseStats {
    val computed = StatKey.entries.associateWith { key ->
        val baseStat = requireNotNull(this.stats[key])
        val afterFlat = flat.modifiers[key]
            ?.let { baseStat.modify(it) }
            ?: baseStat
        afterFlat
    }
    return BaseStats(computed)
}
