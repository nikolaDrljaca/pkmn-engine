package com.drbrosdev.battle.move.damagecalc

import com.drbrosdev.battle.move.MoveType
import com.drbrosdev.battle.pokemon.stats.StatKey

data class StatSelection(val attacker: StatKey, val defender: StatKey) {
    companion object {
        val Default = StatSelection(StatKey.ATTACK, StatKey.DEFENCE)
    }
}

/*
TODO: Maybe this can only depend on the selected Move?
It might not need the entire battle state?
 */
fun interface StatSelector {
    fun select(context: DamageEffectContext): StatSelection?
}

class DefaultStatSelector(
    private val strategies: List<StatSelector> = listOf(
        PsyshockStatSelection,
        NormalStatSelection
    )
) : StatSelector {
    override fun select(context: DamageEffectContext): StatSelection {
        val selection = strategies.firstNotNullOfOrNull { strategy -> strategy.select(context) }
        return requireNotNull(selection) {
            "DefaultStatSelector must resolve a stat selection!"
        }
    }
}

object NormalStatSelection : StatSelector {
    override fun select(context: DamageEffectContext): StatSelection {
        return when (context.move.type) {
            MoveType.PHYSICAL -> StatSelection(
                attacker = StatKey.ATTACK,
                defender = StatKey.DEFENCE
            )

            MoveType.SPECIAL -> StatSelection(
                attacker = StatKey.SPECIAL_ATTACK,
                defender = StatKey.SPECIAL_DEFENCE
            )

            MoveType.STATUS -> error("Cannot apply stat resolution for STATUS moves!")
        }
    }
}

object PsyshockStatSelection : StatSelector {
    override fun select(context: DamageEffectContext): StatSelection? {
        return when {
            context.move.id.id == "psyshock" -> StatSelection(
                attacker = StatKey.SPECIAL_ATTACK,
                defender = StatKey.DEFENCE
            )

            else -> null
        }
    }
}
