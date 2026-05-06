package com.drbrosdev.battle.turn

import com.drbrosdev.battle.Battle
import java.util.logging.Logger

private val LOG = Logger.getLogger("com.drbrosdev.battle.turn.TurnResolution")

// acts like an orchestrator to describe how a turn is resolved
fun Battle.resolveTurn(turn: Turn): Battle {
    // 1 validate turn
    /*
    TODO:
    NOTE This piece might need to be separate from turn Resolution
    as it returns back for user input immediately (in the games)
    This ofc depends on how we want to handle user input and turn construction.
    In this case this is not a part of turn resolution, but a part of turn
    construction.
     */
    LOG.fine { "Resolving turn $turnCount. Active: $active1 : $active2" }
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
        ApplyStartOfTurnEffects(first),
        ExecuteActionStep(first),
        // second pokemon action
        ApplyStartOfTurnEffects(second),
        ExecuteActionStep(second),
        // end of turn effects in order
        ApplyEndOfTurnEffects(first),
        ApplyEndOfTurnEffects(second),
        // check for conclusion - can be a draw here
        CheckConclusion,
        HandleTurnCounter
    )
    // 4 clear logs and execute pipeline
    return copy(turnLog = listOf("Turn $turnCount started."))
        .resolveTurnSteps(steps)
}
