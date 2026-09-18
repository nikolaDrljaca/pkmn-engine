package com.drbrosdev.battle.environment

import com.drbrosdev.battle.pokemon.Effectiveness
import com.drbrosdev.battle.pokemon.Element
import com.drbrosdev.battle.pokemon.MagicGuard
import com.drbrosdev.battle.pokemon.effectiveness
import com.drbrosdev.battle.pokemon.stats.Stat
import java.util.logging.Logger

private val LOG = Logger.getLogger(EnvironmentUnit::class.qualifiedName)

/**
 * [EnvironmentUnit.expiresOnTurn] models environment units which can expire
 * such as Light Screen and Reflect.
 */
data class EnvironmentUnit(
    val id: String,
    val expiresOnTurn: Int? = null,
    val effect: EnvironmentEffect = EnvironmentEffect.NoEffect,
    val name: String = "",
    val enterNarrativeMessage: String = "",
    val exitNarrativeMessage: String = ""
)

