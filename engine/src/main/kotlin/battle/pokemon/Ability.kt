package com.drbrosdev.battle.pokemon

import com.drbrosdev.battle.environment.SwitchInEffect
import com.drbrosdev.battle.environment.Weather
import com.drbrosdev.battle.move.MoveEffect
import com.drbrosdev.battle.move.MovePrecondition
import com.drbrosdev.battle.move.MovePreconditionResult
import com.drbrosdev.battle.pokemon.stats.*
import com.drbrosdev.battle.turn.TurnAction
import com.drbrosdev.battle.turn.validation.TurnValidator
import java.util.logging.Logger

private val LOG = Logger.getLogger(Ability::class.qualifiedName)

interface Ability {
    val name: String

    val switchInEffects: List<SwitchInEffect> get() = emptyList()

    val turnValidators: List<TurnValidator> get() = emptyList()

    val orderingRules: List<Comparator<TurnAction>> get() = emptyList()

    // support for things like Scrappy ability
    val movePrecondition: List<MovePrecondition> get() = emptyList()

    val moveEffects: List<MoveEffect> get() = emptyList()

    val statModifications: List<StatModification> get() = emptyList()
}

val Prankster = object : Ability {
    override val name: String = "Prankster"

}

val Overgrow = object : Ability {
    override val name: String
        get() = "Overgrow"
    override val statModifications: List<StatModification>
        get() = listOf(StatModification { context ->
            val (pokemon, _) = context
            StatModifiers()
        })
}

val RunAway = object : Ability {
    override val name: String = "Run Away"
}
val Pressure = object : Ability {
    override val name: String = "Pressure"
    /* Effectively does nothing */
}

// Prevents Confusion
val OwnTempo = object : Ability {
    override val name: String = "Own Tempo"
}

val Scrappy = object : Ability {
    override val name: String = "Scrappy"
    override val movePrecondition: List<MovePrecondition> = listOf(MovePrecondition {
        MovePreconditionResult(MovePrecondition.Result.PASS)
    })
}

val SandStream = object : Ability {
    override val name: String = "Sand Stream"
    override val switchInEffects: List<SwitchInEffect>
        get() = listOf(SwitchInEffect { _, battle ->
            battle
                .copy(weather = Weather.Sandstorm(null))
                .narrative(message = "A sandstorm kicked up!")
        })
}

val Levitate = object : Ability {
    override val name: String = "Levitate"
    override val movePrecondition: List<MovePrecondition> = listOf(MovePrecondition { battle ->
        val move = battle[userId][moveId]
        val target = battle[targetId]
        when {
            // NOTE: Spikes apply regardless of levitate
            move.id.id == "spikes" -> MovePreconditionResult(MovePrecondition.Result.PASS)

            move.element == Element.GROUND -> {
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
    override val name: String = "Iron Barbs"
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
    override val name: String = "Intimidate"

    override val switchInEffects: List<SwitchInEffect>
        get() = listOf(SwitchInEffect { pokemon, battle ->
            val opponent = when {
                battle.active1 == pokemon.id -> battle[battle.active2]
                else -> battle[battle.active1]
            }
            val immunities = listOf(ClearBody, HyperCutter, WhiteSmoke)
            when {
                opponent.ability in immunities -> battle
                else -> {
                    val modification = StatModification { _ ->
                        StatModifiers(mapOf(StatKey.ATTACK to StatModifier.negativeStage(1)))
                    }
                    val updated = opponent.modifyStats(modification)
                    battle
                        .narrative("${pokemon.name}'s Intimidate cuts ${opponent.name}'s attack!")
                        .updateMons(updated)
                }
            }
        })
}

val Justified = object : Ability {
    override val name: String = "Justified"
    override val moveEffects: List<MoveEffect> = listOf(MoveEffect { battle ->
        val user = battle[userId]
        val move = user[moveId]
        val isDarkMove = move.element == Element.DARK
        when {
            isDarkMove -> {
                val statModification = StatModification {
                    StatModifiers(mapOf(StatKey.ATTACK to StatModifier.positiveStage(1)))
                }
                val updatedUser = user.copy(
                    statModifications = statModifications + statModification
                )
                battle
                    .narrative("${user.name}'s attack is raised!")
                    .updateMons(updatedUser)
            }
            else -> battle
        }
    })
}

val RoughSkin = object : Ability {
    override val name: String = "Rough Skin"
    override val moveEffects: List<MoveEffect> = listOf(MoveEffect { battle ->
        val user = battle[userId]
        val move = battle[userId][moveId]
        val damage = (user.effectiveStats.hp.value / 16).coerceAtLeast(1)
        val newHp = (user.inBattleHp.value - damage).coerceAtLeast(0)
        val updatedUser = user.copy(inBattleHp = Stat(newHp))
        when {
            move.contact -> battle
                .updateMons(updatedUser)
                .narrative("${user.name} is hurt by rough skin for $damage!")

            else -> battle
        }
    })
}

val Sturdy = object : Ability {
    override val name: String = "Sturdy"
}

val PoisonHeal = object : Ability {
    override val name: String = "Poison Heal"
    override val moveEffects: List<MoveEffect> = listOf(MoveEffect { battle ->
        val user = battle[userId]
        val isPoisoned = user.majorStatus is MajorStatus.Poisoned || user.majorStatus is MajorStatus.BadlyPoisoned
        when {
            isPoisoned -> {
                val hpToRestore = (user.effectiveStats.hp.value / 8).coerceAtLeast(1)
                val newHp = (user.inBattleHp.value + hpToRestore).coerceAtMost(user.effectiveStats.hp.value)
                val updatedUser = user.copy(inBattleHp =  Stat(newHp))
                battle
                    .updateMons(updatedUser)
                    .narrative("${user.name} restores $hpToRestore via Poison Heal!")
            }
            else -> battle
        }
    })
}

val SandVeil = object : Ability { /*Effectively does nothing*/
    override val name: String = "Sand Veil"
}
val SandRush = object : Ability { /*Effectively does nothing*/
    override val name: String = "Sand Rush"
}
val SandForce = object : Ability { /*Effectively does nothing*/
    override val name: String = "Sand Force"
}
val MagicGuard = object : Ability { /*Effectively does nothing*/
    override val name: String = "Magic Guard"
}
val IceBody = object : Ability { /*Effectively does nothing*/
    override val name: String = "Ice Body"
}
val Overcoat = object : Ability { /*Effectively does nothing*/
    override val name: String = "Overcoat"
}
val SnowCloak = object : Ability { /*Effectively does nothing*/
    override val name: String = "Snow Cloak"
}
val ShellArmor = object : Ability { /*Effectively does nothing*/
    override val name: String = "Shell Armor"
}
val StrongJaw = object : Ability { /* Effectively does nothing*/
    override val name: String = "Strong Jaw"
}
val ClearBody = object : Ability { /* Effectively does nothing*/
    override val name: String = "Clear Body"
}
val HyperCutter = object : Ability { /* Effectively does nothing*/
    override val name: String = "Hyper Cutter"
}
val WhiteSmoke = object : Ability { /* Effectively does nothing*/
    override val name: String = "White Smoke"
}
val BattleArmor = object : Ability { /*Effectively does nothing*/
    override val name: String = "Battle Armor"
}