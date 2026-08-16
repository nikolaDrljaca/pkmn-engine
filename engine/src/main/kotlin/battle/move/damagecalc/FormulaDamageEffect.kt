package com.drbrosdev.battle.move.damagecalc

import com.drbrosdev.RandomGen
import com.drbrosdev.battle.Battle
import com.drbrosdev.battle.move.CritApplication
import com.drbrosdev.battle.move.MoveContext
import com.drbrosdev.battle.move.MoveCritStage
import com.drbrosdev.battle.move.MoveEffect
import com.drbrosdev.battle.pokemon.*
import com.drbrosdev.battle.pokemon.stats.Stat
import java.util.logging.Logger

private val LOG = Logger.getLogger(ApplyDamage::class.qualifiedName)

/*
Dynamic stat resolution
Dynamic DamageMultiplier application
 */
// decides and delegates to either
// FormulaDamage, CriticalDamage
object ApplyDamage : MoveEffect {
    private val statSelection = DefaultStatSelector()
    private val statResolution = DefaultStatResolution()
    private val damageComputation = DefaultDamageCalculator()
    private val damageApplication = DefaultDamageApplication()

    override fun MoveContext.apply(battle: Battle): Battle {
        // scaffold damage calculation context
        val context = DamageEffectContext(
            battle = battle,
            userId = userId,
            targetId = targetId,
            moveId = moveId,
            critical = shouldApplyCrit(battle)
        )

        // construct and resolve damage calculation pipeline
        return requireNotNull(statSelection.select(context))
            .let { statResolution.resolve(context, it) }
            .let { damageComputation.calculate(context, it) }
            .let { damageApplication.apply(context, it) }
    }

    private fun MoveContext.shouldApplyCrit(battle: Battle): Boolean {
        val user = battle[userId]
        val target = battle[targetId]
        val move = user[moveId]

        // check for abilities which prevent crit - Battle Armor, Shell Armor
        val critPreventingAbilities = setOf(BattleArmor, ShellArmor)
        if (target.ability in critPreventingAbilities) {
            return false
        }

        // TODO: check for environmental effects eg. Lucky Chant

        return when (move.critApplication) {
            // moves that always crit
            is CritApplication.Always -> true
            // crit roll
            is CritApplication.Normal -> {
                val stage = user.effectiveStats.criticalHit.value
                    .plus(move.critApplication.stage.value)
                    .coerceIn(MoveCritStage.MIN, MoveCritStage.MAX)

                val threshold = when (stage) {
                    0 -> 16
                    1 -> 8
                    2 -> 4
                    3 -> 3
                    4 -> 2
                    else -> 16
                }
                RandomGen.nextInt(1, threshold + 1) == 1
            }
        }
    }
}

/**
 * Damage effect from the [VolatileStatus.Confusion] status.
 *
 * Following apply:
 * - 40 power
 * - Always hit accuracy
 * - Cannot Crit
 * - Does not apply STAB
 * - Typeless - no weather modifiers
 * - Unaffected by items like "Life Orb", "Choice X" etc.
 * - Physical move, but ignores [MajorStatus.Burned]
 */
object ApplyConfusionStatusDamage : MoveEffect {
    override fun MoveContext.apply(battle: Battle): Battle {
        // damages is applied to itself
        val user = battle[userId]
        // user must be confused
        require(user.isConfused()) {
            "Cannot apply confusion damage when $userId is not confused! Should not happen!"
        }
        val context = DamageEffectContext(
            battle = battle,
            userId = userId,
            targetId = targetId,
            moveId = moveId,
            critical = false
        )
        val target = battle[userId]
        val movePower = 40
        // resolve relevant stats
        val (attackStat, defenceStat) = ConfusionDamageStatResolution.resolve(context, StatSelection.Default)
        val baseDamage = (2 * user.level.value / 5 + 2) * movePower * attackStat.value / defenceStat.value / 50 + 2
        val multipliers = buildList {
            add(RandomModifier)
        }
        // base times all multipliers
        val finalDamage = multipliers
            .map { it.compute(context) }
            .fold(baseDamage) { damage, multiplier ->
                damage.times(multiplier.value).div(100)
            }
            .coerceAtLeast(1)

        val updatedTarget = target.copy(
            inBattleHp = Stat((target.inBattleHp.value - finalDamage).coerceAtLeast(0))
        )
        LOG.fine { "$userId hurt itself in confusion for $finalDamage" }
        val narrativeLog = buildString {
            appendLine("It hurt itself in confusion!")
            appendLine("${user.name} dealt $finalDamage to itself.")
        }
        return battle
            .narrative(narrativeLog)
            .updateMons(updatedTarget)
    }
}

