package com.drbrosdev.parser

sealed interface TurnSelection {

    data class MoveSelected(val move: String): TurnSelection

    data class Switch(val incoming: String): TurnSelection

}