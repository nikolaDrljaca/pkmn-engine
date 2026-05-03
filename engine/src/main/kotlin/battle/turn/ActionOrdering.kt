package com.drbrosdev.battle.turn

import com.drbrosdev.RandomGen
import com.drbrosdev.battle.Battle
import com.drbrosdev.battle.abilities
import com.drbrosdev.battle.move.Move
import com.drbrosdev.battle.move.MoveId
import com.drbrosdev.battle.pokemon.Pokemon
import com.drbrosdev.battle.pokemon.PokemonId
import com.drbrosdev.battle.pokemon.computeInBattleStats
import java.util.logging.Logger

private val LOG = Logger.getLogger(TurnActionOrderRule::class.qualifiedName)

interface TurnActionOrderRule {
    fun Turn.determine(battle: Battle): OrderingResult
}

// the design pattern is Chain of Responsibility
// effectively like request middleware - either process/transform or passthrough to next
// except here the chain breaks once the first thing is resolved
sealed interface OrderingResult {
    data class Resolved(
        val first: ActionContext,
        val second: ActionContext
    ) : OrderingResult {
        constructor(
            selection1: Pair<PokemonId, TurnAction>,
            selection2: Pair<PokemonId, TurnAction>
        ) : this(
            first = ActionContext(
                user = selection1.first,
                target = selection2.first,
                action = selection1.second
            ),
            second = ActionContext(
                user = selection2.first,
                target = selection1.first,
                action = selection2.second
            ),
        )
    }

    data object Deferred : OrderingResult
}

// Switch actions always go first unless Pursuit is active
private object SwitchRule : TurnActionOrderRule {
    override fun Turn.determine(battle: Battle): OrderingResult {
        val isSwitch1 = selection1.second is TurnAction.Switch
        val isSwitch2 = selection2.second is TurnAction.Switch
        return when {
            isSwitch1 && isSwitch2 -> OrderingResult.Deferred

            isSwitch1 -> OrderingResult.Resolved(selection1, selection2)

            isSwitch2 -> OrderingResult.Resolved(selection2, selection1)

            else -> OrderingResult.Deferred
        }
    }
}

private object PursuitRule : TurnActionOrderRule {
    override fun Turn.determine(battle: Battle): OrderingResult {
        val pursuit1 = selection1.second is TurnAction.MoveSelected
                && (selection1.second as TurnAction.MoveSelected).move.id == "pursuit"
                && selection2.second is TurnAction.Switch

        val pursuit2 = selection2.second is TurnAction.MoveSelected
                && (selection2.second as TurnAction.MoveSelected).move.id == "pursuit"
                && selection1.second is TurnAction.Switch

        return when {
            pursuit1 -> OrderingResult.Resolved(selection1, selection2)

            pursuit2 -> OrderingResult.Resolved(selection2, selection1)

            else -> OrderingResult.Deferred
        }
    }
}

// Priority always resolves if moves differ
private object MovePriorityRule : TurnActionOrderRule {
    override fun Turn.determine(battle: Battle): OrderingResult {
        val move1 = battle[selection1.first][move(selection1.second)]
        val move2 = battle[selection2.first][move(selection2.second)]
        return when {
            move1.priority.value > move2.priority.value ->
                OrderingResult.Resolved(selection1, selection2)

            move2.priority.value > move1.priority.value ->
                OrderingResult.Resolved(selection2, selection1)

            // prios are equal - defer to speed
            else -> OrderingResult.Deferred
        }
    }

    private fun move(action: TurnAction): MoveId = when (action) {
        is TurnAction.MoveSelected -> action.move
        is TurnAction.Switch -> error("Cannot resolve Switch action in MovePriorityRule!")
    }
}

// Quick Claw - only fires if priority didn't resolve
private object QuickClawRule : TurnActionOrderRule {
    // check held items on each pokemon, random chance
    override fun Turn.determine(battle: Battle): OrderingResult {
        val (p1, a1) = selection1
        val (p2, a2) = selection2
        val mon1 = battle[p1]
        val mon2 = battle[p2]
        val claw1 = (mon1.item.id.value == "quick-claw")
            .and(RandomGen.nextInt(1, 101) <= 20)
        val claw2 = (mon2.item.id.value == "quick-claw")
            .and(RandomGen.nextInt(1, 101) <= 20)
        return when {
            // both rolled, so its random
            claw1 && claw2 -> when {
                RandomGen.nextBoolean() -> OrderingResult.Resolved(selection1, selection2)
                else -> OrderingResult.Resolved(selection2, selection1)
            }

            claw1 -> OrderingResult.Resolved(selection1, selection2)
            claw2 -> OrderingResult.Resolved(selection2, selection1)
            else -> OrderingResult.Deferred
        }
    }
}

// Trick Room - only fires if neither priority nor quick claw resolved
private object TrickRoomRule : TurnActionOrderRule {
    // check battle conditions for trick room
    // resolves by reversing speed order, or defers
    // TODO: impl - waiting for TrickRoom / Environment support
    override fun Turn.determine(battle: Battle): OrderingResult {
        return OrderingResult.Deferred
    }
}

// Speed comparison
private object SpeedRule : TurnActionOrderRule {
    override fun Turn.determine(battle: Battle): OrderingResult {
        // compare resolved speed stats
        val (p1, a1) = selection1
        val (p2, a2) = selection2
        val mon1 = battle[p1]
        val mon2 = battle[p2]
        val speed1 = mon1.computeInBattleStats(battle).speed.value
        val speed2 = mon2.computeInBattleStats(battle).speed.value
        return when {
            speed1 > speed2 -> OrderingResult.Resolved(selection1, selection2)

            speed2 > speed1 -> OrderingResult.Resolved(selection2, selection1)

            else -> OrderingResult.Deferred
        }
    }
}

// Terminal - always resolves via coin flip
private object SpeedTieRule : TurnActionOrderRule {
    override fun Turn.determine(battle: Battle): OrderingResult {
        return when {
            RandomGen.nextBoolean() -> OrderingResult.Resolved(selection1, selection2)

            else -> OrderingResult.Resolved(selection2, selection1)
        }
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

    /*
    NOTE:
    Abilities might have to insert themselves at different positions in the sequence.
    To solve for this it's best to introduce a TurnActionOrderRule.priority: Int property
    and here the sequence can be sorted by it. That way each ability can define its own prio,
    and existing rules can use 'static' ones.
     */
    val applicableRules = sequence {
        yield(PursuitRule)
        yieldAll(abilities().flatMap { it.orderingRules })
        yield(SwitchRule)
        yield(MovePriorityRule)
        yield(QuickClawRule)
        yield(TrickRoomRule)
        yield(SpeedRule)
        yield(SpeedTieRule)
    }

    for (rule in applicableRules) {
        val result = with(rule) {
            turn.determine(battle)
        }
        when (result) {
            is OrderingResult.Resolved -> {
                LOG.fine { "${result.first.user.id} moves first against ${result.second.user.id} - resolved by ${rule::class.simpleName}" }
                return result.first to result.second
            }

            OrderingResult.Deferred -> continue
        }
    }
    error("TurnActionOrderRule sequence must have a terminal rule!")
}

