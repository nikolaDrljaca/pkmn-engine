package com.drbrosdev.battle.item

import com.drbrosdev.battle.move.damagecalc.DamageModifier
import com.drbrosdev.battle.move.damagecalc.DamageMultiplier
import com.drbrosdev.battle.move.MoveEffect
import com.drbrosdev.battle.pokemon.stats.*
import com.drbrosdev.battle.turn.EndOfTurnEffect

data class Item(
    val id: ItemId,
    val name: String,
    // end of turn effects,
    val endOfTurnEffect: EndOfTurnEffect = EndOfTurnEffect.NoEffect,
    // Move execution hooks
    /**
     * Applies always before move execution, unconditionally
     */
    val preMoveEffect: MoveEffect = MoveEffect.NoEffect,
    /**
     * Applies only after successful move execution
     */
    val afterMoveEffect: MoveEffect = MoveEffect.NoEffect,
    // support for items like Life Orb, Black Glasses etc
    val damageMultiplier: DamageModifier = DamageModifier { DamageMultiplier.Neutral },
    val statModification: StatModification = StatModification { StatModifiers() }
) {
    companion object {
        val NoItem = Item(ItemId("no-item"), name = "")
    }
}

