package com.drbrosdev.battle.pokemon.stats

@JvmInline
value class Stat(val value: Int = 1) {
    init {
        require(value >= 0)
    }

    override fun toString(): String = value.toString()

    operator fun minus(other: Stat): Stat =
        Stat((value - other.value).coerceAtLeast(0))

    operator fun plus(other: Stat): Stat =
        Stat((value + other.value).coerceAtLeast(0))
}

fun Stat.modify(modifier: StatModifier): Stat = when (modifier) {
    is StatModifier.Percent -> {
        Stat((value * modifier.modifier / 100.0).toInt())
    }

    is StatModifier.Stage -> {
        val multiplier = when {
            modifier.value == 0 -> 1.0

            modifier.value > 0 -> (2 + modifier.value) / 2.0

            else -> 2.0 / (2 - modifier.value)
        }
        Stat((value * multiplier).toInt())
    }
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