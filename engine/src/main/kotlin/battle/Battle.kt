package com.drbrosdev.battle

import com.drbrosdev.battle.pokemon.Pokemon

// state container for the current battle state
data class Battle(
    // cross-team pokemon ids (map keys) are unique! (eg pokemonId-1)
    val team1: Team,
    val team2: Team,
    val active1: String,
    val active2: String,

    val state: BattleState = BattleState.InProgress,
    val turnCount: Int = 1,

    val weather: Weather = Weather.NONE
) {

    val pokemon1 = team1[active1]
    val pokemon2 = team2[active2]

    fun updateMons(vararg pokemon: Pokemon): Battle {
        return pokemon.fold(this) { battle, mon ->
            when {
                battle.team1.hasMember(mon) -> battle.copy(team1 = team1.updateMember(mon))
                battle.team2.hasMember(mon) -> battle.copy(team2 = team2.updateMember(mon))
                else -> error("Pokemon ${mon.id} is not in the current Battle!")
            }
        }
    }

    operator fun get(pokemonId: String): Pokemon = when {
        team1.hasMember(pokemonId) -> team1[pokemonId]
        team2.hasMember(pokemonId) -> team2[pokemonId]
        else -> error("Pokemon $pokemonId is not in the current Battle!")
    }

    fun switch(user: Pokemon, target: Pokemon): Battle {
        // user volatile status clears when switching out
        val afterHeal = updateMons(this[user.id].copy(volatileStatus = emptySet()))
        return when (user.id) {
            pokemon1.id -> afterHeal.copy(active1 = target.id)
            pokemon2.id -> afterHeal.copy(active2 = target.id)
            else -> error("Pokemon ${user.id} not found in battle!")
        }
    }

}

fun Battle.abilities() = sequenceOf(pokemon1.ability, pokemon2.ability)

sealed interface BattleOutcome {
    data class Winner(val team: Team) : BattleOutcome
    data object Draw : BattleOutcome
}

// NOTE: I'm not sure BattleState and BattleOutcome are the proper abstractions
sealed interface BattleState {
    data object InProgress : BattleState
    data class Concluded(val outcome: BattleOutcome) : BattleState
}

enum class Weather {
    NONE,
    HARSH_SUN,
    RAIN,
    HAIL,
    SANDSTORM,
}
