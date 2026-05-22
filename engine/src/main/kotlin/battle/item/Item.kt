package com.drbrosdev.battle.item

import com.drbrosdev.battle.move.damagecalc.DamageModifier
import com.drbrosdev.battle.move.damagecalc.DamageMultiplier
import com.drbrosdev.battle.move.MoveEffect
import com.drbrosdev.battle.pokemon.Effectiveness
import com.drbrosdev.battle.pokemon.Element
import com.drbrosdev.battle.pokemon.effectiveness
import com.drbrosdev.battle.pokemon.stats.*
import com.drbrosdev.battle.turn.EndOfTurnEffect
import com.drbrosdev.battle.turn.EndOfTurnEffectResult
import java.util.logging.Logger

private val LOG = Logger.getLogger(Item::class.qualifiedName)

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
    val damageMultiplier: DamageModifier = DamageModifier { null },
    val statModification: StatModification = StatModification { StatModifiers() }
) {
    companion object {
        val NoItem = Item(ItemId("no-item"), name = "")
    }
}

@JvmInline
value class ItemId(val value: String)

