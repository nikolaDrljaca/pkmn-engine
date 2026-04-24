package com.drbrosdev.battle.pokemon

import com.drbrosdev.RandomGen
import com.drbrosdev.battle.pokemon.stats.StatKey
import com.drbrosdev.battle.pokemon.stats.StatModification
import com.drbrosdev.battle.pokemon.stats.StatModifier
import com.drbrosdev.battle.pokemon.stats.StatModifiers


// Conditions which persist and require
sealed interface MajorStatus {

    data object Normal : MajorStatus

    data object Paralyzed : MajorStatus {
        val modification = StatModification { context ->
            StatModifiers(mapOf(StatKey.SPEED to StatModifier.Percent(50)))
        }
    }

    data object Poisoned : MajorStatus

    data class BadlyPoisoned(val counter: Int) : MajorStatus

    data object Burned : MajorStatus

    data class Asleep(val expiresOnTurn: Int) : MajorStatus

    data object Frozen : MajorStatus

    companion object {
        /*
        A frozen pokemon has a 20% to thaw each turn.
         */
        fun shouldThaw(): Boolean = RandomGen.nextInt(1, 101) <= 20

        fun shouldParalyze(): Boolean = RandomGen.nextInt(1, 101) <= 25
    }
}

fun MajorStatus.statModifications(): StatModification = when (this) {
    is MajorStatus.Paralyzed -> this.modification
    else -> StatModification { StatModifiers() }
}

/*
How will this integrate into the system?
It needs to be a part of the Pokemon state object.

 */
// Ephemeral conditions
sealed interface VolatileStatus {
    val expiresOnTurn: Int

    data class Confusion(override val expiresOnTurn: Int) : VolatileStatus {
        companion object {
            fun shouldTrigger(): Boolean = RandomGen.nextInt(1, 101) <= 50
        }
    }

    data class Infatuation(override val expiresOnTurn: Int) : VolatileStatus {
        companion object {
            fun shouldTrigger(): Boolean = RandomGen.nextInt(1, 101) <= 50
        }
    }

    data class Taunt(override val expiresOnTurn: Int) : VolatileStatus

    companion object {
        /*
        Volatile status lasts for a pre-determined number of turns, after which they heal.
        This rolls and computes number of turns for a new application.
         */
        fun computeExpiry(currentTurn: Int): Int = currentTurn + RandomGen.nextInt(2, 6)

        // factory functions
        fun confusion(currentTurn: Int): VolatileStatus = Confusion(computeExpiry(currentTurn))
        fun infatuation(currentTurn: Int): VolatileStatus = Infatuation(computeExpiry(currentTurn))
        fun taunt(currentTurn: Int): VolatileStatus = Taunt(computeExpiry(currentTurn))
    }
}
