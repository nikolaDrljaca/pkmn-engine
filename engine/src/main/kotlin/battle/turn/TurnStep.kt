package com.drbrosdev.battle.turn

import com.drbrosdev.battle.Battle
import com.drbrosdev.battle.BattleOutcome
import com.drbrosdev.battle.BattleState
import com.drbrosdev.battle.Weather
import com.drbrosdev.battle.allFainted
import com.drbrosdev.battle.pokemon.Pokemon
import com.drbrosdev.battle.pokemon.hasFainted
import com.drbrosdev.battle.turn.HailEndOfTurnEffect
import com.drbrosdev.battle.turn.LeftoversEndOfTurnEffect

fun interface TurnStep {
    fun apply(battle: Battle): Battle
}

class ExecuteActionStep(private val context: ActionContext) : TurnStep {

    override fun apply(battle: Battle): Battle = when (context.action) {
        is TurnAction.MoveSelected -> {
            val startOfTurnEffects = ApplyStartOfTurnEffects(context)
            val afterEffectBattle = startOfTurnEffects.apply(battle)
            // move execution
            with(context.toMoveContext()) {
                with(context.action.move.effect) {
                    apply(afterEffectBattle)
                }
            }
        }

        is TurnAction.Switch -> with(context) {
            battle.switch(user = user, target = target)
        }
    }

}

class ApplyStartOfTurnEffects(private val context: ActionContext) : TurnStep {
    override fun apply(battle: Battle): Battle {
        // clear all volatile status conditions which have expired
        val cleared1 = with(battle.pokemon1) {
            copy(
                volatileStatus = volatileStatus
                    .filter { battle.turnCount < it.expiresOnTurn }
                    .toSet()
            )
        }
        val cleared2 = with(battle.pokemon2) {
            copy(
                volatileStatus = volatileStatus
                    .filter { battle.turnCount < it.expiresOnTurn }
                    .toSet()
            )
        }

        return battle.updateMons(cleared1, cleared2)
    }
}

class ApplyEndOfTurnEffects(private val context: ActionContext) : TurnStep {

    override fun apply(battle: Battle): Battle {
        val applicableEffects = buildList {
            // weather
            if (battle.weather == Weather.SANDSTORM) {
                add(SandstormEndOfTurnEffect)
            }
            if (battle.weather == Weather.HAIL) {
                add(HailEndOfTurnEffect)
            }
            // held item
            add(LeftoversEndOfTurnEffect)
            add(BlackSludgeEndOfTurnEffect)
            // major status condition
            add(PoisonEndOfTurnEffect)
            add(BadPoisonEndOfTurnEffect)
            add(BurnEndOfTurnEffect)
        }
        return battle.updateMons(
            applicableEffects.fold(battle.pokemon1) { pokemon, effect -> effect.apply(pokemon) },
            applicableEffects.fold(battle.pokemon2) { pokemon, effect -> effect.apply(pokemon) }
        )
    }

}

val CheckConclusion = TurnStep { battle ->
    // is any mon fainted
    when {
        battle.team1.allFainted() && battle.team2.allFainted() -> battle.copy(
            state = BattleState.Concluded(
                outcome = BattleOutcome.Draw
            )
        )

        battle.team1.allFainted() -> battle.copy(
            state = BattleState.Concluded(
                outcome = BattleOutcome.Winner(battle.team2)
            )
        )

        battle.team2.allFainted() -> battle.copy(
            state = BattleState.Concluded(
                outcome = BattleOutcome.Winner(battle.team1)
            )
        )

        else -> battle.copy(state = BattleState.InProgress)
    }
}

val HandleTurnCounter = TurnStep { battle ->
    battle.copy(turnCount = battle.turnCount + 1)
}

/*
Pipeline Design pattern
Execute-all pipeline.
All steps run unconditionally.
Implementations decide to return a new state of the battle
 */
fun Battle.resolveTurnSteps(steps: Sequence<TurnStep>): Battle {
    return steps.fold(this) { currentBattle, step ->
        when (currentBattle.state) {
            is BattleState.Concluded -> currentBattle
            else -> step.apply(currentBattle)
        }
    }
}

