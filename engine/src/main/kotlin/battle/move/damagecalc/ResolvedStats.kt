package com.drbrosdev.battle.move.damagecalc

import com.drbrosdev.battle.pokemon.stats.Stat

data class ResolvedStats(
    val attackStat: Stat,
    val defenceStat: Stat,
)