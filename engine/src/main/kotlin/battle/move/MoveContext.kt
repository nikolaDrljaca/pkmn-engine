package com.drbrosdev.battle.move

import com.drbrosdev.battle.pokemon.PokemonId

/*
Carries only directional targeting information.
Actual pokemon objects are derived from the battle object itself
since it is the main state holder.
 */
data class MoveContext(
    val userId: PokemonId,
    val targetId: PokemonId,
    val moveId: MoveId
)