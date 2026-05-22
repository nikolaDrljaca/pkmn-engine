package com.drbrosdev.battle.pokemon.registry

import com.drbrosdev.battle.pokemon.Adamant
import com.drbrosdev.battle.pokemon.Bashful
import com.drbrosdev.battle.pokemon.Bold
import com.drbrosdev.battle.pokemon.Brave
import com.drbrosdev.battle.pokemon.Calm
import com.drbrosdev.battle.pokemon.Careful
import com.drbrosdev.battle.pokemon.Docile
import com.drbrosdev.battle.pokemon.Gentle
import com.drbrosdev.battle.pokemon.Hardy
import com.drbrosdev.battle.pokemon.Hasty
import com.drbrosdev.battle.pokemon.Impish
import com.drbrosdev.battle.pokemon.Jolly
import com.drbrosdev.battle.pokemon.Lax
import com.drbrosdev.battle.pokemon.Lonely
import com.drbrosdev.battle.pokemon.Mild
import com.drbrosdev.battle.pokemon.Modest
import com.drbrosdev.battle.pokemon.Naive
import com.drbrosdev.battle.pokemon.Naughty
import com.drbrosdev.battle.pokemon.Quiet
import com.drbrosdev.battle.pokemon.Quirky
import com.drbrosdev.battle.pokemon.Rash
import com.drbrosdev.battle.pokemon.Relaxed
import com.drbrosdev.battle.pokemon.Sassy
import com.drbrosdev.battle.pokemon.Serious
import com.drbrosdev.battle.pokemon.Timid

object NatureIndex {
    val lookup = mapOf(
        // Neutral
        Hardy.name to Hardy,
        Docile.name to Docile,
        Serious.name to Serious,
        Bashful.name to Bashful,
        Quirky.name to Quirky,
        // +Attack
        Lonely.name to Lonely,
        Brave.name to Brave,
        Adamant.name to Adamant,
        Naughty.name to Naughty,
        // +Defence
        Bold.name to Bold,
        Relaxed.name to Relaxed,
        Impish.name to Impish,
        Lax.name to Lax,
        // +Sp. Attack
        Modest.name to Modest,
        Mild.name to Mild,
        Quiet.name to Quiet,
        Rash.name to Rash,
        // +Sp. Defence
        Calm.name to Calm,
        Gentle.name to Gentle,
        Sassy.name to Sassy,
        Careful.name to Careful,
        // +Speed
        Timid.name to Timid,
        Hasty.name to Hasty,
        Jolly.name to Jolly,
        Naive.name to Naive,
    )
}