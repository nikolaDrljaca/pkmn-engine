package com.drbrosdev.battle.move

/*
Carries only directional targeting information.
Actual pokemon objects are derived from the battle object itself
since it is the main state holder.
 */
data class MoveContext(
    val userId: String,
    val targetId: String,
    val moveId: String
)