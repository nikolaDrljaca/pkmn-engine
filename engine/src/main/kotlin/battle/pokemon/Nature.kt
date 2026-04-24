package com.drbrosdev.battle.pokemon

import com.drbrosdev.battle.pokemon.stats.StatKey
import com.drbrosdev.battle.pokemon.stats.StatModification
import com.drbrosdev.battle.pokemon.stats.StatModifier
import com.drbrosdev.battle.pokemon.stats.StatModifiers

class Nature(
    val name: String,
    val changes: StatModifiers
) {
    companion object {
        fun of(name: String, beneficial: StatKey, hindering: StatKey) = Nature(
            name = name,
            changes = StatModifiers(
                mapOf(
                    beneficial to StatModifier.beneficialNature(),
                    hindering to StatModifier.hinderingNature()
                )
            )
        )

        fun neutral(name: String) = Nature(
            name = name,
            changes = StatModifiers()
        )
    }
}

val Nature.statModification: StatModification
    get() = StatModification { changes }

// Neutral natures
val Hardy = Nature.neutral("Hardy")
val Docile = Nature.neutral("Docile")
val Serious = Nature.neutral("Serious")
val Bashful = Nature.neutral("Bashful")
val Quirky = Nature.neutral("Quirky")

// +Attack natures
val Lonely = Nature.of(beneficial = StatKey.ATTACK, hindering = StatKey.DEFENCE, name = "Lonely")
val Brave = Nature.of(beneficial = StatKey.ATTACK, hindering = StatKey.SPEED, name = "Brave")
val Adamant = Nature.of(beneficial = StatKey.ATTACK, hindering = StatKey.SPECIAL_ATTACK, name = "Adamant")
val Naughty = Nature.of(beneficial = StatKey.ATTACK, hindering = StatKey.SPECIAL_DEFENCE, name = "Naughty")

// +Defence natures
val Bold = Nature.of(beneficial = StatKey.DEFENCE, hindering = StatKey.ATTACK, name = "Bold")
val Relaxed = Nature.of(beneficial = StatKey.DEFENCE, hindering = StatKey.SPEED, name = "Relaxed")
val Impish = Nature.of(beneficial = StatKey.DEFENCE, hindering = StatKey.SPECIAL_ATTACK, name = "Impish")
val Lax = Nature.of(beneficial = StatKey.DEFENCE, hindering = StatKey.SPECIAL_DEFENCE, name = "Lax")

// +Sp. Attack natures
val Modest = Nature.of(beneficial = StatKey.SPECIAL_ATTACK, hindering = StatKey.ATTACK, name = "Modest")
val Mild = Nature.of(beneficial = StatKey.SPECIAL_ATTACK, hindering = StatKey.DEFENCE, name = "Mild")
val Quiet = Nature.of(beneficial = StatKey.SPECIAL_ATTACK, hindering = StatKey.SPEED, name = "Quiet")
val Rash = Nature.of(beneficial = StatKey.SPECIAL_ATTACK, hindering = StatKey.SPECIAL_DEFENCE, name = "Rash")

// +Sp. Defence natures
val Calm = Nature.of(beneficial = StatKey.SPECIAL_DEFENCE, hindering = StatKey.ATTACK, name = "Calm")
val Gentle = Nature.of(beneficial = StatKey.SPECIAL_DEFENCE, hindering = StatKey.DEFENCE, name = "Gentle")
val Sassy = Nature.of(beneficial = StatKey.SPECIAL_DEFENCE, hindering = StatKey.SPEED, name = "Sassy")
val Careful = Nature.of(beneficial = StatKey.SPECIAL_DEFENCE, hindering = StatKey.SPECIAL_ATTACK, name = "Careful")

// +Speed natures
val Timid = Nature.of(beneficial = StatKey.SPEED, hindering = StatKey.ATTACK, name = "Timid")
val Hasty = Nature.of(beneficial = StatKey.SPEED, hindering = StatKey.DEFENCE, name = "Hasty")
val Jolly = Nature.of(beneficial = StatKey.SPEED, hindering = StatKey.SPECIAL_ATTACK, name = "Jolly")
val Naive = Nature.of(beneficial = StatKey.SPEED, hindering = StatKey.SPECIAL_DEFENCE, name = "Naive")
