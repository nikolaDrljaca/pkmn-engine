package com.drbrosdev.battle.environment

import com.drbrosdev.battle.pokemon.Pokemon

fun interface SwitchEffect {
    fun apply(pokemon: Pokemon): Pokemon
}

class SwitchInEffect(private val units: Collection<EnvironmentUnit>) : SwitchEffect {
    override fun apply(pokemon: Pokemon): Pokemon {
        return units.fold(pokemon) { mon, unit ->
            unit.effect.apply(mon)
        }
    }
}

object SwitchOutEffect : SwitchEffect {
    /**
     * Apply all side effects which happen when a [Pokemon]
     * is switched out.
     */
    override fun apply(pokemon: Pokemon): Pokemon {
        return pokemon.copy(
            // clear volatile status
            volatileStatus = emptySet(),
            // enable all moves
            moves = pokemon.moves
                .map { it.enable() },
            // clear all stat modifications (coming from moves etc.)
            statModifications = emptyList()
        )
    }
}
