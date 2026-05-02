package com.drbrosdev.battle.environment

import com.drbrosdev.battle.pokemon.Effectiveness
import com.drbrosdev.battle.pokemon.Element
import com.drbrosdev.battle.pokemon.MagicGuard
import com.drbrosdev.battle.pokemon.effectiveness
import com.drbrosdev.battle.pokemon.stats.Stat
import java.util.logging.Logger

private val LOG = Logger.getLogger(EnvironmentUnit::class.qualifiedName)

/*
 * Switch system:
 * 1. Switch out effect run 
 *  - clear volatile states
 *  - clear temporary stat changes / modifiers
 *  - enable disabled moves
 * 2. Perform Switch
 * 3. Switch in effects run, from the environment
 */
data class EnvironmentUnit(
    val id: String,
    val effect: EnvironmentEffect = EnvironmentEffect.NoEffect
)

val StealthRockEnvUnit = EnvironmentUnit(
    id = "stealth-rock",
    effect = { pokemon ->
        // magic-guard ability is immune
        if (pokemon.ability == MagicGuard) {
            return@EnvironmentUnit pokemon
        }
        val effectiveness = effectiveness(Element.ROCK, pokemon.elements)
        val percentDamage = with(pokemon.effectiveStats.hp) {
            when (effectiveness) {
                Effectiveness.IMMUNE -> value / 8
                Effectiveness.QUARTER -> value / 32
                Effectiveness.NOT_VERY -> value / 16
                Effectiveness.NEUTRAL -> value / 8
                Effectiveness.SUPER -> value / 4
                Effectiveness.DOUBLE_SUPER -> value / 2
            }
        }
        val damage = percentDamage.coerceAtLeast(1)
        val newHp = (pokemon.inBattleHp.value - damage).coerceAtLeast(0)
        LOG.fine { "Stealth Rock deals $damage($effectiveness) to ${pokemon.id}." }
        pokemon.copy(
            inBattleHp = Stat(newHp)
        )
    }
)

val SpikesEnvUnit = EnvironmentUnit(
    id = "spikes",
    effect = { pokemon ->
        // TODO: implement
        pokemon
    }
)

val ToxicSpikesEnvUnit = EnvironmentUnit(
    id = "toxic-spikes",
    effect = { pokemon ->
        // TODO: implement
        pokemon
    }
)

val LightScreenEnvUnit = EnvironmentUnit(
    id = "light-screen",
    effect = { pokemon ->
        // TODO: implement
        pokemon
    }
)

val ReflectEnvUnit = EnvironmentUnit(
    id = "reflect",
    effect = { pokemon ->
        // TODO: implement
        pokemon
    }
)

val TailwindEnvUnit = EnvironmentUnit(
    id = "tailwind",
    effect = { pokemon ->
        // TODO: implement
        pokemon
    }
)

val StickyWebEnvUnit = EnvironmentUnit(
    id = "sticky-web",
    effect = { pokemon ->
        // TODO: implement
        pokemon
    }
)


