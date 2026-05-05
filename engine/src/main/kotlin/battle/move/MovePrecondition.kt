package com.drbrosdev.battle.move

import com.drbrosdev.RandomGen
import com.drbrosdev.battle.Battle
import com.drbrosdev.battle.abilities
import com.drbrosdev.battle.pokemon.MajorStatus
import com.drbrosdev.battle.pokemon.VolatileStatus
import com.drbrosdev.battle.pokemon.hasAnyOf
import com.drbrosdev.battle.pokemon.isConfused
import com.drbrosdev.battle.pokemon.isInfatuated
import com.drbrosdev.battle.pokemon.relations
import com.drbrosdev.battle.pokemon.stats.Stat
import com.drbrosdev.battle.pokemon.stats.modify
import java.util.logging.Logger

private val LOG = Logger.getLogger(MovePrecondition::class.qualifiedName)

fun interface MovePrecondition {
    fun MoveContext.check(battle: Battle): MovePreconditionResult


    enum class Result {
        PASS,
        TRIGGER,
        MISS,
        PROTECTED,
        IMMUNE,
        PARALYZED,
        FROZEN,
        ASLEEP,
        CONFUSED,
        INFATUATED
    }
}

data class MovePreconditionResult(
    val result: MovePrecondition.Result,
    val narrativeMessage: String = ""
)

// Extension is only for scoping.
fun MovePrecondition.result(preconditionResult: MovePrecondition.Result, message: () -> String = { "" }) = MovePreconditionResult(
    result = preconditionResult,
    narrativeMessage = message()
)

private object AccuracyPrecondition : MovePrecondition {
    override fun MoveContext.check(battle: Battle): MovePreconditionResult {
        val move = battle[userId][moveId]
        return when (val accuracy = move.accuracy) {
            is MoveAccuracy.AlwaysHit -> result(MovePrecondition.Result.PASS)
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
                    RandomGen.nextInt(1, 101) <= effectiveAccuracy -> result(MovePrecondition.Result.PASS)

                    else -> result(MovePrecondition.Result.MISS) {
                        "${user.name} attack missed!"
                    }
                }
            }
        }
    }
}

private object StatusPrecondition : MovePrecondition {
    override fun MoveContext.check(battle: Battle): MovePreconditionResult {
        val user = battle[userId]

        if (user.majorStatus is MajorStatus.Paralyzed && MajorStatus.shouldParalyze()) {
            return result(MovePrecondition.Result.PARALYZED) {
                "${user.name} is paralyzed! It can't move!"
            }
        }

        if (user.isConfused() && VolatileStatus.Confusion.shouldTrigger()) {
            return result(MovePrecondition.Result.CONFUSED) {
                "${user.name} is confused!"
            }
        }

        if (user.isInfatuated() && VolatileStatus.Infatuation.shouldTrigger()) {
            return result(MovePrecondition.Result.INFATUATED) {
                "${user.name} is incapacitated with affection!"
            }
        }

        // other status conditions are simple checks since their healing is handled
        // with ApplyStartOfTurnEffects
        return when (user.majorStatus) {
            is MajorStatus.Frozen -> result(MovePrecondition.Result.FROZEN) {
                "${user.name} is frozen solid!"
            }
            is MajorStatus.Asleep -> result(MovePrecondition.Result.ASLEEP) {
                "${user.name} is fast asleep."
            }
            else -> result(MovePrecondition.Result.PASS)
        }
    }
}

private object ProtectionPrecondition : MovePrecondition {
    // TODO impl
    // To check things like Protect/Detect/Wide Guard etc
    override fun MoveContext.check(battle: Battle): MovePreconditionResult {
        return result(MovePrecondition.Result.PASS)
    }
}

private object ElementImmunityPrecondition : MovePrecondition {
    override fun MoveContext.check(battle: Battle): MovePreconditionResult {
        val target = battle[targetId]
        val moveElement = battle[userId][moveId].element
        return when {
            moveElement.relations.immune.hasAnyOf(target.elements.values) ->
                result(MovePrecondition.Result.IMMUNE) {
                    "It does not affect ${target.name}!"
                }

            else -> result(MovePrecondition.Result.PASS)
        }
    }
}

private object TargetFaintedPrecondition : MovePrecondition {
    override fun MoveContext.check(battle: Battle): MovePreconditionResult {
        // If the target is 0 HP already
        val target = battle[targetId]
        return when {
            target.inBattleHp.value == 0 -> result(MovePrecondition.Result.TRIGGER) {
                "But it failed!"
            }
            else -> result(MovePrecondition.Result.PASS)
        }
    }
}

/*
Chain of Responsibility pattern.
Each MovePrecondition decides to either handle (return) or pass along.
The first Trigger result ends the chain.
 */
fun MoveContext.resolvePreconditions(
    battle: Battle,
): MovePreconditionResult {
    val conditions = sequence {
        yield(TargetFaintedPrecondition)
        yield(StatusPrecondition)
        yield(AccuracyPrecondition)
        yield(ProtectionPrecondition)
        yield(ElementImmunityPrecondition)
        yieldAll(battle.abilities().flatMap { it.movePrecondition })
    }
    val applicablePreconditionResults = MovePrecondition.Result.entries
        .filter { it != MovePrecondition.Result.PASS }
    return conditions
        .map { with(it) { check(battle) } }
        .firstOrNull { result -> applicablePreconditionResults.any { it == result.result } }
        ?.also { result ->
            LOG.info { "$userId fails to execute move - $result" }
        }
        ?: MovePreconditionResult(MovePrecondition.Result.PASS)
}
