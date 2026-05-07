package com.drbrosdev.battle.environment

import com.drbrosdev.battle.pokemon.Effectiveness
import com.drbrosdev.battle.pokemon.Element
import com.drbrosdev.battle.pokemon.MagicGuard
import com.drbrosdev.battle.pokemon.effectiveness
import com.drbrosdev.battle.pokemon.stats.Stat
import java.util.logging.Logger

private val LOG = Logger.getLogger(EnvironmentUnit::class.qualifiedName)

/**
 * [EnvironmentUnit.expiresOnTurn] models environment units which can expire
 * such as Light Screen and Reflect.
 */
data class EnvironmentUnit(
    val id: String,
    val expiresOnTurn: Int? = null,
    val effect: EnvironmentEffect = EnvironmentEffect.NoEffect,
    val name: String = "",
    val enterNarrativeMessage: String = "",
    val exitNarrativeMessage: String = ""
)

val StealthRockEnvUnit = EnvironmentUnit(
    id = "stealth-rock",
    enterNarrativeMessage = "Pointed stones float in the air!",
    name = "Stealth Rock",
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
    enterNarrativeMessage = "Spikes were scattered on the ground!",
    name = "Spikes",
    effect = { pokemon ->
        // TODO: implement
        pokemon
    }
)

val ToxicSpikesEnvUnit = EnvironmentUnit(
    id = "toxic-spikes",
    name = "Toxic Spikes",
    enterNarrativeMessage = "Toxic spikes were scattered on the ground!",
    effect = { pokemon ->
        // TODO: implement
        pokemon
    }
)

val TailwindEnvUnit = EnvironmentUnit(
    id = "tailwind",
    name = "Tailwind",
    enterNarrativeMessage = "The tailwind blew from behind!",
    effect = { pokemon ->
        // TODO: implement
        pokemon
    }
)