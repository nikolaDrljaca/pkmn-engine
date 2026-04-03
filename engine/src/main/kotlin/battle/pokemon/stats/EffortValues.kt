package com.drbrosdev.battle.pokemon.stats

import com.drbrosdev.battle.pokemon.PokemonDsl

data class EffortValues(
    val stats: Map<StatKey, Stat> = StatKey.entries.associateWith { Stat(0) }
) {
    init {
        require(stats.values.sumOf { it.value } <= MAX_SUM) {
            "Effort Value sum cannot exceed $MAX_SUM!"
        }
    }

    operator fun get(key: StatKey) = requireNotNull(stats[key]) {
        "Effort Value for $key cannot be null!"
    }

    companion object {
        const val SINGLE_MAX = 252
        const val MAX_SUM = 508
    }
}

@PokemonDsl
class EffortValuesBuilder {

    var hp: Int = 0
    var attack: Int = 0
    var defence: Int = 0
    var specialAttack: Int = 0
    var specialDefence: Int = 0
    var speed: Int = 0

    fun maxHp() { hp = EffortValues.SINGLE_MAX }
    fun maxAttack() { attack = EffortValues.SINGLE_MAX }
    fun maxDefence() { defence = EffortValues.SINGLE_MAX }
    fun maxSpecialAttack() { specialAttack = EffortValues.SINGLE_MAX }
    fun maxSpecialDefence() { specialDefence = EffortValues.SINGLE_MAX }
    fun maxSpeed() { speed = EffortValues.SINGLE_MAX }

    fun build() = EffortValues(
        stats = mapOf(
            StatKey.HP to Stat(hp),
            StatKey.ATTACK to Stat(attack),
            StatKey.DEFENCE to Stat(defence),
            StatKey.SPECIAL_ATTACK to Stat(specialAttack),
            StatKey.SPECIAL_DEFENCE to Stat(specialDefence),
            StatKey.SPEED to Stat(speed),
        ),
    )
}
