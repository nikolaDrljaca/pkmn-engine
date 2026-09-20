package com.drbrosdev.battle.environment.hazard

import com.drbrosdev.battle.pokemon.Pokemon

interface EntryHazard {
    fun apply(pokemon: Pokemon): Pokemon

    companion object {
        const val Spikes = "hazard-spikes"
        const val ToxicSpikes = "hazard-toxic-spikes"
        const val StealtRock = "hazard-stealth-rock"
        const val StickyWeb = "hazard-sticky-web"
    }
}

