package com.drbrosdev.battle.move.damagecalc

import com.drbrosdev.battle.Battle
import com.drbrosdev.battle.pokemon.effectiveness
import com.drbrosdev.battle.pokemon.narrativeMessage
import com.drbrosdev.battle.pokemon.registry.AbilityIndex
import com.drbrosdev.battle.pokemon.stats.Stat

fun interface DamageApplication {
    fun apply(
        context: DamageEffectContext,
        damage: Int
    ): Battle
}

class DefaultDamageApplication : DamageApplication {
    override fun apply(
        context: DamageEffectContext,
        damage: Int
    ): Battle = with(context) {
        val canSurviveOhko =
            target.ability == AbilityIndex.lookup["Sturdy"]
        val isOhko = target.effectiveStats.hp == target.inBattleHp && damage >= target.effectiveStats.hp.value

        val updatedHealth = when {
            // in cases where target can survive one-hit-knockouts
            canSurviveOhko && isOhko -> Stat(1)
            // normal damage application
            else -> Stat((target.inBattleHp.value - damage).coerceAtLeast(0))
        }
        val updatedTarget = target.copy(inBattleHp = updatedHealth)

        // get effectiveness for narrative logging
        val effectiveness = effectiveness(move.element, target.elements)
        val narrativeLog = buildString {
            if (effectiveness.narrativeMessage.isNotBlank())
                appendLine(effectiveness.narrativeMessage)
            if (critical)
                appendLine("It's a critical hit!")
            appendLine("The opposing ${target.name} lost $damage health.")
        }

        return battle
            .narrative(narrativeLog)
            .updateMons(updatedTarget)
    }
}
