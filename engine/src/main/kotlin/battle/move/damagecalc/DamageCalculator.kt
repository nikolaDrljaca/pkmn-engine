package com.drbrosdev.battle.move.damagecalc

fun interface DamageCalculator {
    fun calculate(
        context: DamageEffectContext,
        stats: ResolvedStats,
    ): Int
}

class DefaultDamageCalculator : DamageCalculator {
    override fun calculate(
        context: DamageEffectContext,
        stats: ResolvedStats,
    ): Int {
        val (attackStat, defenceStat) = stats
        val baseDamage = with(context) {
            (2 * user.level.value / 5 + 2) * move.power * attackStat.value / defenceStat.value / 50 + 2
        }
        val multipliers = buildList {
            // regular
            add(StabModifier)
            add(TypeEffectivenessModifier)
            add(WeatherModifier)
            add(RandomModifier)
            add(BurnModifier)
            add(LightScreenModifier)
            add(ReflectModifier)
            // item support
            add(ItemModifier)
            // ability support
            add(AbilityModifier)
            // crit support
            add(CriticalHitModifier)
        }
        // base times all multipliers
        val calculatedDamage = multipliers
            .map { it.compute(context) }
            .fold(baseDamage) { damage, multiplier ->
                damage.times(multiplier.value).div(100)
            }
            .coerceAtLeast(1)

        return calculatedDamage
    }
}
