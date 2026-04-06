package com.drbrosdev.battle.move

import com.drbrosdev.RandomGen
import com.drbrosdev.battle.Battle
import com.drbrosdev.battle.Weather
import com.drbrosdev.battle.pokemon.*
import com.drbrosdev.battle.pokemon.stats.Stat
import com.drbrosdev.battle.pokemon.stats.StatModification
import com.drbrosdev.battle.pokemon.stats.StatModifier
import com.drbrosdev.battle.pokemon.stats.increaseStageBy
import java.util.logging.Logger

/*
Pipeline Design pattern
Execute-all pipeline.
All steps run unconditionally.
Implementations decide to return a new state of the battle
*/

private val LOG = Logger.getLogger(MoveEffect::class.qualifiedName)

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

/**
 * General formula damage application.
 *
 * For moves such as Psyshock which change how stas are resolved,
 * implement anew with [MoveEffect].
 */
val ApplyFormulaDamage = MoveEffect { battle ->
    val user = battle[userId]
    val target = battle[targetId]
    val move = battle[userId][moveId]

    require(move.power != 0) { "Cannot apply formula damage with 0 power move!" }

    // check to land crit
    if (shouldMoveCrit(user, move)) {
        return@MoveEffect with(ApplyCriticalHitDamage) { apply(battle) }
    }
    // apply regular damage calc
    val userStats = user.computeInBattleStats(battle)
    val targetStats = target.computeInBattleStats(battle)

    // resolve relevant stats
    val (attackStat, defenceStat) = when (move.type) {
        MoveType.PHYSICAL -> userStats.attack to targetStats.defence
        MoveType.SPECIAL -> userStats.specialAttack to targetStats.specialDefence
        MoveType.STATUS -> error("Attempting to apply formula damage for a STATUS move!")
    }
    // multipliers
    val stabMultiplier = if (user.elements.hasAnyOf(move.element)) 150 else 100
    val typeMultiplier = effectiveness(move.element, target.elements).multiplier
    val burnMultiplier = when {
        move.isPhysicalMove() && user.isBurned() -> 50
        else -> 100
    }
    val weatherMultiplier = when (battle.weather) {
        Weather.HARSH_SUN -> when (move.element) {
            Element.WATER -> 50
            Element.FIRE -> 150
            else -> 100
        }
        Weather.RAIN -> when (move.element) {
            Element.WATER -> 150
            Element.FIRE -> 50
            else -> 100
        }
        else -> 100
    }
    val randomMultiplier = RandomGen.nextInt(85, 101)

    val baseDamage = (2 * user.level.value / 5 + 2) * move.power * attackStat.value / defenceStat.value / 50 + 2

    val finalDamage = baseDamage
        .times(stabMultiplier).div(100)
        .times(typeMultiplier).div(100)
        .times(weatherMultiplier).div(100)
        .times(burnMultiplier).div(100)
        .times(randomMultiplier).div(100)
        .coerceAtLeast(1)

    val updatedTarget = target.copy(
        inBattleHp = Stat((target.inBattleHp.value - finalDamage).coerceAtLeast(0))
    )

    LOG.fine { "$userId dealt $finalDamage damage to $targetId with $moveId by formula" }

    // NOTE: To add berry support you'd need to hook in here
    // or after a MoveEffect executes since berries usually trigger before/after move execution

    battle.updateMons(updatedTarget)
}

val ApplyDirectDamage = MoveEffect { battle ->
    // NOTE support for moves like dragon rage, sonic boom
    // take power of a move and apply it directly as damage
    val targetMon = battle[targetId]
    val move = battle[userId][moveId]
    val targetHp = (targetMon.inBattleHp.value - move.power).coerceAtLeast(0)
    LOG.fine { "$userId dealt ${move.power} damage to $targetId with $moveId by direct" }
    battle.updateMons(targetMon.copy(inBattleHp = Stat(targetHp)))
}

val ApplyCriticalHitDamage =  MoveEffect { battle ->
    // same as ApplyFormulaDamage but:
    // - ignores negative attack stages on user
    // - ignores positive defence stages on target
    // - ignores burn penalty
    // - applies 200 critical modifier
    val user = battle[userId]
    val target = battle[targetId]
    val move = battle[userId][moveId]

    val userStats = user.computeInBattleStatsForCrit(battle)
    val targetStats = target.computeInBattleStatsForCrit(battle)

    // resolve relevant stats
    val (attackStat, defenceStat) = when (move.type) {
        MoveType.PHYSICAL -> userStats.attack to targetStats.defence
        MoveType.SPECIAL -> userStats.specialAttack to targetStats.specialDefence
        MoveType.STATUS -> error("Attempting to apply crit damage for a STATUS move!")
    }
    // multipliers
    val stabMultiplier = if (user.elements.hasAnyOf(move.element)) 150 else 100
    val typeMultiplier = effectiveness(move.element, target.elements).multiplier
    // ignores burn multiplier
    val weatherMultiplier = when (battle.weather) {
        Weather.HARSH_SUN -> when (move.element) {
            Element.WATER -> 50
            Element.FIRE -> 150
            else -> 100
        }
        Weather.RAIN -> when (move.element) {
            Element.WATER -> 150
            Element.FIRE -> 50
            else -> 100
        }
        else -> 100
    }
    val randomMultiplier = RandomGen.nextInt(85, 101)

    val baseDamage = (2 * user.level.value / 5 + 2) * move.power * attackStat.value / defenceStat.value / 50 + 2

    val finalDamage = baseDamage
        .times(stabMultiplier).div(100)
        .times(typeMultiplier).div(100)
        .times(weatherMultiplier).div(100)
        .times(2) // Crit multiplier
        .times(randomMultiplier).div(100)
        .coerceAtLeast(1)

    val updatedTarget = target.copy(
        inBattleHp = Stat((target.inBattleHp.value - finalDamage).coerceAtLeast(0))
    )

    LOG.fine { "$userId dealt $finalDamage damage to $targetId with $moveId by crit formula" }

    // NOTE: To add berry support you'd need to hook in here
    // or after a MoveEffect executes since berries usually trigger before/after move execution

    battle.updateMons(updatedTarget)
    battle
}

fun shouldMoveCrit(user: Pokemon, move: Move): Boolean {
    val stage = (user.effectiveStats.criticalHit.value + move.critStage.value)
        .coerceIn(0, 4)
    val threshold = when (stage) {
        0 -> 16
        1 -> 8
        2 -> 4
        3 -> 3
        4 -> 2
        else -> 16
    }
    return RandomGen.nextInt(1, threshold + 1) == 1
}

fun ApplyStatModification(statModification: StatModification) = MoveEffect { battle ->
    with(battle[targetId]) {
        val statMods = buildList {
            addAll(statModifications)
            add(statModification)
        }
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
