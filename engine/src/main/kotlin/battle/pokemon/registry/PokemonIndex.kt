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

    private val Breloom = buildPokemon {
        pokemonId("breloom")
        name = "Breloom"
        elements(Element.GRASS, Element.FIGHTING)
        baseStats {
            baseHp = 60
            attack = 130
            defence = 80
            specialAttack = 60
            specialDefence = 60
            speed = 70
        }
    }

    private val Garchomp = buildPokemon {
        pokemonId("garchomp")
        name = "Garchomp"
        elements(Element.DRAGON, Element.GROUND)
        baseStats {
            baseHp = 108
            attack = 130
            defence = 95
            specialAttack = 80
            specialDefence = 85
            speed = 102
        }
    }

    private val Azelf = buildPokemon {
        pokemonId("azelf")
        name = "Azelf"
        elements(Element.PSYCHIC)
        baseStats {
            baseHp = 75
            attack = 125
            defence = 70
            specialAttack = 125
            specialDefence = 70
            speed = 115
        }
    }

    private val Terrakion = buildPokemon {
        pokemonId("terrakion")
        name = "Terrakion"
        elements(Element.ROCK, Element.FIGHTING)
        baseStats {
            baseHp = 91
            attack = 129
            defence = 90
            specialAttack = 72
            specialDefence = 90
            specialAttack = 108
        }
    }

    private val Salamance = buildPokemon {
        pokemonId("salamence")
        name = "Salamence"
        elements(Element.DRAGON, Element.FLYING)
        baseStats {
            baseHp = 95
            attack = 135
            defence = 80
            specialAttack = 110
            specialDefence = 80
            speed = 100
        }
    }

    val lookup = mapOf(
        Terrakion.name to Terrakion,
        Azelf.name to Azelf,
        Garchomp.name to Garchomp,
        Breloom.name to Breloom,
        Tyranitar.name to Tyranitar,
        Latios.name to Latios,
        Ferrothorn.name to Ferrothorn,
        Alakazam.name to Alakazam,
        LandorusTherian.name to LandorusTherian,
        Salamance.name to Salamance
    )
}