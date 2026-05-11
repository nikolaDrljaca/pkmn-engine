package com.drbrosdev.sample

import com.drbrosdev.battle.Team
import com.drbrosdev.battle.TeamId
import com.drbrosdev.battle.pokemon.registry.PokemonIndex

object SampleTeams {
    fun getSampleTeam1(): Team {
        val tyranitar = PokemonIndex.getTyranitar()
        val alakazam = PokemonIndex.getAlakazam()
        val ferrothrorn = PokemonIndex.getFerrothorn()
        return Team(
            id = TeamId("sample-1"),
            members = mapOf(
                tyranitar.id to tyranitar,
                alakazam.id to alakazam,
                ferrothrorn.id to ferrothrorn
            )
        )
    }

    fun getSampleTeam2(): Team {
        val therian = PokemonIndex.getLandorusTherian()
        val latios = PokemonIndex.getLatios()
        val ferrothrorn = PokemonIndex.getFerrothorn()
        return Team(
            id = TeamId("sample-2"),
            members = mapOf(
                therian.id to therian,
                latios.id to latios,
                ferrothrorn.id to ferrothrorn
            )
        )
    }
}
