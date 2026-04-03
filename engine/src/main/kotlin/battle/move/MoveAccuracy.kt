package com.drbrosdev.battle.move

sealed interface MoveAccuracy {
    data object AlwaysHit : MoveAccuracy

    data class Percent(private val value: Percentage) : MoveAccuracy
}