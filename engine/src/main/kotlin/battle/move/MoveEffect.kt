package com.drbrosdev.battle.move

import com.drbrosdev.battle.Battle
import com.drbrosdev.battle.pokemon.MajorStatus
import com.drbrosdev.battle.pokemon.VolatileStatus
import com.drbrosdev.battle.pokemon.stats.StatModification

/*
Pipeline Design pattern
Execute-all pipeline.
All steps run unconditionally.
Implementations decide to return a new state of the battle
*/

fun interface MoveEffect {
    fun MoveContext.apply(battle: Battle): Battle
}

/*
To be used by most move implementations
*/
class SequenceMoveEffect(private val effects: List<MoveEffect>) : MoveEffect {
    override fun MoveContext.apply(battle: Battle): Battle {
        val preconditionResult = resolvePreconditions(battle)
        return when (preconditionResult) {
            // all preconditions passed - execute move
            MovePrecondition.Result.PASS -> effects.fold(battle) { current, effect ->
                with(effect) {
                    apply(current)
                }
            }

            // a precondition has triggered, no effects are applied
            MovePrecondition.Result.TRIGGER -> battle
        }
    }
}

@JvmInline
value class Percentage(val value: Int = 100) {
    init {
        require(value in 1..100)
    }
}

val NoEffect = MoveEffect { it }

val ApplyFormulaDamage = MoveEffect { battle ->
    // TODO
    // type effectiveness
    // STAB
    // NOTE: To add berry support you'd need to hook in here
    // or after a MoveEffect executes since berries usually trigger before/after move execution
    battle
}

val ApplyDirectDamage = MoveEffect { battle ->
    // TODO things like dragon rage, sonic boom
    battle
}

fun ApplyStatModification(statModification: StatModification) = MoveEffect { battle ->
    when(resolvePreconditions(battle)) {
        MovePrecondition.Result.PASS -> {
            val updatedTarget = target.copy(statModifications = buildList {
                addAll(target.statModifications)
                add(statModification)
            })
            battle.updateMons(updatedTarget)
        }

        MovePrecondition.Result.TRIGGER -> battle
    }
}

class ApplyStatusCondition(
    private val percentage: Percentage,
    private val condition: MajorStatus
) : MoveEffect {
    override fun MoveContext.apply(battle: Battle): Battle {
        TODO("Not yet implemented")
    }
}

class ApplyVolatileStatusCondition(
    private val percentage: Percentage,
    private val volatileStatus: VolatileStatus
) : MoveEffect {
    override fun MoveContext.apply(battle: Battle): Battle {
        TODO("Not yet implemented")
    }
}
