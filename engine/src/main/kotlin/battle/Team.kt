package com.drbrosdev.battle

import com.drbrosdev.battle.pokemon.Pokemon
import com.drbrosdev.battle.pokemon.PokemonId
import com.drbrosdev.battle.pokemon.hasFainted
import java.util.UUID

data class Team(
    val members: Map<PokemonId, Pokemon>,
    val id: TeamId = TeamId()
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

fun Team.updateMember(pokemon: Pokemon) = copy(members = members.toMutableMap() + (pokemon.id to pokemon))

fun Team.allFainted() = members.values.all { it.hasFainted() }

@JvmInline
value class TeamId private constructor(val id: String) {

    override fun toString(): String = id

    companion object {
        operator fun invoke(): TeamId {
            val unique = UUID.randomUUID()
                .toString()
                .take(4)
            return TeamId("team-$unique")
        }

        operator fun invoke(id: String) : TeamId {
            require(id.contains("-"))
            return TeamId(id)
        }
    }
}