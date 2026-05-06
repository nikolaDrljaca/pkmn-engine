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

fun MajorStatus.enterNarrativeMessage(user: String): String = when (this) {
    is MajorStatus.Asleep -> "$user fell asleep!"
    is MajorStatus.BadlyPoisoned -> "$user was badly poisoned!"
    MajorStatus.Burned -> "$user was burned!"
    MajorStatus.Frozen -> "$user was frozen solid!"
    MajorStatus.Paralyzed -> "$user is paralyzed! It may be unable to move."
    MajorStatus.Poisoned -> "$user was poisoned!"
    MajorStatus.Normal -> ""
}

fun MajorStatus.exitNarrativeMessage(user: String): String = when (this) {
    is MajorStatus.Asleep -> "$user woke up!"
    MajorStatus.Frozen -> "$user was thawed out!"
    else -> ""
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

fun VolatileStatus.enterNarrativeMessage(user: String): String = when (this) {
    is VolatileStatus.Confusion -> "$user became confused!"
    is VolatileStatus.Infatuation -> "$user fell in love!"
    is VolatileStatus.Taunt -> ""
}

fun VolatileStatus.exitNarrativeMessage(user: String): String = when (this) {
    is VolatileStatus.Confusion -> "$user snapped out of confusion!"
    is VolatileStatus.Infatuation -> "$user got over its infatuation!"
    is VolatileStatus.Taunt -> ""
}
