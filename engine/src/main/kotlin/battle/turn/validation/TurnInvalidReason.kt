package com.drbrosdev.battle.turn.validation

/*
Error messages can be introduced here
 */
sealed interface TurnInvalidReason {
    data object NoPowerPoints : TurnInvalidReason
    data object MoveDisabled : TurnInvalidReason
    data object PokemonTaunted : TurnInvalidReason
    data object ActivePokemonFainted : TurnInvalidReason
    data object SameSwitchTarget : TurnInvalidReason
}

val TurnInvalidReason.narrativeMessage: String
    get() = when (this) {
        TurnInvalidReason.ActivePokemonFainted -> "The active pokemon has fainted!"
        TurnInvalidReason.MoveDisabled -> "That move is disabled!"
        TurnInvalidReason.NoPowerPoints -> "That move has no more power points!"
        TurnInvalidReason.PokemonTaunted -> "That move cannot be used while Taunted!"
        TurnInvalidReason.SameSwitchTarget -> "Cannot switch into the same pokemon!"
    }
