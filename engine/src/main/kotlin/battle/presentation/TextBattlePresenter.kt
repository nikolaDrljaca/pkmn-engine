package com.drbrosdev.battle.presentation

import com.drbrosdev.battle.Battle
import com.drbrosdev.battle.environment.name
import com.drbrosdev.battle.pokemon.Pokemon

object TextBattlePresenter {
    private const val DIVIDER = "═══════════════════════════════════════"

    fun present(battle: Battle) = buildString {
        val pokemon1 = battle[battle.active1]
        val pokemon2 = battle[battle.active2]
        appendLine(DIVIDER)
        appendBattleInfo(battle)
        appendLine(DIVIDER)
        with(TextPokemonPresenter) {
            appendBasicInfo(pokemon1)
            appendEnvironment(pokemon1, battle)
            appendLine(DIVIDER)
            appendBasicInfo(pokemon2)
            appendEnvironment(pokemon1, battle)
        }
        appendLine(DIVIDER)
    }

    private fun StringBuilder.appendBattleInfo(battle: Battle) {
        appendLine(" Turn ${battle.turnCount}")
        appendLine(" Weather: ${battle.weather.name}")
    }

    private fun StringBuilder.appendEnvironment(pokemon: Pokemon, battle: Battle) {
        val env = battle.environment(pokemon.id)
        if (env.isEmpty()) return
        appendLine(" Environment: ${env.joinToString { it.name }}")
    }
}