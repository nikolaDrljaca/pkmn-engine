package com.drbrosdev.battle.turn

import com.drbrosdev.battle.Battle
import com.drbrosdev.battle.pokemon.hasFainted

// acts like an orchestrator to describe how a turn is resolved
fun Battle.resolveTurn(turn: Turn): Battle {
    // 1 Determine which action goes first - to build TurnStep pipeline
    val (first, second) = resolveActionOrder(turn)

    // 2 construct TurnStep pipeline
    // This way battles can end in a draw, which is legal
    val steps: List<TurnStep> = when {
        // free switches happen after faints
        isFreeSwitch(first) && isFreeSwitch(second) -> listOf(
            ExecuteActionStep(first),
            ExecuteActionStep(second)
        )

        // first fainted, second is on field
        isFreeSwitch(first) -> listOf(
            ExecuteActionStep(first),
            ApplyEndOfTurnEffects(second)
        )

        // second fainted, first is on field
        isFreeSwitch(second) -> listOf(
            ExecuteActionStep(second),
            ApplyEndOfTurnEffects(first)
        )

        // regular turn execution
        else -> buildList {
            // first turn, both active pokemon switch in
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
            // Switch In Effects if they are applicable
            add(ExecuteSwitchEffects(first))
            add(ExecuteSwitchEffects(second))
            // end of turn effects in order
            add(ApplyEndOfTurnEffects(first))
            add(ApplyEndOfTurnEffects(second))
            // check for conclusion - can be a draw here
            add(CheckConclusion)
            add(HandleTurnCounter)
        }
    }
    // 3 clear logs and execute pipeline
    return copy(turnLog = listOf("Turn $turnCount started."))
        .resolveTurnSteps(steps)
}

private fun Battle.isFreeSwitch(context: ActionContext): Boolean {
    val isSwitching = context.action is TurnAction.Switch
    val activeHasFainted = this[context.user].hasFainted()
    return isSwitching && activeHasFainted
}
