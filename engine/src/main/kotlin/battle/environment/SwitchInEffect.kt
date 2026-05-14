package com.drbrosdev.battle.environment

import com.drbrosdev.battle.Battle
import com.drbrosdev.battle.pokemon.Pokemon

fun interface SwitchInEffect {
    fun apply(incoming: Pokemon, battle: Battle): Battle
}
