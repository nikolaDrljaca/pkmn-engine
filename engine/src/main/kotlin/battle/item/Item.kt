package com.drbrosdev.battle.item

import com.drbrosdev.battle.move.MoveEffect
import com.drbrosdev.battle.pokemon.Element
import com.drbrosdev.battle.pokemon.stats.Stat
import com.drbrosdev.battle.turn.EndOfTurnEffect
import java.util.logging.Logger

private val LOG = Logger.getLogger("com.drbrosdev.battle.item.Item")

// TODO: Item subsystem, work-in-progress

data class Item(
    val id: ItemId,
    // end of turn effects,
    val endOfTurnEffect: EndOfTurnEffect = EndOfTurnEffect.NoEffect,
    // Move execution hooks
    // preMoveEffect
    val preMoveEffect: MoveEffect = MoveEffect.NoEffect,
    val afterMoveEffect: MoveEffect = MoveEffect.NoEffect,
    // afterMoveEffect
) {
    companion object {
        val NoItem = Item(ItemId("no-item"))
    }
}

@JvmInline
value class ItemId(val value: String) {}

val Leftovers = Item(
    id = ItemId("leftovers"),
    endOfTurnEffect = { pokemon ->
        // healing is calculated of maxHp
        val healing = (pokemon.effectiveStats.hp.value / 8).coerceAtLeast(1)
        val newHp = (pokemon.inBattleHp.value + healing)
            // cannot over-heal
            .coerceAtMost(pokemon.effectiveStats.hp.value)
        LOG.fine { "${pokemon.id} heals $healing using Leftovers" }
        pokemon.copy(
            inBattleHp = Stat(newHp)
        )
    }
)

val BlackSludge = Item(
    id = ItemId("black-sludge"),
    endOfTurnEffect = { pokemon ->
        when {
            pokemon.elements.values.contains(Element.POISON) -> {
                val healing = (pokemon.effectiveStats.hp.value / 16).coerceAtLeast(1)
                val newHp = (pokemon.inBattleHp.value + healing)
                    // cannot over-heal
                    .coerceAtMost(pokemon.effectiveStats.hp.value)
                pokemon.copy(
                    inBattleHp = Stat(newHp)
                )
            }
            // take 1/16 damage
            else -> {
                val damage = (pokemon.effectiveStats.hp.value / 8).coerceAtLeast(1)
                val newHp = (pokemon.inBattleHp.value - damage)
                    // hp cannot go below 0
                    .coerceAtLeast(0)
                pokemon.copy(
                    inBattleHp = Stat(newHp)
                )
            }
        }
    }
)