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
    /*
        TODO
        These are self-healing statuses - is it chance based or?
        If so, a system is necessary to allow this to be applied/calculated at start of turn effects
         */
    data object Asleep : MajorStatus

    data object Frozen : MajorStatus
}

/*
How will this integrate into the system?
It needs to be a part of the Pokemon state object.

TODO
We are missing ways to count down the turns - or to keep track of
which turn it is for these types of systems.

Turn Count system is needed!
Weather effects (no longer permanent?)
Volatile status (infatuation/confusion)
etc?
 */
// Ephemeral conditions
sealed interface VolatileStatus {
    val expiresOnTurn: Int

    data class Confusion(override val expiresOnTurn: Int) : VolatileStatus

    data class Infatuation(override val expiresOnTurn: Int) : VolatileStatus

    data class Taunt(override val expiresOnTurn: Int) : VolatileStatus

    companion object {
        fun computeExpiry(currentTurn: Int): Int = currentTurn + RandomGen.nextInt(2, 6)

        // factory functions
        fun confusion(currentTurn: Int): VolatileStatus = Confusion(computeExpiry(currentTurn))
        fun infatuation(currentTurn: Int): VolatileStatus = Infatuation(computeExpiry(currentTurn))
        fun taunt(currentTurn: Int): VolatileStatus = Taunt(computeExpiry(currentTurn))
    }
}
