package com.drbrosdev.battle.presentation

import com.drbrosdev.battle.Battle
import com.drbrosdev.battle.TeamId
import com.drbrosdev.battle.pokemon.MajorStatus
import com.drbrosdev.battle.pokemon.Pokemon
import com.drbrosdev.battle.pokemon.name

object TextTeamPresenter {

    private const val DIVIDER = "═══════════════════════════════════════"

    fun present(battle: Battle, teamId: TeamId, censor: Boolean = false) = buildString {
        val team = battle.team(teamId)
        val activeId = when {
            battle.team1.id == teamId -> battle.active1
            else -> battle.active2
        }
        appendLine(DIVIDER)
        appendLine(" Team")
        appendLine(DIVIDER)
        team.members.values.forEach { pokemon ->
            appendMember(
                pokemon = pokemon,
                isActive = pokemon.id == activeId,
                censor = censor
            )
        }
        append(DIVIDER)
    }

    private fun StringBuilder.appendMember(
        pokemon: Pokemon,
        isActive: Boolean,
        censor: Boolean
    ) {
        val indicator = if (isActive) "★" else " "
        val name = pokemon.name.padEnd(12)
        val elements = pokemon.elements.values
            .joinToString("/") { it.name }
            .padEnd(10)
        val level = "Lv.${pokemon.level.value}".padEnd(6)
        val hp = when {
            censor.not() -> "${pokemon.inBattleHp.value}/${pokemon.effectiveStats.hp.value}".padEnd(10)
            else -> ""
        }
        val status = when (pokemon.majorStatus) {
            is MajorStatus.Normal -> ""
            else -> pokemon.majorStatus.name
        }
        appendLine(" $indicator $name$elements$level$hp$status")
    }
}