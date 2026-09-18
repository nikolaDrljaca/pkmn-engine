package com.drbrosdev.battle.environment.hazard

import com.drbrosdev.battle.pokemon.Pokemon

data class SpikesHazard(val layers: Layers) : EntryHazard {
    override fun apply(pokemon: Pokemon): Pokemon {
        // TODO: @drljacan implement
        return pokemon
    }
}
