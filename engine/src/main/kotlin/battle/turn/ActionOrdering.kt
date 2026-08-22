package com.drbrosdev.battle.turn

import com.drbrosdev.RandomGen
import com.drbrosdev.battle.Battle
import com.drbrosdev.battle.abilities
import com.drbrosdev.battle.move.Move
import com.drbrosdev.battle.pokemon.computeInBattleStats

object SwitchRuleComparator : Comparator<TurnAction> {
    // 0 - can't decide, go to next
    // +/-1 - decided, use me as comparator
    override fun compare(
        o1: TurnAction?,
        o2: TurnAction?
    ): Int {
        val isSwitch1 = o1 is TurnAction.Switch
        val isSwitch2 = o2 is TurnAction.Switch
        return when {
            isSwitch1 && isSwitch2 -> 0

            isSwitch1 -> -1

            isSwitch2 -> 1

            else -> 0
        }
    }
}

object PursuitRuleComparator : Comparator<TurnAction> {
    override fun compare(
        o1: TurnAction?,
        o2: TurnAction?
    ): Int {
        val pursuit1 = o1 is TurnAction.MoveSelected
                && o1.move.id.id == "pursuit"
                && o2 is TurnAction.Switch

        val pursuit2 = o2 is TurnAction.MoveSelected
                && o2.move.id.id == "pursuit"
                && o1 is TurnAction.Switch
        return when {
            pursuit1 -> -1

            pursuit2 -> 1

            else -> 0
        }
    }
}

object MovePriorityRuleComparator : Comparator<TurnAction> {
    override fun compare(
        o1: TurnAction?,
        o2: TurnAction?
    ): Int {
        if (o1 is TurnAction.Switch && o2 is TurnAction.Switch) {
            return 0
        }

        val move1 = extractMove(o1)
        val move2 = extractMove(o2)
        return when {
            move1.priority.value > move2.priority.value -> -1

            move2.priority.value > move1.priority.value -> 1
            // prios are equal - defer to speed
            else -> 0
        }
    }

    private fun extractMove(action: TurnAction?): Move = when (action) {
        is TurnAction.MoveSelected -> action.move
        else -> error("")
    }
}

class QuickClawRuleComparator(
    val coinFlip: Boolean,
    val roll: Boolean
) : Comparator<TurnAction> {
    override fun compare(
        o1: TurnAction,
        o2: TurnAction
    ): Int {
        val claw1 = rolled(o1)
        val claw2 = rolled(o2)
        return when {
            claw1 && claw2 -> if (coinFlip) -1 else 1
            claw1 -> -1
            claw2 -> 1
            else -> 0
        }
    }

    fun rolled(sel: TurnAction) = sel.activePokemon.item.id.value == "quick-claw" && roll
}

class TrickRoomRuleComparator(private val battle: Battle) : Comparator<TurnAction> {
    override fun compare(
        o1: TurnAction,
        o2: TurnAction
    ): Int {
        // TODO: impl waiting for TrickRoom / Environment support
        return 0
    }
}

class SpeedRuleComparator(private val battle: Battle) : Comparator<TurnAction> {
    override fun compare(
        o1: TurnAction,
        o2: TurnAction
    ): Int {
        val speed1 = o1.activePokemon.computeInBattleStats(battle).speed.value
        val speed2 = o2.activePokemon.computeInBattleStats(battle).speed.value
        return when {
            speed1 > speed2 -> -1

            speed2 > speed1 -> 1

            else -> 0
        }
    }
}

class SpeedTieRuleComparator(val coinToss: Boolean) : Comparator<TurnAction> {
    override fun compare(
        o1: TurnAction,
        o2: TurnAction
    ): Int = when {
        coinToss -> -1
        else -> 1
    }

}

/*
Chain of Responsibility pattern.
Each ActionOrderRule decides to either handle (return) or pass along.
The first Resolved result ends the chain.
 */
fun Battle.resolveActionOrder(
    turn: Turn,
): Pair<ActionContext, ActionContext> {
    val battle = this
    // pass this into QuickClaw and SpeedTie to get a stable sort
    val coinFlip = RandomGen.nextBoolean()
    val roll = RandomGen.nextBoolean()
    val rules = buildList {
        add(PursuitRuleComparator)
        // TODO add ability support
        // they might need environment support
        addAll(abilities().flatMap { it.orderingRules })
        add(SwitchRuleComparator)
        add(MovePriorityRuleComparator)
        add(QuickClawRuleComparator(coinFlip, roll))
        add(TrickRoomRuleComparator(battle))
        add(SpeedRuleComparator(battle))
        add(SpeedTieRuleComparator(coinFlip))
    }
    val comparator = rules.reduce { acc, comparator -> acc.then(comparator) }
    val ordered = turn.actions.sortedWith(comparator)
    // TODO how to use ActionContext now?
    // simple, for now ordered[0] is first and ordered[2] is second
    val selection1 = ordered[0]
    val selection2 = ordered[1]
    val first = ActionContext(
        user = battle.team(selection1.activePokemon.id).id,
        target = battle.team(selection2.activePokemon.id).id,
        action = selection1
    )

    val second = ActionContext(
        user = battle.team(selection2.activePokemon.id).id,
        target = battle.team(selection1.activePokemon.id).id,
        action = selection2
    )
    return first to second
}

