package com.drbrosdev.parser.team

import com.drbrosdev.battle.Team
import com.drbrosdev.battle.item.ItemIndex
import com.drbrosdev.battle.move.registry.MoveIndex
import com.drbrosdev.battle.pokemon.Pokemon
import com.drbrosdev.battle.pokemon.buildPokemon
import com.drbrosdev.battle.pokemon.registry.AbilityIndex
import com.drbrosdev.battle.pokemon.registry.NatureIndex
import com.drbrosdev.battle.pokemon.registry.PokemonIndex
import com.drbrosdev.battle.pokemon.stats.StatKey

object TextTeamParser : TeamParser {
    override fun parse(input: String): Team {
        val team = parseIntoChunks(input)
            .map { parseIntoPokemon(it) }
            .associateBy { it.id }
        return Team(members = team)
    }

    private fun parseIntoPokemon(chunk: List<String>): Pokemon = buildPokemon {
        individualValues { allMax() }
        for (line in chunk) {
            when {
                line.contains("@") -> {
                    val (pokemonSlug, itemSlug) = line.split("@").also {
                        require(it.size == 2) {
                            "Cannot parse line $line!"
                        }
                    }
                    val pokemon = requireNotNull(PokemonIndex.lookup[pokemonSlug.trim()]) {
                        "Cannot find pokemon $name in index!"
                    }
                    staticConfig(pokemon)

                    item = requireNotNull(ItemIndex.lookup[itemSlug.trim()]) {
                        "Cannot find item $itemSlug in index!"
                    }
                }

                line.contains("Ability:") -> {
                    val abilitySlug = line.removePrefix("Ability:").trim()
                    ability = requireNotNull(AbilityIndex.lookup[abilitySlug]) {
                        "Cannot find ability $abilitySlug in index!"
                    }
                }

                line.startsWith("EVs:") -> {
                    line.removePrefix("EVs:")
                        .split("/")
                        .map { parseStat(it) }
                        .forEach { effortValue(it.first, it.second) }
                }

                line.startsWith("IVs:") -> {
                    line.removePrefix("IVs:")
                        .split("/")
                        .map { parseStat(it) }
                        .forEach { individualValue(it.first, it.second) }
                }

                line.endsWith("Nature") -> {
                    val natureSlug = line.split(" ").first().trim()
                    nature = requireNotNull(NatureIndex.lookup[natureSlug]) {
                        "Cannot find nature $natureSlug in index!"
                    }
                }

                line.startsWith("-") -> {
                    val moveSlug = line.removePrefix("-").trim()
                    val move = requireNotNull(MoveIndex.lookup[moveSlug]) {
                        "Cannot find move $moveSlug in index!"
                    }
                    addMove(move)
                }
            }
        }
    }

    private fun parseStat(input: String): Pair<Int, StatKey> {
        val temp = input.trim().split(" ")
        val key = when (val current = temp.last().trim()) {
            "SpA" -> StatKey.SPECIAL_ATTACK
            "SpD" -> StatKey.SPECIAL_DEFENCE
            "Spe" -> StatKey.SPEED
            "HP" -> StatKey.HP
            "Atk" -> StatKey.ATTACK
            "Def" -> StatKey.DEFENCE
            else -> error("Cannot parse EffortValue $current!")
        }
        return temp.first().toInt() to key
    }

    private fun parseIntoChunks(input: String): List<List<String>> {
        return input.lines()
            .fold(mutableListOf(mutableListOf<String>())) { chunks, line ->
                when {
                    line.isBlank() -> if (chunks.last().isNotEmpty()) chunks.add(mutableListOf())
                    else -> chunks.last().add(line)
                }
                chunks
            }
            .filter { it.isNotEmpty() }
    }
}