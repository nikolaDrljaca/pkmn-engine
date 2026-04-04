package com.drbrosdev.battle

import com.drbrosdev.battle.pokemon.Pokemon
import com.drbrosdev.battle.pokemon.PokemonId
import com.drbrosdev.battle.pokemon.hasFainted

data class Team(
    val members: Map<PokemonId, Pokemon>
) {
    init {
        require(members.size <= MEMBER_LIMIT) {
            "Team cannot have more than $MEMBER_LIMIT members!"
        }
    }

    operator fun get(key: PokemonId) = requireNotNull(members[key])

    companion object {
        const val MEMBER_LIMIT = 6
    }
}

fun Team.hasMember(pokemon: Pokemon) = members.containsKey(pokemon.id)

fun Team.hasMember(id: PokemonId) = members.containsKey(id)

fun Team.updateMember(pokemon: Pokemon) = Team(members = members.toMutableMap() + (pokemon.id to pokemon))

fun Team.allFainted() = members.values.all { it.hasFainted() }

