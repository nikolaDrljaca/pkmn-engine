package com.drbrosdev.battle.pokemon.registry

import com.drbrosdev.battle.item.ChoiceScarf
import com.drbrosdev.battle.item.Leftovers
import com.drbrosdev.battle.item.LifeOrb
import com.drbrosdev.battle.move.registry.MoveIndex
import com.drbrosdev.battle.pokemon.*

object PokemonIndex {
    val Tyranitar = buildPokemon {
        pokemonId("tyranitar")
        elements(Element.ROCK, Element.DARK)
        name = "Tyranitar"
        nature = Brave // user input
        item = Leftovers // user input
        ability = SandStream // user input
        baseStats {
            baseHp = 100
            attack = 134
            defence = 110
            specialAttack = 95
            specialDefence = 100
            speed = 61
        }
        individualValues { allMax() } // user input
        effortValues { // user input
            hp = 248
            attack = 44
            specialAttack = 44
            speed = 172
        }
        addMoves( // user input
            MoveIndex.Crunch,
            MoveIndex.Flamethrower,
            MoveIndex.ThunderWave,
            MoveIndex.Pursuit,
        )
    }

    val Alakazam = buildPokemon {
        pokemonId("alakazam")
        name = "Alakazam"
        elements(Element.PSYCHIC)
        nature = Timid
        item = LifeOrb
        ability = MagicGuard
        baseStats {
            baseHp = 55
            attack = 50
            defence = 45
            specialAttack = 135
            specialDefence = 85
            speed = 120
        }
        individualValues { allMax() }
        effortValues {
            defence = 88
            specialAttack = 188
            speed = 232
        }
        addMoves(
            MoveIndex.Psychic,
            MoveIndex.GrassKnot,
            MoveIndex.ShadowBall,
            MoveIndex.Flamethrower,
        )
    }

    val Latios = buildPokemon {
        pokemonId("latios")
        name = "Latios"
        elements(Element.DRAGON, Element.PSYCHIC)
        nature = Timid
        ability = Levitate
        item = ChoiceScarf
        baseStats {
            baseHp = 80
            attack = 90
            defence = 80
            specialAttack = 130
            specialDefence = 110
            speed = 110
        }
        individualValues {
            allMax()
            attack = 0
        }
        effortValues {
            specialAttack = 252
            speed = 252
            specialDefence = 4
        }
        addMoves(
            MoveIndex.DracoMeteor,
            MoveIndex.Psychic,
            MoveIndex.Surf,
            MoveIndex.DragonPulse
        )
    }

    val Ferrothorn = buildPokemon {
        pokemonId("ferrothorn")
        name = "Ferrothorn"
        elements(Element.GRASS, Element.STEEL)
        nature = Relaxed
        ability = IronBarbs
        item = Leftovers
        baseStats {
            baseHp = 74
            attack = 94
            defence = 131
            specialAttack = 54
            specialDefence = 116
            speed = 20
        }
        individualValues {
            allMax()
            speed = 0
        }
        effortValues {
            hp = 248
            defence = 8
            speed = 252
        }
        addMoves(
            MoveIndex.Spikes,
            MoveIndex.KnockOff,
            MoveIndex.PowerWhip,
            MoveIndex.GyroBall
        )
    }

    val LandorusTherian = buildPokemon {
        pokemonId("landorus-therian")
        name = "Landorus-Therian"
        elements(Element.GROUND, Element.FLYING)
        nature = Naive
        ability = Intimidate
        item = Leftovers
        baseStats {
            baseHp = 89
            attack = 145
            defence = 90
            specialAttack = 105
            specialDefence = 80
            speed = 91
        }
        individualValues {
            allMax()
            hp = 30
            defence = 30
        }
        effortValues {
            hp = 100
            defence = 156
            speed = 252
        }
        addMoves(
            MoveIndex.StealthRock,
            MoveIndex.Earthquake,
            MoveIndex.UTurn,
            MoveIndex.HiddenPowerIce,
        )
    }
}