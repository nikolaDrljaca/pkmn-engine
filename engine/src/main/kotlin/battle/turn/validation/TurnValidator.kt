package com.drbrosdev.battle.turn.validation

import com.drbrosdev.battle.Battle
import com.drbrosdev.battle.turn.Turn

fun interface TurnValidator {
    fun Turn.validate(battle: Battle): TurnValidity
}