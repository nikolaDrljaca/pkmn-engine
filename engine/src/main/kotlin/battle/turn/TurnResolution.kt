package com.drbrosdev.battle.turn

import com.drbrosdev.battle.Battle

// acts like an orchestrator to describe how a turn is resolved
fun Battle.resolveTurn(turn: Turn): Battle {
    // 1 validate turn
    /*
    TODO
    NOTE This piece might need to be separate from turn Resolution
    as it returns back for user input immediately (in the games)
    This ofc depends on how we want to handle user input and turn construction.
    In this case this is not a part of turn resolution, but a part of turn
    construction.
     */
    val turnValidity = validateTurn(turn)
    if (turnValidity is TurnValidity.Invalid) {
        return this
    }

    // 2 Determine which action goes first - to build TurnStep pipeline
    val (first, second) = resolveActionOrder(turn)

    // 3 construct TurnStep pipeline
    // This way battles can end in a draw, which is legal
    val steps = sequenceOf(
        // first pokemon action
        ExecuteActionStep(first),
        // second pokemon action
        ExecuteActionStep(second),
        // end of turn effects in order
        ApplyEndOfTurnEffects(first),
        ApplyEndOfTurnEffects(second),
        // check for conclusion - can be a draw here
        CheckConclusion,
        HandleTurnCounter
    )
    // 4 execute pipeline
    return resolveTurnSteps(steps)
}
