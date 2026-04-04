package com.drbrosdev.battle.move

import com.drbrosdev.RandomGen
import com.drbrosdev.battle.Battle
import com.drbrosdev.battle.abilities
import com.drbrosdev.battle.pokemon.MajorStatus
import com.drbrosdev.battle.pokemon.VolatileStatus
import com.drbrosdev.battle.pokemon.hasAnyOf
import com.drbrosdev.battle.pokemon.relations
import com.drbrosdev.battle.pokemon.stats.Stat
import com.drbrosdev.battle.pokemon.stats.modify

fun interface MovePrecondition {
    fun MoveContext.check(battle: Battle): Result

    enum class Result {
        PASS,
        TRIGGER
    }
}

private val AccuracyPrecondition = MovePrecondition { battle ->
    val move = battle[userId][moveId]
    when (val accuracy = move.accuracy) {
        is MoveAccuracy.AlwaysHit -> MovePrecondition.Result.PASS
        is MoveAccuracy.Percent -> {
            val user = battle[userId]
            val target = battle[targetId]

            // resolve accuracy and evasion stages
            val effectiveAccuracy = Stat(accuracy.value.value)
                .modify(user.effectiveStats.accuracy)
                .modify(target.effectiveStats.evasion)
                .value
                // accuracy cannot go below 1 and is capped at 100
                .coerceIn(1, 100)

            when {
                RandomGen.nextInt(1, 101) <= effectiveAccuracy -> MovePrecondition.Result.PASS

                else -> MovePrecondition.Result.TRIGGER
            }
        }
    }
}

private val StatusPrecondition = MovePrecondition { battle ->
    val user = battle[userId]
    // To check user pokemon status, like paralysis, freeze, confusion, sleep
    // paralysis chance 25% to prevent attack - we check that here
    if(user.majorStatus is MajorStatus.Paralyzed && MajorStatus.shouldParalyze()) {
        return@MovePrecondition MovePrecondition.Result.TRIGGER
    }

    // other status conditions are simple checks since their healing is handled
    // with ApplyStartOfTurnEffects
    when {
        user.majorStatus is MajorStatus.Frozen -> MovePrecondition.Result.TRIGGER
        user.majorStatus is MajorStatus.Asleep -> MovePrecondition.Result.TRIGGER
        // taunt does not prevent move usage during MovePrecondition
        // Taunt is checked as a part of TurnValidation
        // if any of volatileStatus are not Taunt
        user.volatileStatus.any { it !is VolatileStatus.Taunt } -> MovePrecondition.Result.TRIGGER
        else -> MovePrecondition.Result.PASS
    }
}

private val ProtectionPrecondition = MovePrecondition { battle ->
    // TODO
    // To check things like Protect/Detect/Wide Guard etc
    MovePrecondition.Result.PASS
}

private val ElementImmunityPrecondition = MovePrecondition { battle ->
    val target = battle[targetId]
    val moveElement = battle[userId][moveId].element
    when {
        moveElement.relations.immune.hasAnyOf(target.elements.values) ->
            MovePrecondition.Result.TRIGGER

        else -> MovePrecondition.Result.PASS
    }
}

private val TargetFaintedPrecondition = MovePrecondition { battle ->
    // If the target is 0 HP already
    val target = battle[targetId]
    when {
        target.inBattleHp.value == 0 -> MovePrecondition.Result.TRIGGER
        else -> MovePrecondition.Result.PASS
    }
}
/*
Chain of Responsibility pattern.
Each MovePrecondition decides to either handle (return) or pass along.
The first Trigger result ends the chain.
 */
fun MoveContext.resolvePreconditions(
    battle: Battle,
): MovePrecondition.Result {
    val context = this
    val conditions = sequence {
        yield(TargetFaintedPrecondition)
        yield(StatusPrecondition)
        yield(AccuracyPrecondition)
        yield(ProtectionPrecondition)
        yield(ElementImmunityPrecondition)
        yieldAll(battle.abilities().flatMap { it.movePrecondition })
    }
    return conditions
        .map { with(it) { context.check(battle) } }
        .firstOrNull { it == MovePrecondition.Result.TRIGGER }
        ?: MovePrecondition.Result.PASS
}
