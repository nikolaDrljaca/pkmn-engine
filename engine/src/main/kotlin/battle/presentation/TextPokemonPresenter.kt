package com.drbrosdev.battle.presentation

import com.drbrosdev.battle.Battle
import com.drbrosdev.battle.move.MoveType
import com.drbrosdev.battle.pokemon.*
import com.drbrosdev.battle.pokemon.stats.StatKey
import com.drbrosdev.battle.pokemon.stats.StatModificationContext
import com.drbrosdev.battle.pokemon.stats.StatModifier
import com.drbrosdev.battle.pokemon.stats.stage

/*
 * Computes a Text based representation of Pokemon details.
 * Shows the most essential information, with possible censoring.
 * Meant to be used with commands:
 * `show active`
 * `show active op` (censored)
 */
object TextPokemonPresenter {
    private const val DIVIDER = "═══════════════════════════════════════"
    private const val BAR_LENGTH = 16

    fun present(
        pokemonId: PokemonId,
        battle: Battle,
        censor: Boolean = false
    ): String = buildString {
        val pokemon = battle[pokemonId]
        appendLine(DIVIDER)
        appendHeader(pokemon)
        appendHpBar(pokemon)
        appendStatus(pokemon)
        appendAbility(pokemon)
        if (censor.not()) {
            appendItem(pokemon)
        }
        appendLine(DIVIDER)
        appendStats(pokemon, battle, censor)
        appendLine(DIVIDER)
        if (censor.not()) {
            appendMoves(pokemon)
            append(DIVIDER)
        }
    }

    fun StringBuilder.appendBasicInfo(pokemon: Pokemon) {
        appendHeader(pokemon)
        appendHpBar(pokemon)
        appendStatus(pokemon)
    }

    private fun StringBuilder.appendAbility(pokemon: Pokemon) {

    }

    private fun StringBuilder.appendHeader(pokemon: Pokemon) {
        val elements = pokemon.elements.values.joinToString("/") { it.name }
        appendLine(" ${pokemon.name} ($elements) Lv.${pokemon.level.value}")
    }

    private fun StringBuilder.appendHpBar(pokemon: Pokemon) {
        val current = pokemon.inBattleHp.value
        val max = pokemon.effectiveStats.hp.value
        val filled = ((current.toDouble() / max) * BAR_LENGTH).toInt()
        val empty = BAR_LENGTH - filled
        val bar = "█".repeat(filled) + "░".repeat(empty)
        appendLine(" HP: $bar $current/$max")
    }

    private fun StringBuilder.appendStatus(pokemon: Pokemon) {
        if (pokemon.majorStatus !is MajorStatus.Normal) {
            appendLine(" Status: ${pokemon.majorStatus.name}")
        }
        pokemon.volatileStatus.forEach { status ->
            appendLine(" Minor: ${status.name}")
        }
    }

    private fun StringBuilder.appendItem(pokemon: Pokemon) {
        appendLine(" Item: ${pokemon.item.name}")
    }

    private fun StringBuilder.appendStats(
        pokemon: Pokemon,
        battle: Battle,
        censor: Boolean = false
    ) {
        appendLine(" Stats (${pokemon.nature.name})")
        val stats = pokemon.computeInBattleStats(battle)
        val stages = pokemon.statModifications
            .asSequence()
            .map { it.compute(StatModificationContext(pokemon, battle)) }
            .map { it.modifiers }
            .flatMap { it.entries }
            .filter { it.value is StatModifier.Stage }
            .associate { it.key to it.value }

        listOf(
            StatKey.ATTACK to "ATK",
            StatKey.DEFENCE to "DEF",
            StatKey.SPECIAL_ATTACK to "SP.ATK",
            StatKey.SPECIAL_DEFENCE to "SP.DEF",
            StatKey.SPEED to "SPD"
        ).forEach { (key, label) ->
            val value = stats[key].value
            val stage = stages[key]?.stage() ?: 0
            val stageValue = when {
                stage > 0 -> "(+$stage)"
                stage < 0 -> "($stage)"
                else -> ""
            }
            when {
                censor -> appendLine(" ${label.padEnd(7)}${stageValue}")
                else -> appendLine(" ${label.padEnd(7)}${value.toString().padEnd(4)}${stageValue}")
            }
        }
    }

    private fun StringBuilder.appendMoves(pokemon: Pokemon) {
        appendLine(" Moves")
        pokemon.moves.forEachIndexed { index, move ->
            val category = when (move.type) {
                MoveType.PHYSICAL -> "PH"
                MoveType.SPECIAL -> "SP"
                MoveType.STATUS -> "ST"
            }
            val name = move.name.padEnd(14)
            val element = move.element.name.padEnd(12)
            val pp = "${move.powerPoints}"
            appendLine(" [${index + 1}] $name$element$category  $pp")
        }
    }
}
