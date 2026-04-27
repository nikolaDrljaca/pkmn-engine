package com.drbrosdev.battle.move

import com.drbrosdev.RandomGen
import com.drbrosdev.battle.Battle
import com.drbrosdev.battle.Weather
import com.drbrosdev.battle.pokemon.*
import com.drbrosdev.battle.pokemon.stats.*
import java.util.logging.Logger

private val LOG = Logger.getLogger("com.drbrosdev.battle.move.DamageCalculation")

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

fun interface StatResolutionStrategy {
    fun MoveContext.resolve(battle: Battle): Pair<Stat, Stat>
}

val NormalStatResolution = StatResolutionStrategy { battle ->
    val user = battle[userId]
    val target = battle[targetId]
    val move = user[moveId]

    val userStats = user.allStatModifications
        .map { it.compute(StatModificationContext(user, battle)) }
        .fold(user.effectiveStats) { stats, mod -> stats.resolve(mod) }
    val targetStats = target.allStatModifications
        .map { it.compute(StatModificationContext(target, battle)) }
        .fold(target.effectiveStats) { stats, mod -> stats.resolve(mod) }

    when (move.type) {
        MoveType.PHYSICAL -> userStats.attack to targetStats.defence
        MoveType.SPECIAL -> userStats.specialAttack to targetStats.specialDefence
        MoveType.STATUS -> error("Cannot apply stat resolution for STATUS moves!")
    }
}

val CritStatResolution = StatResolutionStrategy { battle ->
    val user = battle[userId]
    val target = battle[targetId]
    val move = user[moveId]

    val userStats = user.allStatModifications
        .map { it.compute(StatModificationContext(user, battle)) }
        // ignore negative stage changes on user
        .filter { mod -> mod.modifiers.values.none { it.isNegativeStage() } }
        .fold(user.effectiveStats) { stats, mod -> stats.resolve(mod) }
    val targetStats = target.allStatModifications
        .map { it.compute(StatModificationContext(target, battle)) }
        // ignore positive stage changes on target
        .filter { mod -> mod.modifiers.values.none { it.isPositiveStage() } }
        .fold(target.effectiveStats) { stats, mod -> stats.resolve(mod) }

    when (move.type) {
        MoveType.PHYSICAL -> userStats.attack to targetStats.defence
        MoveType.SPECIAL -> userStats.specialAttack to targetStats.specialDefence
        MoveType.STATUS -> error("Cannot apply stat resolution for STATUS moves!")
    }
}

val ConfusionDamageStatResolution = StatResolutionStrategy { battle ->
    // TODO: (stat-resolution) implement
    TODO()
}

val PsyshockStatResolution = StatResolutionStrategy { battle ->
    // TODO: (stat-resolution) implement
    // Moves like foul play etc.
    TODO()
}

@JvmInline
value class DamageMultiplier(val value: Int) // hundreds-scaled

fun interface DamageModifier {
    /**
     * Computes a hundreds-scaled damage multiplier or returns null
     * if no multiplier should be applied.
     */
    fun MoveContext.compute(battle: Battle): DamageMultiplier?
}

// always applies
val StabModifier = DamageModifier { battle ->
    val user = battle[userId]
    val move = user[moveId]
    when {
        user.elements.hasAnyOf(move.element) -> DamageMultiplier(150)
        else -> null
    }
}

val TypeEffectivenessModifier = DamageModifier { battle ->
    val move = battle[userId][moveId]
    val target = battle[targetId]
    when (val effectiveness = effectiveness(move.element, target.elements)) {
        Effectiveness.NEUTRAL -> null
        else -> DamageMultiplier(effectiveness.multiplier)
    }
}

// TODO: introduce Environment, allows light screen to apply modifiers

// TODO: move to weather itself
val WeatherModifier = DamageModifier { battle ->
    val move = battle[userId][moveId]
    when (battle.weather) {
        Weather.HARSH_SUN -> when (move.element) {
            Element.FIRE -> DamageMultiplier(150)
            Element.WATER -> DamageMultiplier(50)
            else -> null
        }

        Weather.RAIN -> when (move.element) {
            Element.WATER -> DamageMultiplier(150)
            Element.FIRE -> DamageMultiplier(50)
            else -> null
        }

        else -> null
    }
}

val RandomModifier = DamageModifier { _ ->
    DamageMultiplier(RandomGen.nextInt(85, 101))
}

val CriticalHitModifier = DamageModifier {
    DamageMultiplier(200)
}

val BurnModifier = DamageModifier { battle ->
    val user = battle[userId]
    val move = battle[userId][moveId]
    when {
        move.isPhysical() && user.isBurned() -> DamageMultiplier(50)
        else -> null
    }
}

// items
val ExpertBeltModifier = DamageModifier { battle ->
    // TODO: waiting for item subsystem
    val user = battle[userId]
    val move = battle[userId][moveId]
    val target = battle[targetId]
    when {
        user.item.id.value != "expert-belt" -> return@DamageModifier null
        effectiveness(move.element, target.elements) != Effectiveness.SUPER -> return@DamageModifier null
        else -> DamageMultiplier(120)
    }
}

val LifeOrbModifier = DamageModifier { battle ->
    // TODO: waiting for item subsystem
    when (battle[userId].item.id.value) {
        "life-orb" -> DamageMultiplier(130)
        else -> null
    }
}

// abilities
val FlashFireModifier = DamageModifier { battle ->
    val user = battle[userId]
    val move = battle[userId][moveId]
    // TODO: implement, move to ability subsystem
//    if (user.ability !is FlashFire) return@DamageModifier null
//    if (move.element != Element.FIRE) return@DamageModifier null
//    if (!(user.ability as FlashFire).isActive) return@DamageModifier null
//    DamageMultiplier(150)
    null
}

val TintedLensModifier = DamageModifier { battle ->
    val user = battle[userId]
    val move = battle[userId][moveId]
    val target = battle[targetId]
    // TODO: implement, move to ability subsystem
//    if (user.ability !is TintedLens) return@DamageModifier null
//    if (effectiveness(move.element, target.elements) != Effectiveness.NOT_VERY) return@DamageModifier null
//    DamageMultiplier(200)
    null
}

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

        LOG.fine { "$userId dealt $finalDamage damage to $targetId with $moveId by formula" }

        return battle.updateMons(updatedTarget)
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

        LOG.fine { "$userId dealt $finalDamage damage to $targetId with $moveId by crit!" }

        return battle.updateMons(updatedTarget)
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
object ApplyConfusionStatusDamage: MoveEffect {
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
        val (attackStat, defenceStat) = with(NormalStatResolution) { resolve(battle) }
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
        return battle.updateMons(updatedTarget)
    }
}

