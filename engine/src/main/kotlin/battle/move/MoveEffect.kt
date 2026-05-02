package com.drbrosdev.battle.move

import com.drbrosdev.RandomGen
import com.drbrosdev.battle.Battle
import com.drbrosdev.battle.environment.EnvironmentUnit
import com.drbrosdev.battle.move.damagecalc.ApplyConfusionStatusDamage
import com.drbrosdev.battle.pokemon.*
import com.drbrosdev.battle.pokemon.statModifications
import com.drbrosdev.battle.pokemon.stats.Stat
import com.drbrosdev.battle.pokemon.stats.StatModification
import com.drbrosdev.battle.pokemon.stats.StatModifier
import com.drbrosdev.battle.pokemon.stats.increaseStageBy
import java.util.logging.Logger
import kotlin.text.get

/*
Pipeline Design pattern
Execute-all pipeline.
All steps run unconditionally.
Implementations decide to return a new state of the battle
*/

private val LOG = Logger.getLogger(MoveEffect::class.qualifiedName)

fun interface MoveEffect {
    fun MoveContext.apply(battle: Battle): Battle

    companion object {
        val NoEffect = MoveEffect { it }
    }
}

/**
 * Applies a sequence of move effects if all [MovePrecondition] resolve to [MovePrecondition.Result.PASS].
 * To be used by most [Move] implementations.
 *
 * Also applies [VolatileStatus.Confusion] self damage if triggered.
 */
class SequenceMoveEffect(private val effects: List<MoveEffect>) : MoveEffect {
    override fun MoveContext.apply(battle: Battle): Battle {
        // 1 PP reduction happens always
        val afterPP = with(ReducePowerPoints) { apply(battle) }
        // 2 check preconditions
        val preconditionResult = resolvePreconditions(afterPP)
        // 3 execute effects
        return when (preconditionResult) {
            // all preconditions passed - execute move
            MovePrecondition.Result.PASS -> effects
                .plus(battle[userId].item.afterMoveEffect)
                .fold(afterPP) { current, effect ->
                    with(effect) {
                        apply(current)
                    }
                }
            // Precondition already checks if confusion should trigger
            // means you fail to execute AND you hit yourself
            MovePrecondition.Result.CONFUSED -> with(ApplyConfusionStatusDamage) { apply(battle) }
            // a precondition has triggered, no effects are applied
            MovePrecondition.Result.TRIGGER -> afterPP
            MovePrecondition.Result.MISS -> afterPP
            MovePrecondition.Result.PROTECTED -> afterPP
            MovePrecondition.Result.IMMUNE -> afterPP
            MovePrecondition.Result.PARALYZED -> afterPP
            MovePrecondition.Result.FROZEN -> afterPP
            MovePrecondition.Result.ASLEEP -> afterPP
            MovePrecondition.Result.INFATUATED -> afterPP
        }
    }
}

@JvmInline
value class Percentage(val value: Int = 100) {
    init {
        require(value in 1..100)
    }
}

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

/**
 * Support for moves like "Dragon Rage", "Sonic Boom".
 *
 * Applies [directDamage] directly.
 */
class ApplyDirectDamage(private val directDamage: Int) : MoveEffect {
    override fun MoveContext.apply(battle: Battle): Battle {
        val targetMon = battle[targetId]
        val targetHp = (targetMon.inBattleHp.value - directDamage).coerceAtLeast(0)
        LOG.fine { "$userId dealt $directDamage damage to $targetId with $moveId by direct" }
        return battle.updateMons(targetMon.copy(inBattleHp = Stat(targetHp)))
    }
}

class ApplyStatModification(private val statModification: StatModification) : MoveEffect {
    override fun MoveContext.apply(battle: Battle): Battle = with(battle[targetId]) {
        val statMods = buildList {
            addAll(statModifications)
            add(statModification)
        }
        LOG.fine { "$userId lowers stats of $targetId with $moveId" }
        battle.updateMons(copy(statModifications = statMods))
    }
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
        // TODO: We need to account for abilities which prevent status conditions
        // EG: Water Veil prevents burn effects etc, Insomnia prevents sleep etc
        return battle.updateMons(updatedTarget)
    }
}

class ApplyVolatileStatusCondition(
    private val percentage: Percentage,
    private val volatileStatusFactory: (turnCount: Int) -> VolatileStatus
) : MoveEffect {
    override fun MoveContext.apply(battle: Battle): Battle {
        val targetMon = battle[targetId]
        val volatileStatus = volatileStatusFactory(battle.turnCount)
        // OwnTempo support
        if (targetMon.ability == OwnTempo && volatileStatus is VolatileStatus.Confusion) return battle
        // TODO: Add other abilities which prevent volatile status changes
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

class ApplyEnvironmentUnit(private val unit: EnvironmentUnit): MoveEffect {
    override fun MoveContext.apply(battle: Battle): Battle {
        return battle.updateEnvironment(targetId, unit)
    }
}