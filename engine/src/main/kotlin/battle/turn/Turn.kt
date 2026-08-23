package com.drbrosdev.battle.turn

/*
A turn is a list of turn actions which need to be executed in a battle.
This enables support for 1v1 and 2v2 battles.
 */
data class Turn(
    val actions: List<TurnAction>
) {
    init {
        require(actions.isNotEmpty()) {
            "Turn with no actions is not possible!"
        }
    }
}
