package com.drbrosdev.battle.presentation

import com.drbrosdev.battle.move.Move
import com.drbrosdev.battle.move.MoveAccuracy
import com.drbrosdev.battle.move.MoveType
import com.drbrosdev.battle.pokemon.Pokemon

object TextMovePresenter {

    private const val DIVIDER = "═══════════════════════════════════════"

    fun present(pokemon: Pokemon) = buildString {
        appendLine(DIVIDER)
        appendLine(" Moves — ${pokemon.name}")
        appendLine(DIVIDER)
        pokemon.moves.forEachIndexed { index, move ->
            appendMove(index + 1, move)
        }
        append(DIVIDER)
    }

    private fun StringBuilder.appendMove(index: Int, move: Move) {
        val category = when (move.type) {
            MoveType.PHYSICAL -> "Physical"
            MoveType.SPECIAL -> "Special"
            MoveType.STATUS -> "Status"
        }
        val power = move.power.toString()
        val accuracy = when (move.accuracy) {
            MoveAccuracy.AlwaysHit -> "-"
            is MoveAccuracy.Percent -> move.accuracy.value.value.toString()
        }
        appendLine(" [$index] ${move.name} · ${move.element.name} · $category")
        appendLine("     Power: ${power.padEnd(4)} Accuracy: ${accuracy.padEnd(4)} PP: ${move.powerPoints}")
    }

}