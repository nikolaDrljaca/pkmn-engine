package com.drbrosdev.battle.turn.validation

sealed interface TurnValidity {

    data object Valid : TurnValidity

    data class Invalid(val reason: TurnInvalidReason) : TurnValidity

}