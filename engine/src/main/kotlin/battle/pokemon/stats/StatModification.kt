package com.drbrosdev.battle.pokemon.stats

import com.drbrosdev.battle.Battle
import com.drbrosdev.battle.pokemon.Pokemon

fun interface StatModification {
    fun compute(context: StatModificationContext): StatModifiers
}

sealed interface StatModifier {
    data class Percent(val modifier: Int) : StatModifier {
        init {
            require(modifier > 0)
        }
    }

    data class Stage(val value: Int) : StatModifier {
        init {
            require(value in RANGE)
        }

        companion object {
            val RANGE = (-6)..6
        }
    }

    companion object {
        fun beneficialNature() = Percent(110)
        fun hinderingNature() = Percent(90)

        fun initialStage() = Stage(0)
    }
}

fun StatModifier.increaseStageBy(stage: StatModifier.Stage): StatModifier =
    when (this) {
        is StatModifier.Percent -> this
        is StatModifier.Stage -> StatModifier.Stage(this.value + stage.value)
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

// modifiers applied by nature and held items
data class StatModifiers(val modifiers: Map<StatKey, StatModifier> = emptyMap())

data class StatModificationContext(
    // pokemon whose stats are being modified
    val pokemon: Pokemon,
    val battle: Battle
)
