package com.drbrosdev.battle.pokemon.stats

import com.drbrosdev.battle.Battle
import com.drbrosdev.battle.pokemon.Pokemon

fun interface StatModification {
    fun compute(context: StatModificationContext): StatModifiers

    companion object {
        val NoModification = StatModification { StatModifiers() }
    }
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

        fun negativeStage(value: Int) = Stage(-value)
        fun positiveStage(value: Int) = Stage(value)
    }
}

fun StatModifier.increaseStageBy(stage: StatModifier.Stage): StatModifier.Stage =
    when (this) {
        is StatModifier.Percent -> error("Cannot increase Stage of Percent StatModifier!")
        is StatModifier.Stage -> StatModifier.Stage(this.value + stage.value)
    }

fun StatModifier.stage(): Int =
    when (this) {
        is StatModifier.Percent -> error("Cannot access Stage for Percent StatModifier!")
        is StatModifier.Stage -> value
    }

fun StatModifier.isNegativeStage() = when (this) {
    is StatModifier.Percent -> false
    is StatModifier.Stage -> value < 0
}

fun StatModifier.isPositiveStage() = when (this) {
    is StatModifier.Percent -> false
    is StatModifier.Stage -> value > 0
}

// modifiers applied by nature and held items
data class StatModifiers(val modifiers: Map<StatKey, StatModifier> = emptyMap())

data class StatModificationContext(
    // pokemon whose stats are being modified
    val pokemon: Pokemon,
    val battle: Battle
)
