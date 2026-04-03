package com.drbrosdev.battle.move

import com.drbrosdev.battle.Battle
import com.drbrosdev.battle.abilities

fun interface MovePrecondition {
    fun MoveContext.check(battle: Battle): Result

    enum class Result {
        PASS,
        TRIGGER
    }
}

private val AccuracyPrecondition = MovePrecondition { battle ->
    // TODO
    MovePrecondition.Result.PASS
}

private val StatusPrecondition = MovePrecondition { battle ->
    // TODO
    MovePrecondition.Result.PASS
}

private val ProtectionPrecondition = MovePrecondition { battle ->
    // TODO
    MovePrecondition.Result.PASS
}

private val ImmunityPrecondition = MovePrecondition { battle ->
    // TODO
    MovePrecondition.Result.PASS
}

private val TargetFaintedPrecondition = MovePrecondition { battle ->
    // TODO
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
