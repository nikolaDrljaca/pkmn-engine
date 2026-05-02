package com.drbrosdev.battle.move.damagecalc

import com.drbrosdev.RandomGen
import com.drbrosdev.battle.Battle
import com.drbrosdev.battle.Weather
import com.drbrosdev.battle.move.MoveContext
import com.drbrosdev.battle.move.isPhysical
import com.drbrosdev.battle.pokemon.Effectiveness
import com.drbrosdev.battle.pokemon.Element
import com.drbrosdev.battle.pokemon.effectiveness
import com.drbrosdev.battle.pokemon.hasAnyOf
import com.drbrosdev.battle.pokemon.isBurned
import com.drbrosdev.battle.pokemon.multiplier

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
    val move = user[moveId]
    when {
        move.isPhysical() && user.isBurned() -> DamageMultiplier(50)
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