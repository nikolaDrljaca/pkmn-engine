package com.drbrosdev.sample

import com.drbrosdev.battle.Team
import com.drbrosdev.battle.TeamId
import com.drbrosdev.battle.pokemon.registry.PokemonIndex

val SampleTeam1 = Team(
    id = TeamId("sample-1"),
    members = mapOf(
        PokemonIndex.Tyranitar.id to PokemonIndex.Tyranitar,
        PokemonIndex.Alakazam.id to PokemonIndex.Alakazam,
        PokemonIndex.Ferrothorn.id to PokemonIndex.Ferrothorn,
    )
)

val SampleTeam2 = Team(
    id = TeamId("sample-2"),
    members = mapOf(
        PokemonIndex.LandorusTherian.id to PokemonIndex.LandorusTherian,
        PokemonIndex.Latios.id to PokemonIndex.Latios,
        PokemonIndex.Ferrothorn.id to PokemonIndex.Ferrothorn,
    )
)
