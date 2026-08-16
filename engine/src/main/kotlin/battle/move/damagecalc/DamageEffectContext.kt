package com.drbrosdev.battle.move.damagecalc

import com.drbrosdev.battle.Battle
import com.drbrosdev.battle.move.MoveId
import com.drbrosdev.battle.pokemon.PokemonId

/**
 * Context holder for information needed in the
 * damage calculation pipeline.
 */
data class DamageEffectContext(
    val battle: Battle,
    val userId: PokemonId,
    val targetId: PokemonId,
    val moveId: MoveId,
    val critical: Boolean,
) {
    val user = battle[userId]
    val target = battle[targetId]
    val move = user[moveId]
}
