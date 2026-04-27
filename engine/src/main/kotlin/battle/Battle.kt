package com.drbrosdev.battle

import com.drbrosdev.battle.pokemon.Pokemon
import com.drbrosdev.battle.pokemon.PokemonId

// state container for the current battle state
data class Battle(
    // cross-team pokemon ids (map keys) are unique! (eg pokemonId-1)
    val team1: Team,
    val team2: Team,
    val active1: PokemonId,
    val active2: PokemonId,

    val state: BattleState = BattleState.InProgress,
    val turnCount: Int = 1,

    val weather: Weather = Weather.NONE
) {

    operator fun get(pokemonId: PokemonId): Pokemon = when {
        team1.hasMember(pokemonId) -> team1[pokemonId]
        team2.hasMember(pokemonId) -> team2[pokemonId]
        else -> error("Pokemon $pokemonId is not in the current Battle!")
    }

    fun updateMons(vararg pokemon: Pokemon): Battle {
        return pokemon.fold(this) { battle, mon ->
            when {
                battle.team1.hasMember(mon) -> battle.copy(team1 = team1.updateMember(mon))
                battle.team2.hasMember(mon) -> battle.copy(team2 = team2.updateMember(mon))
                else -> error("Pokemon ${mon.id} is not in the current Battle!")
            }
        }
    }

    fun switch(user: PokemonId, target: PokemonId): Battle {
        // user volatile status clears when switching out
        val afterHeal = updateMons(this[user].copy(volatileStatus = emptySet()))
        return when (user) {
            active1 -> afterHeal.copy(active1 = target)
            active2 -> afterHeal.copy(active2 = target)
            else -> error("Pokemon ${user.id} not found in battle!")
        }
    }
}

fun Battle.abilities() = sequenceOf(
    this[active1].ability,
    this[active2].ability
)

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
