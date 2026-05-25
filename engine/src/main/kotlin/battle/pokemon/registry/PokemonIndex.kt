package com.drbrosdev.battle.pokemon.registry

import com.drbrosdev.battle.pokemon.Element
import com.drbrosdev.battle.pokemon.buildPokemon

object PokemonIndex {
    private val Tyranitar = buildPokemon {
        pokemonId("tyranitar")
        elements(Element.ROCK, Element.DARK)
        name = "Tyranitar"
        baseStats {
            baseHp = 100
            attack = 134
            defence = 110
            specialAttack = 95
            specialDefence = 100
            speed = 61
        }
    }

    private val Alakazam = buildPokemon {
        pokemonId("alakazam")
        name = "Alakazam"
        elements(Element.PSYCHIC)
        baseStats {
            baseHp = 55
            attack = 50
            defence = 45
            specialAttack = 135
            specialDefence = 85
            speed = 120
        }
    }

    private val Latios = buildPokemon {
        pokemonId("latios")
        name = "Latios"
        elements(Element.DRAGON, Element.PSYCHIC)
        baseStats {
            baseHp = 80
            attack = 90
            defence = 80
            specialAttack = 130
            specialDefence = 110
            speed = 110
        }
    }

    private val Ferrothorn = buildPokemon {
        pokemonId("ferrothorn")
        name = "Ferrothorn"
        elements(Element.GRASS, Element.STEEL)
        baseStats {
            baseHp = 74
            attack = 94
            defence = 131
            specialAttack = 54
            specialDefence = 116
            speed = 20
        }
    }

    private val LandorusTherian = buildPokemon {
        pokemonId("landorus-therian")
        name = "Landorus-Therian"
        elements(Element.GROUND, Element.FLYING)
        baseStats {
            baseHp = 89
            attack = 145
            defence = 90
            specialAttack = 105
            specialDefence = 80
            speed = 91
        }
    }

    val lookup = mapOf(
        Tyranitar.name to Tyranitar,
        Latios.name to Latios,
        Ferrothorn.name to Ferrothorn,
        Alakazam.name to Alakazam,
        LandorusTherian.name to LandorusTherian,
    )
}