package com.drbrosdev.battle.pokemon.stats

import com.drbrosdev.battle.pokemon.PokemonDsl

data class IndividualValues(
    val stats: Map<StatKey, Stat> = StatKey.entries.associateWith { Stat() }
) {
    init {
        require(stats.values.all { it.value <= MAX }) {
            "Individual Value cannot exceed $MAX per stat!"
        }
    }

    operator fun get(key: StatKey) = requireNotNull(stats[key]) {
        "Individual Value for $key cannot be null!"
    }

    companion object {
        const val MAX = 31
    }
}

@PokemonDsl
class IndividualValuesBuilder {

    var hp: Int = 0
    var attack: Int = 0
    var defence: Int = 0
    var specialAttack: Int = 0
    var specialDefence: Int = 0
    var speed: Int = 0

    fun allMax() {
        hp = IndividualValues.MAX
        attack = IndividualValues.MAX
        defence = IndividualValues.MAX
        specialAttack = IndividualValues.MAX
        specialDefence = IndividualValues.MAX
        speed = IndividualValues.MAX
    }

    fun build() = IndividualValues(
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
