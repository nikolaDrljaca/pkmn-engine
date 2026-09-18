package com.drbrosdev.battle

import com.drbrosdev.battle.environment.BattleEnvironment
import com.drbrosdev.battle.environment.EnvironmentUnit
import com.drbrosdev.battle.environment.Weather
import com.drbrosdev.battle.pokemon.Pokemon
import com.drbrosdev.battle.pokemon.PokemonId

// state container for the current battle state
data class Battle(
    // cross-team pokemon ids (map keys) are unique! (eg pokemonId-1)
    val team1: Team,
    val team2: Team,
    val active1: PokemonId,
    val active2: PokemonId,

    val weather: Weather = Weather.None,
    // BUG: CARE that only Spikes and ToxicSpikes can be applied twice
    // Probably this should be its own model!
    val environmentUnits: Map<TeamId, Set<EnvironmentUnit>> = emptyMap(),
    val environment: BattleEnvironment = BattleEnvironment(),

    // state information
    val state: BattleState = BattleState.InProgress,
    val turnCount: Int = 1,
    val turnLog: List<String> = emptyList()
) {

    operator fun get(pokemonId: PokemonId): Pokemon = when {
        team1.hasMember(pokemonId) -> team1[pokemonId]
        team2.hasMember(pokemonId) -> team2[pokemonId]
        else -> error("Pokemon $pokemonId is not in the current Battle!")
    }

    operator fun get(teamId: TeamId): Pokemon = when {
        team1.id == teamId -> team1[active1]
        team2.id == teamId -> team2[active2]
        else -> error("Team $teamId is not in the current Battle!")
    }

    fun team(teamId: TeamId): Team = when {
        team1.id == teamId -> team1
        team2.id == teamId -> team2
        else -> error("Team $teamId is not in the current Battle!")
    }

    fun team(pokemonId: PokemonId): Team = when {
        team1.hasMember(pokemonId) -> team1
        team2.hasMember(pokemonId) -> team2
        else -> error("Team $pokemonId is not in the current Battle!")
    }

    fun environment(pokemon: PokemonId): Set<EnvironmentUnit> {
        val units = when {
            team1.hasMember(pokemon) -> environmentUnits[team1.id]
            team2.hasMember(pokemon) -> environmentUnits[team2.id]
            else -> error("Pokemon ${pokemon.id} is not in the current Battle!")
        }
        return units.orEmpty()
    }

    fun entryHazards(pokemon: PokemonId) = when {
        team1.hasMember(pokemon) -> environment.entryHazards(team1.id)
        team2.hasMember(pokemon) -> environment.entryHazards(team2.id)
        else -> error("Pokemon ${pokemon.id} is not in the current Battle!")
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

    fun updateEnvironment(target: PokemonId, vararg unit: EnvironmentUnit): Battle {
        val teamId = when {
            team1.hasMember(target) -> team1.id
            team2.hasMember(target) -> team2.id
            else -> error("Pokemon $target is not in the current Battle!")
        }
        val updatedUnits = environmentUnits[teamId].orEmpty() + unit
        return copy(
            environmentUnits = environmentUnits + (teamId to updatedUnits),
            turnLog = turnLog + unit.map { it.enterNarrativeMessage }
        )
    }

    fun switch(user: PokemonId, target: PokemonId): Battle {
        return when (user) {
            active1 -> copy(active1 = target)
            active2 -> copy(active2 = target)
            else -> error("Pokemon ${user.id} not found in battle!")
        }
    }

    /**
     * Use to append a narrative message into the turn log.
     */
    fun narrative(message: String): Battle = when {
        message.isNotBlank() -> copy(turnLog = turnLog + message)
        else -> this
    }
}


fun Battle.abilities() = sequenceOf(
    this[active1].ability,
    this[active2].ability
)

sealed interface BattleOutcome {
    data class Winner(val team: Team) : BattleOutcome {
        override fun toString(): String = team.id.toString()
    }

    data object Draw : BattleOutcome
}

// NOTE: I'm not sure BattleState and BattleOutcome are the proper abstractions
sealed interface BattleState {
    data object InProgress : BattleState
    data class Concluded(val outcome: BattleOutcome) : BattleState
}
