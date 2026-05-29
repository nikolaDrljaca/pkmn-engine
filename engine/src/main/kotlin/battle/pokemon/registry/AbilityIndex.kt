package com.drbrosdev.battle.pokemon.registry

import com.drbrosdev.battle.pokemon.BattleArmor
import com.drbrosdev.battle.pokemon.ClearBody
import com.drbrosdev.battle.pokemon.HyperCutter
import com.drbrosdev.battle.pokemon.IceBody
import com.drbrosdev.battle.pokemon.Intimidate
import com.drbrosdev.battle.pokemon.IronBarbs
import com.drbrosdev.battle.pokemon.Justified
import com.drbrosdev.battle.pokemon.Levitate
import com.drbrosdev.battle.pokemon.MagicGuard
import com.drbrosdev.battle.pokemon.Overcoat
import com.drbrosdev.battle.pokemon.Overgrow
import com.drbrosdev.battle.pokemon.OwnTempo
import com.drbrosdev.battle.pokemon.PoisonHeal
import com.drbrosdev.battle.pokemon.Prankster
import com.drbrosdev.battle.pokemon.Pressure
import com.drbrosdev.battle.pokemon.RoughSkin
import com.drbrosdev.battle.pokemon.RunAway
import com.drbrosdev.battle.pokemon.SandForce
import com.drbrosdev.battle.pokemon.SandRush
import com.drbrosdev.battle.pokemon.SandStream
import com.drbrosdev.battle.pokemon.SandVeil
import com.drbrosdev.battle.pokemon.Scrappy
import com.drbrosdev.battle.pokemon.ShellArmor
import com.drbrosdev.battle.pokemon.SnowCloak
import com.drbrosdev.battle.pokemon.StrongJaw
import com.drbrosdev.battle.pokemon.Sturdy
import com.drbrosdev.battle.pokemon.WhiteSmoke

object AbilityIndex {
    val lookup = mapOf(
        Sturdy.name to Sturdy,
        Justified.name to Justified,
        RoughSkin.name to RoughSkin,
        PoisonHeal.name to PoisonHeal,
        Overgrow.name to Overgrow,
        Prankster.name to Prankster,
        RunAway.name to RunAway,
        Pressure.name to Pressure,
        OwnTempo.name to OwnTempo,
        Scrappy.name to Scrappy,
        SandStream.name to SandStream,
        Levitate.name to Levitate,
        IronBarbs.name to IronBarbs,
        Intimidate.name to Intimidate,
        SandVeil.name to SandVeil,
        SandRush.name to SandRush,
        SandForce.name to SandForce,
        MagicGuard.name to MagicGuard,
        IceBody.name to IceBody,
        Overcoat.name to Overcoat,
        SnowCloak.name to SnowCloak,
        ShellArmor.name to ShellArmor,
        StrongJaw.name to StrongJaw,
        ClearBody.name to ClearBody,
        HyperCutter.name to HyperCutter,
        WhiteSmoke.name to WhiteSmoke,
        BattleArmor.name to BattleArmor,
    )
}