package com.drbrosdev.battle.pokemon

import com.drbrosdev.battle.move.MoveEffect
import com.drbrosdev.battle.move.MovePrecondition
import com.drbrosdev.battle.move.MovePreconditionResult
import com.drbrosdev.battle.move.isPhysical
import com.drbrosdev.battle.pokemon.stats.Stat
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

val SandStream = object : Ability {
    // BUG: this is a switch-in effect, and they should apply to the first turn
    /*
    override val moveEffects: List<MoveEffect>
        get() = listOf( MoveEffect { battle ->
            battle
                .copy(weather = Weather.Sandstorm(null))
                .narrative(message = "A sandstorm kicked up!")
        })
     */
}

val Levitate = object : Ability {
    override val movePrecondition: List<MovePrecondition> = listOf(MovePrecondition { battle ->
        val move = battle[userId][moveId]
        val target = battle[targetId]
        when (move.element) {
            Element.GROUND -> {
                MovePreconditionResult(
                    result = MovePrecondition.Result.IMMUNE,
                    narrativeMessage = "It does not affect ${target.name}!"
                )
            }
            else -> MovePreconditionResult(MovePrecondition.Result.PASS)
        }
    })
}

val IronBarbs = object : Ability {
    override val moveEffects: List<MoveEffect> = listOf(MoveEffect { battle ->
        val user = battle[userId]
        val move = battle[userId][moveId]
        val damage = (user.effectiveStats.hp.value / 8).coerceAtLeast(1)
        val newHp = (user.inBattleHp.value - damage).coerceAtLeast(0)
        val updatedUser = user.copy(inBattleHp = Stat(newHp))
        when {
            move.contact -> battle
                .updateMons(updatedUser)
                .narrative("${user.name} is hurt by thorns for $damage!")
            else -> battle
        }
    })
}

val Intimidate = object : Ability {

}

val SandVeil = object : Ability { /*Effectively does nothing*/ }
val SandRush = object : Ability { /*Effectively does nothing*/ }
val SandForce = object : Ability { /*Effectively does nothing*/ }
val MagicGuard = object : Ability { /*Effectively does nothing*/ }
val IceBody = object : Ability { /*Effectively does nothing*/ }
val Overcoat = object : Ability { /*Effectively does nothing*/ }
val SnowCloak = object : Ability { /*Effectively does nothing*/ }
val ShellArmor = object : Ability { /*Effectively does nothing*/ }
val StrongJaw = object : Ability { /* TODO: Effectively does nothing*/ }
val BattleArmor = object : Ability { /*Effectively does nothing*/ }