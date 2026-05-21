package com.drbrosdev.battle.turn

import com.drbrosdev.battle.Battle
import com.drbrosdev.battle.pokemon.hasFainted
import com.drbrosdev.battle.turn.validation.TurnValidity
import com.drbrosdev.battle.turn.validation.narrativeMessage
import com.drbrosdev.battle.turn.validation.validateTurn
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
        return copy(turnLog = listOf(turnValidity.reason.narrativeMessage))
    }

    // 2 Determine which action goes first - to build TurnStep pipeline
    val (first, second) = resolveActionOrder(turn)

    // 3 construct TurnStep pipeline
    // This way battles can end in a draw, which is legal
    val steps: List<TurnStep> = when {
        isFreeSwitch(first) && isFreeSwitch(second) -> listOf(
            ExecuteActionStep(first),
            ExecuteActionStep(second)
        )

        isFreeSwitch(first) -> listOf(
            ExecuteActionStep(first),
            ApplyEndOfTurnEffects(second)
        )

        isFreeSwitch(second) -> listOf(
            ExecuteActionStep(second),
            ApplyEndOfTurnEffects(first)
        )

        else -> buildList {
            if (turnCount == 1) {
                add(SwitchInTurnStep(get(first.user).id))
                add(SwitchInTurnStep(get(second.user).id))
            }
            add(ResolveStartOfTurnWeather())
            // first pokemon action
            add(ApplyStartOfTurnEffects(first))
            add(ExecuteActionStep(first))
            // second pokemon action
            add(ApplyStartOfTurnEffects(second))
            add(ExecuteActionStep(second))
            // end of turn effects in order
            add(ApplyEndOfTurnEffects(first))
            add(ApplyEndOfTurnEffects(second))
            // check for conclusion - can be a draw here
            add(CheckConclusion)
            add(HandleTurnCounter)
        }
    }
    // 4 clear logs and execute pipeline
    return copy(turnLog = listOf("Turn $turnCount started."))
        .resolveTurnSteps(steps)
}

private fun Battle.isFreeSwitch(context: ActionContext): Boolean {
    val isSwitching = context.action is TurnAction.Switch
    val activeHasFainted = this[context.user].hasFainted()
    return isSwitching && activeHasFainted
}
