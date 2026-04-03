package com.drbrosdev.battle.move

import com.drbrosdev.battle.Battle
import com.drbrosdev.battle.abilities
import com.drbrosdev.battle.pokemon.MajorStatus
import com.drbrosdev.battle.pokemon.hasAnyOf

fun interface MovePrecondition {
    fun MoveContext.check(battle: Battle): Result

    enum class Result {
        PASS,
        TRIGGER
    }
}

private val AccuracyPrecondition = MovePrecondition { battle ->
    // TODO
    // For moves with MoveAccuracy.Percentage
    MovePrecondition.Result.PASS
}

private val StatusPrecondition = MovePrecondition { battle ->
    // TODO
    // To check user pokemon status, like paralysis, freeze, confusion, sleep
    MovePrecondition.Result.PASS
}

private val ProtectionPrecondition = MovePrecondition { battle ->
    // TODO
    // To check things like Protect/Detect/Wide Guard etc
    MovePrecondition.Result.PASS
}

private val ImmunityPrecondition = MovePrecondition { battle ->
    // TODO
    // To check things like Typing Immunity (normal -> ghost)
    MovePrecondition.Result.PASS
}

private val TargetFaintedPrecondition = MovePrecondition { battle ->
    // TODO
    // If the target is 0 HP already
    MovePrecondition.Result.PASS
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
        yieldAll(battle.abilities().flatMap { it.movePrecondition })
        yield(StatusPrecondition)
        yield(AccuracyPrecondition)
        yield(ProtectionPrecondition)
        yield(ImmunityPrecondition)
    }
    return conditions
        .map { with(it) { context.check(battle) } }
        .firstOrNull { it == MovePrecondition.Result.TRIGGER }
        ?: MovePrecondition.Result.PASS
}
