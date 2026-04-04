package com.drbrosdev.battle.move

import com.drbrosdev.RandomGen
import com.drbrosdev.battle.Battle
import com.drbrosdev.battle.pokemon.Element
import com.drbrosdev.battle.pokemon.MajorStatus
import com.drbrosdev.battle.pokemon.OwnTempo
import com.drbrosdev.battle.pokemon.Pressure
import com.drbrosdev.battle.pokemon.VolatileStatus
import com.drbrosdev.battle.pokemon.hasAnyOf
import com.drbrosdev.battle.pokemon.stats.Stat
import com.drbrosdev.battle.pokemon.stats.StatModification
import com.drbrosdev.battle.pokemon.stats.StatModifier
import com.drbrosdev.battle.pokemon.stats.increaseStageBy

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
        // 1 PP reduction happens always
        val afterPP = with(ReducePowerPoints) { apply(battle) }
        // 2 check preconditions
        val preconditionResult = resolvePreconditions(afterPP)
        // 3 execute effects
        return when (preconditionResult) {
            // a precondition has triggered, no effects are applied
            MovePrecondition.Result.TRIGGER -> afterPP

            // all preconditions passed - execute move
            MovePrecondition.Result.PASS -> effects.fold(afterPP) { current, effect ->
                with(effect) {
                    apply(current)
                }
            }
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

val ReducePowerPoints = MoveEffect { battle ->
    val targetMon = battle[targetId]
    val userMon = battle[userId]
    val move = userMon[moveId]
    val reduction = if (targetMon.ability == Pressure) 2 else 1
    val updatedUser = userMon.copy(
        moves = userMon.moves.map { m ->
            if (m == move) m.copy(powerPoints = (m.powerPoints - reduction).coerceAtLeast(0))
            else m
        }
    )
    battle.updateMons(updatedUser)
}

val ApplyFormulaDamage = MoveEffect { battle ->
    // TODO
    // type effectiveness
    // STAB
    // NOTE: To add berry support you'd need to hook in here
    // or after a MoveEffect executes since berries usually trigger before/after move execution
    battle
}

val ApplyDirectDamage = MoveEffect { battle ->
    // NOTE support for moves like dragon rage, sonic boom
    // take power of a move and apply it directly as damage
    val targetMon = battle[targetId]
    val move = battle[userId][moveId]
    val targetHp = (targetMon.inBattleHp.value - move.power).coerceAtLeast(0)
    battle.updateMons(targetMon.copy(inBattleHp = Stat(targetHp)))
}

fun ApplyStatModification(statModification: StatModification) = MoveEffect { battle ->
    val targetMon = battle[targetId]
    val updatedTarget = targetMon.copy(statModifications = buildList {
        addAll(targetMon.statModifications)
        add(statModification)
    })
    battle.updateMons(updatedTarget)
}

class ApplyAccuracyChange(private val stage: StatModifier.Stage) : MoveEffect {
    override fun MoveContext.apply(battle: Battle): Battle {
        val target = battle[targetId]
        val updatedEffectiveStats = target.effectiveStats.copy(
            accuracy = target.effectiveStats.accuracy.increaseStageBy(stage)
        )
        val updatedTarget = target.copy(
            effectiveStats = updatedEffectiveStats
        )
        return battle.updateMons(updatedTarget)
    }
}

class ApplyEvasionChange(private val stage: StatModifier.Stage) : MoveEffect {
    override fun MoveContext.apply(battle: Battle): Battle {
        val target = battle[targetId]
        val updatedEffectiveStats = target.effectiveStats.copy(
            accuracy = target.effectiveStats.evasion.increaseStageBy(stage)
        )
        val updatedTarget = target.copy(
            effectiveStats = updatedEffectiveStats
        )
        return battle.updateMons(updatedTarget)
    }
}

class ApplyStatusCondition(
    private val percentage: Percentage,
    private val condition: MajorStatus
) : MoveEffect {
    override fun MoveContext.apply(battle: Battle): Battle {
        val targetMon = battle[targetId]
        // a mon with a status cannot receive another
        if (targetMon.majorStatus != MajorStatus.Normal) return battle
        /*
        Fire types cannot be burned
        Electric types cannot be paralyzed
        Ice types cannot be frozen
         */
        val immune = when (condition) {
            is MajorStatus.Burned -> targetMon.elements.hasAnyOf(Element.FIRE)
            is MajorStatus.Frozen -> targetMon.elements.hasAnyOf(Element.ICE)
            is MajorStatus.Paralyzed -> targetMon.elements.hasAnyOf(Element.ELECTRIC)
            is MajorStatus.Poisoned, is MajorStatus.BadlyPoisoned ->
                targetMon.elements.hasAnyOf(Element.POISON, Element.STEEL)
            else -> false
        }
        if (immune) return battle

        // probability check
        if (RandomGen.nextInt(1, 101) > percentage.value) return battle
        // apply major status condition
        val updatedTarget = targetMon.copy(majorStatus = condition)
        // TODO We need to account for abilities which prevent status conditions
        // EG: Water Veil prevents burn effects etc, Insomnia prevents sleep etc
        return battle.updateMons(updatedTarget)
    }
}

class ApplyVolatileStatusCondition(
    private val percentage: Percentage,
    private val volatileStatus: VolatileStatus
) : MoveEffect {
    override fun MoveContext.apply(battle: Battle): Battle {
        val targetMon = battle[targetId]
        // OwnTempo support
        if (targetMon.ability == OwnTempo && volatileStatus is VolatileStatus.Confusion) return battle
        // TODO Add other abilities which prevent volatile status changes
        // a mon cannot receive a volatile status it already has
        if (targetMon.volatileStatus.any { it::class == volatileStatus::class }) return battle
        // probability check
        if (RandomGen.nextInt(1, 101) > percentage.value) return battle
        // apply volatile status
        val updatedTarget = targetMon.copy(
            volatileStatus = targetMon.volatileStatus + volatileStatus
        )
        return battle.updateMons(updatedTarget)
    }
}
