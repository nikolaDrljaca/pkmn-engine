package com.drbrosdev.battle.item

import com.drbrosdev.battle.move.damagecalc.DamageModifier
import com.drbrosdev.battle.move.damagecalc.DamageMultiplier
import com.drbrosdev.battle.move.MoveEffect
import com.drbrosdev.battle.pokemon.Effectiveness
import com.drbrosdev.battle.pokemon.Element
import com.drbrosdev.battle.pokemon.effectiveness
import com.drbrosdev.battle.pokemon.stats.*
import com.drbrosdev.battle.turn.EndOfTurnEffect
import java.util.logging.Logger

private val LOG = Logger.getLogger("com.drbrosdev.battle.item.Item")

data class Item(
    val id: ItemId,
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
        val NoItem = Item(ItemId("no-item"))
    }
}

@JvmInline
value class ItemId(val value: String)

val ExpertBelt = Item(
    id = ItemId("expert-belt"),
    damageMultiplier = { battle ->
        val user = battle[userId]
        val move = battle[userId][moveId]
        val target = battle[targetId]
        when {
            user.item.id.value != "expert-belt" -> null
            effectiveness(move.element, target.elements) != Effectiveness.SUPER -> null
            else -> DamageMultiplier(120)
        }
    }
)

val LifeOrb = Item(
    id = ItemId("life-orb"),
    damageMultiplier = { battle ->
        when (battle[userId].item.id.value) {
            "life-orb" -> DamageMultiplier(130)
            else -> null
        }
    },
    afterMoveEffect = { battle ->
        // apply 10% recoil damage to user
        val user = battle[userId]
        val updated = with(user) {
            val damage = (effectiveStats.hp.value / 10).coerceAtLeast(1)
            val newHp = (inBattleHp.value - damage)
                .coerceAtLeast(0)
            copy(inBattleHp = Stat(newHp))

        }
        battle.updateMons(updated)
    }
)

val ChoiceBand = Item(
    id = ItemId("choice-band"),
    statModification = StatModification { context ->
        StatModifiers(
            mapOf(
                StatKey.ATTACK to StatModifier.Percent(150)
            )
        )
    },
    afterMoveEffect = MoveEffect { battle ->
        val user = battle[userId]
        val move = user[moveId]

        battle.updateMons(user.choiceMove(move.id))
    }
)

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
