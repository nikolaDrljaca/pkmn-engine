package com.drbrosdev.battle.pokemon

import com.drbrosdev.battle.move.MoveEffect
import com.drbrosdev.battle.move.MovePrecondition
import com.drbrosdev.battle.move.MovePreconditionResult
import com.drbrosdev.battle.pokemon.stats.StatModification
import com.drbrosdev.battle.pokemon.stats.StatModifiers
import com.drbrosdev.battle.turn.TurnActionOrderRule
import com.drbrosdev.battle.turn.TurnStep
import com.drbrosdev.battle.turn.TurnValidator

interface Ability {
    val turnValidators: List<TurnValidator> get() = emptyList()

    val orderingRules: List<TurnActionOrderRule> get() = emptyList()

    // support for things like Scrappy ability
    val movePrecondition: List<MovePrecondition> get() = emptyList()

    val moveEffects: List<MoveEffect> get() = emptyList()

    val turnSteps: List<TurnStep> get() = emptyList()

    val statModifications: List<StatModification> get() = emptyList()
}

val Prankster = object : Ability {

}

val Overgrow = object : Ability {
    override val statModifications: List<StatModification>
        get() = listOf(StatModification { context ->
            val (pokemon, _) = context
            StatModifiers()
        })
}

val RunAway = object : Ability { /*Effectively does nothing*/ }
val Pressure = object : Ability {
    /* Effectively does nothing */
}

// Prevents Confusion
val OwnTempo = object : Ability {}

val Scrappy = object : Ability {
    override val movePrecondition: List<MovePrecondition> = listOf(MovePrecondition {
        MovePreconditionResult(MovePrecondition.Result.PASS)
    })
}

val SandVeil = object : Ability { /*Effectively does nothing*/ }
val SandRush = object : Ability { /*Effectively does nothing*/ }
val SandForce = object : Ability { /*Effectively does nothing*/ }
val MagicGuard = object : Ability { /*Effectively does nothing*/ }
val IceBody = object : Ability { /*Effectively does nothing*/ }
val Overcoat = object : Ability { /*Effectively does nothing*/ }
val SnowCloak = object : Ability { /*Effectively does nothing*/ }
val ShellArmor = object : Ability { /*Effectively does nothing*/ }
val BattleArmor = object : Ability { /*Effectively does nothing*/ }