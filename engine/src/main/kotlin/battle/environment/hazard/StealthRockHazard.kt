package com.drbrosdev.battle.environment.hazard

import com.drbrosdev.battle.pokemon.Effectiveness
import com.drbrosdev.battle.pokemon.Element
import com.drbrosdev.battle.pokemon.MagicGuard
import com.drbrosdev.battle.pokemon.Pokemon
import com.drbrosdev.battle.pokemon.effectiveness
import com.drbrosdev.battle.pokemon.stats.Stat

data object StealthRockHazard : EntryHazard {
    override fun apply(pokemon: Pokemon): Pokemon {
        // magic-guard ability is immune
        if (pokemon.ability == MagicGuard) {
            return pokemon
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
        return pokemon.copy(inBattleHp = Stat(newHp))
    }
}
