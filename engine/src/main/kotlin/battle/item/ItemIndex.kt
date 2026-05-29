package com.drbrosdev.battle.item

import com.drbrosdev.battle.move.MoveEffect
import com.drbrosdev.battle.move.damagecalc.DamageMultiplier
import com.drbrosdev.battle.pokemon.Effectiveness
import com.drbrosdev.battle.pokemon.Element
import com.drbrosdev.battle.pokemon.MajorStatus
import com.drbrosdev.battle.pokemon.effectiveness
import com.drbrosdev.battle.pokemon.stats.Stat
import com.drbrosdev.battle.pokemon.stats.StatKey
import com.drbrosdev.battle.pokemon.stats.StatModification
import com.drbrosdev.battle.pokemon.stats.StatModifier
import com.drbrosdev.battle.pokemon.stats.StatModifiers
import com.drbrosdev.battle.turn.EndOfTurnEffectResult
import jdk.incubator.vector.VectorOperators.LOG

object ItemIndex {

    private val ExpertBelt = Item(
        id = ItemId("expert-belt"),
        name = "Expert Belt",
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

    private val LifeOrb = Item(
        id = ItemId("life-orb"),
        name = "Life Orb",
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

    private val ChoiceBand = Item(
        id = ItemId("choice-band"),
        name = "Choice Band",
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

    private val ChoiceScarf = Item(
        id = ItemId("choice-scarf"),
        name = "Choice Scarf",
        statModification = StatModification { context ->
            StatModifiers(
                mapOf(
                    StatKey.SPEED to StatModifier.Percent(150)
                )
            )
        },
        afterMoveEffect = MoveEffect { battle ->
            val user = battle[userId]
            val move = user[moveId]

            battle.updateMons(user.choiceMove(move.id))
        }
    )

    private val Leftovers = Item(
        id = ItemId("leftovers"),
        name = "Leftovers",
        endOfTurnEffect = { pokemon ->
            // healing is calculated of maxHp
            val healing = (pokemon.effectiveStats.hp.value / 8).coerceAtLeast(1)
            val newHp = (pokemon.inBattleHp.value + healing)
                // cannot over-heal
                .coerceAtMost(pokemon.effectiveStats.hp.value)
            EndOfTurnEffectResult(
                pokemon = pokemon.copy(inBattleHp = Stat(newHp)),
                narrativeMessage = "${pokemon.name} restored $healing using Leftovers."
            )
        }
    )

    private val BlackSludge = Item(
        id = ItemId("black-sludge"),
        name = "Black Sludge",
        endOfTurnEffect = { pokemon ->
            when {
                pokemon.elements.values.contains(Element.POISON) -> {
                    val healing = (pokemon.effectiveStats.hp.value / 16).coerceAtLeast(1)
                    val newHp = (pokemon.inBattleHp.value + healing)
                        // cannot over-heal
                        .coerceAtMost(pokemon.effectiveStats.hp.value)
                    EndOfTurnEffectResult(
                        pokemon = pokemon.copy(inBattleHp = Stat(newHp)),
                        narrativeMessage = "${pokemon.name} restored $healing using Black Sludge."
                    )
                }
                // take 1/16 damage
                else -> {
                    val damage = (pokemon.effectiveStats.hp.value / 8).coerceAtLeast(1)
                    val newHp = (pokemon.inBattleHp.value - damage)
                        // hp cannot go below 0
                        .coerceAtLeast(0)
                    EndOfTurnEffectResult(
                        pokemon = pokemon.copy(inBattleHp = Stat(newHp)),
                        narrativeMessage = "${pokemon.name} took $damage from Black Sludge."
                    )
                }
            }
        }
    )

    private val ToxicOrb = Item(
        id = ItemId("toxic-orb"),
        name = "Toxic Orb",
        endOfTurnEffect = { pokemon ->
            val isPoisoned = pokemon.majorStatus is MajorStatus.Poisoned || pokemon.majorStatus is MajorStatus.BadlyPoisoned
            when {
                isPoisoned -> EndOfTurnEffectResult(pokemon)
                else -> {
                    val updated = pokemon.copy(majorStatus = MajorStatus.Poisoned)
                    EndOfTurnEffectResult(
                        pokemon = updated,
                        narrativeMessage = "${pokemon.name} is poisoned by Toxic Orb!"
                    )
                }
            }
        }
    )

    val lookup = mapOf(
        ToxicOrb.name to ToxicOrb,
        Leftovers.name to Leftovers,
        BlackSludge.name to BlackSludge,
        ExpertBelt.name to ExpertBelt,
        LifeOrb.name to LifeOrb,
        ChoiceBand.name to ChoiceBand,
        ChoiceScarf.name to ChoiceScarf,
    )
}