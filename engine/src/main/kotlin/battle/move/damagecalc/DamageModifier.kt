package com.drbrosdev.battle.move.damagecalc

import com.drbrosdev.RandomGen
import com.drbrosdev.battle.environment.TemporaryEffect
import com.drbrosdev.battle.environment.Weather
import com.drbrosdev.battle.move.isPhysical
import com.drbrosdev.battle.move.isSpecialMove
import com.drbrosdev.battle.move.isStatusMove
import com.drbrosdev.battle.pokemon.*
import jdk.jfr.DataAmount


fun interface DamageModifier {
    /**
     * Computes a hundreds-scaled damage multiplier
     */
    fun compute(context: DamageEffectContext): DamageMultiplier
}

// hundreds-scaled
@JvmInline
value class DamageMultiplier(val value: Int) {
    companion object {
        val Neutral = DamageMultiplier(100)
    }
}

// always applies
object StabModifier : DamageModifier {
    override fun compute(context: DamageEffectContext): DamageMultiplier = with(context) {
        when {
            user.elements.hasAnyOf(move.element) -> DamageMultiplier(150)
            else -> DamageMultiplier.Neutral
        }
    }
}

object ItemModifier : DamageModifier {
    override fun compute(context: DamageEffectContext): DamageMultiplier = with(context) {
        when {
            // if critical do nothing
            critical -> DamageMultiplier.Neutral
            // delegate to implementation on item
            else -> with(user.item.damageMultiplier) { compute(context) }
        }
    }
}

object AbilityModifier : DamageModifier {
    // TODO
    // delegate to implementation on ability
    override fun compute(context: DamageEffectContext): DamageMultiplier {
        // consider crit
        return DamageMultiplier.Neutral
    }
}

object TypeEffectivenessModifier : DamageModifier {
    override fun compute(context: DamageEffectContext): DamageMultiplier = with(context) {
        when (val effectiveness = effectiveness(move.element, target.elements)) {
            Effectiveness.NEUTRAL -> DamageMultiplier.Neutral
            else -> DamageMultiplier(effectiveness.multiplier)
        }
    }
}

object WeatherModifier : DamageModifier {
    override fun compute(context: DamageEffectContext): DamageMultiplier = with(context) {
        when (battle.weather) {
            is Weather.HarshSun -> when (move.element) {
                Element.FIRE -> DamageMultiplier(150)
                Element.WATER -> DamageMultiplier(50)
                else -> DamageMultiplier.Neutral
            }

            is Weather.Rain -> when (move.element) {
                Element.WATER -> DamageMultiplier(150)
                Element.FIRE -> DamageMultiplier(50)
                else -> DamageMultiplier.Neutral
            }

            else -> DamageMultiplier.Neutral
        }
    }
}

object RandomModifier : DamageModifier {
    override fun compute(context: DamageEffectContext): DamageMultiplier {
        return DamageMultiplier(RandomGen.nextInt(85, 101))
    }
}

object CriticalHitModifier : DamageModifier {
    override fun compute(context: DamageEffectContext): DamageMultiplier {
        return when {
            context.critical -> DamageMultiplier(200)
            else -> DamageMultiplier(100)
        }
    }
}

object BurnModifier : DamageModifier {
    override fun compute(context: DamageEffectContext): DamageMultiplier = with(context) {
        when {
            move.isPhysical() && user.isBurned() -> DamageMultiplier(50)
            else -> DamageMultiplier.Neutral
        }
    }
}

object ReflectModifier : DamageModifier {
    override fun compute(context: DamageEffectContext): DamageMultiplier = with(context) {
        val hasReflect = battle.temporaryEffects(target.id)
            .filterIsInstance<TemporaryEffect.Reflect>()
            .firstOrNull() != null
        return when {
            // if critical hit, do nothing
            critical -> DamageMultiplier.Neutral
            (move.isPhysical() and hasReflect) -> DamageMultiplier(50)
            else -> DamageMultiplier.Neutral
        }
    }
}

object LightScreenModifier : DamageModifier {
    override fun compute(context: DamageEffectContext): DamageMultiplier = with(context) {
        val targetId = target.id
        val hasLightScreen = battle.temporaryEffects(targetId)
            .filterIsInstance<TemporaryEffect.LightScreen>()
            .firstOrNull() != null
        return when {
            // if critical hit, do nothing
            critical -> DamageMultiplier.Neutral
            (move.isSpecialMove() and hasLightScreen) -> DamageMultiplier(50)
            else -> DamageMultiplier.Neutral
        }
    }
}