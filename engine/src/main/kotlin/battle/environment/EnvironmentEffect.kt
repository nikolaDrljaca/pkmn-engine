package com.drbrosdev.battle.environment

import com.drbrosdev.battle.pokemon.Pokemon

/**
 * An effect which applies for an environment subject
 * or entry hazard.
 *
 * Usually applied after a switch.
 *
 * Provides support for:
 * - Stealth Rock
 * - Spikes
 * - Toxic Spikes
 * - Light Screen
 * - Reflect
 * - Tailwind
 * - Sticky Web
 */
fun interface EnvironmentEffect {
    fun apply(pokemon: Pokemon): Pokemon

    companion object {
        val NoEffect = EnvironmentEffect { it }
    }
}