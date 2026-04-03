package com.drbrosdev.battle.pokemon.stats

import com.drbrosdev.battle.pokemon.PokemonDsl

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

    operator fun get(key: StatKey) = getStat(key)

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
