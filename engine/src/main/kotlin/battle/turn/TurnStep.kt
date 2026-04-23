package com.drbrosdev.battle.turn

import com.drbrosdev.battle.*
import com.drbrosdev.battle.pokemon.MajorStatus
import com.drbrosdev.battle.pokemon.Pokemon
import java.util.logging.Logger

private val LOG = Logger.getLogger(TurnStep::class.qualifiedName)

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
                val move = afterEffectBattle[userId][moveId]
                LOG.info { "$userId is attempting to execute ${move.name}" }
                move.effect.run { apply(afterEffectBattle) }
            }
        }

        is TurnAction.Switch -> {
            val incoming = battle[context.action.incoming]
            LOG.info { "${context.user.id} is attempting to switch with ${incoming.id}" }
            battle.switch(user = context.user, target = incoming)
        }
    }

}

class ApplyStartOfTurnEffects(private val context: ActionContext) : TurnStep {
    override fun apply(battle: Battle): Battle {
        val cleared1 = computeVolatileAndSelfHealingStatus(
            pokemon = battle.pokemon1,
            turnCount = battle.turnCount
        )
        val cleared2 = computeVolatileAndSelfHealingStatus(
            pokemon = battle.pokemon2,
            turnCount = battle.turnCount
        )
        return battle.updateMons(cleared1, cleared2)
    }

    // Clear all volatile status conditions which have expired
    // MajorStatus Asleep and Frozen need to be checked here
    // since they are self-healing.
    private fun computeVolatileAndSelfHealingStatus(
        pokemon: Pokemon,
        turnCount: Int
    ): Pokemon = with(pokemon) {
        val afterMajor = when (majorStatus) {
            is MajorStatus.Asleep -> {
                val newMajorStatus = when {
                    turnCount > majorStatus.expiresOnTurn -> MajorStatus.Normal
                    else -> majorStatus
                }
                copy(majorStatus = newMajorStatus)
            }

            is MajorStatus.Frozen -> {
                val newStatus = when {
                    MajorStatus.shouldThaw() -> MajorStatus.Normal
                    else -> majorStatus
                }

                copy(majorStatus = newStatus)
            }

            else -> this
        }

        afterMajor.copy(
            volatileStatus = volatileStatus
                .filter { turnCount < it.expiresOnTurn }
                .toSet()
        )
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
            // TODO Waiting for item support
//            add(LeftoversEndOfTurnEffect)
//            add(BlackSludgeEndOfTurnEffect)
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
            is BattleState.Concluded -> {
                LOG.info { "Battle has concluded at turn $turnCount with outcome ${currentBattle.state.outcome}" }
                currentBattle
            }
            else -> step.apply(currentBattle)
        }
    }
}

