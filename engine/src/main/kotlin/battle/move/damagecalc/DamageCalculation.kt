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

    override fun MoveContext.apply(battle: Battle): Battle {
        val effect = when {
            shouldApplyCrit(battle) -> ApplyCriticalDamage
            else -> ApplyNormalDamage
        }
        return with(effect) { apply(battle) }
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

// TODO: introduce Environment, allows light screen to apply modifiers

object ApplyNormalDamage : MoveEffect {
    override fun MoveContext.apply(battle: Battle): Battle {
        val user = battle[userId]
        val target = battle[targetId]
        val move = battle[userId][moveId]

        require(move.power != 0) { "Cannot apply formula damage with 0 power move!" }

        // resolve relevant stats
        val (attackStat, defenceStat) = with(NormalStatResolution) { resolve(battle) }
        // base damage
        val baseDamage = (2 * user.level.value / 5 + 2) * move.power * attackStat.value / defenceStat.value / 50 + 2
        // multipliers
        val multipliers = buildList {
            add(StabModifier)
            add(TypeEffectivenessModifier)
            add(WeatherModifier)
            add(RandomModifier)
            add(BurnModifier)
            add(LightScreenModifier)
            add(ReflectModifier)
            // item support (Eg Black Glasses etc)
            add(user.item.damageMultiplier)
            // TODO: Add ability support
        }

        // base times all multipliers
        val finalDamage = multipliers
            .mapNotNull { with(it) { compute(battle) } }
            .fold(baseDamage) { damage, multiplier ->
                damage.times(multiplier.value).div(100)
            }
            .coerceAtLeast(1)

        val updatedTarget = target.copy(
            inBattleHp = Stat((target.inBattleHp.value - finalDamage).coerceAtLeast(0))
        )

        // handle logging
        // get effectiveness for narrative logging
        val effectiveness = effectiveness(move.element, target.elements)
        LOG.fine { "$userId dealt $finalDamage($effectiveness) damage to $targetId with $moveId by formula" }
        val narrativeLog = buildString {
            if (effectiveness.narrativeMessage.isNotBlank())
                appendLine(effectiveness.narrativeMessage)
            appendLine("The opposing ${target.name} lost $finalDamage health.")
        }

        return battle
            .narrative(narrativeLog)
            .updateMons(updatedTarget)
    }
}

object ApplyCriticalDamage : MoveEffect {
    override fun MoveContext.apply(battle: Battle): Battle {
        val user = battle[userId]
        val target = battle[targetId]
        val move = battle[userId][moveId]

        require(move.power != 0) { "Cannot apply formula damage with 0 power move!" }

        // resolve relevant stats
        val (attackStat, defenceStat) = with(CritStatResolution) { resolve(battle) }
        // base damage
        val baseDamage = (2 * user.level.value / 5 + 2) * move.power * attackStat.value / defenceStat.value / 50 + 2
        // multipliers
        val multipliers = buildList {
            add(StabModifier)
            add(TypeEffectivenessModifier)
            add(WeatherModifier)
            add(RandomModifier)
            add(BurnModifier)
            add(CriticalHitModifier)
            // TODO Add item and ability support
        }
        // base times all multipliers
        val finalDamage = multipliers
            .mapNotNull { with(it) { compute(battle) } }
            .fold(baseDamage) { damage, multiplier ->
                damage.times(multiplier.value).div(100)
            }
            .coerceAtLeast(1)

        val updatedTarget = target.copy(
            inBattleHp = Stat((target.inBattleHp.value - finalDamage).coerceAtLeast(0))
        )

        // handle logging
        // get effectiveness for narrative logging
        LOG.fine { "$userId dealt $finalDamage damage to $targetId with $moveId by crit" }
        val narrativeLog = buildString {
            appendLine("It's a critical hit!")
            appendLine("The opposing ${target.name} lost $finalDamage health.")
        }

        return battle
            .narrative(narrativeLog)
            .updateMons(updatedTarget)
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
        val target = battle[userId]
        val movePower = 40
        // resolve relevant stats
        val (attackStat, defenceStat) = with(ConfusionDamageStatResolution) { resolve(battle) }
        val baseDamage = (2 * user.level.value / 5 + 2) * movePower * attackStat.value / defenceStat.value / 50 + 2
        val multipliers = buildList {
            add(RandomModifier)
        }
        // base times all multipliers
        val finalDamage = multipliers
            .mapNotNull { with(it) { compute(battle) } }
            .fold(baseDamage) { damage, multiplier ->
                damage.times(multiplier.value).div(100)
            }
            .coerceAtLeast(1)

        val updatedTarget = target.copy(
            inBattleHp = Stat((target.inBattleHp.value - finalDamage).coerceAtLeast(0))
        )
        LOG.fine { "$userId hurt itself in confusion for $finalDamage" }
        val narrativeLog = buildString {
            appendLine("${user.name} hurt itself in confusion!")
            appendLine("${user.name} dealt $finalDamage to itself.")
        }
        return battle
            .narrative(narrativeLog)
            .updateMons(updatedTarget)
    }
}

