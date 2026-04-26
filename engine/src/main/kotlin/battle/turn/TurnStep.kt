package com.drbrosdev.battle.turn

import com.drbrosdev.battle.*
import com.drbrosdev.battle.pokemon.MajorStatus
import com.drbrosdev.battle.pokemon.Pokemon
import com.drbrosdev.battle.pokemon.hasFainted
import java.util.logging.Logger
import kotlin.collections.fold

private val LOG = Logger.getLogger(TurnStep::class.qualifiedName)

fun interface TurnStep {
    fun apply(battle: Battle): Battle
}

class ExecuteActionStep(private val context: ActionContext) : TurnStep {

    override fun apply(battle: Battle): Battle = when (context.action) {
        // move execution
        is TurnAction.MoveSelected -> with(context.toMoveContext()) {
            val move = battle[userId][moveId]
            LOG.info { "$userId is attempting to use ${move.name}" }
            move.effect.run { apply(battle) }
        }

        is TurnAction.Switch -> {
            val incoming = battle[context.action.incoming]
            LOG.info { "${context.user.id} is attempting to switch with ${incoming.id}" }
            battle.switch(user = context.user, target = incoming)
        }
    }

}

class ApplyStartOfTurnEffects(private val context: ActionContext) : TurnStep {
    override fun apply(battle: Battle): Battle = when (context.action) {
        // start of turn effects do not apply when switching
        is TurnAction.Switch -> battle

        is TurnAction.MoveSelected -> {
            val updated = computeVolatileAndSelfHealingStatus(
                pokemon = battle[context.user.id],
                turnCount = battle.turnCount
            )
            battle.updateMons(updated)
        }
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
        val user = battle[context.user.id]
        if (user.hasFainted()) {
            return battle
        }
        val applicableEffects = buildList {
            // weather
            if (battle.weather == Weather.SANDSTORM) {
                add(SandstormEndOfTurnEffect)
            }
            if (battle.weather == Weather.HAIL) {
                add(HailEndOfTurnEffect)
            }
            // held item support for Leftovers etc
            // TODO: waiting for item support
            // major status condition
            add(PoisonEndOfTurnEffect)
            add(BadPoisonEndOfTurnEffect)
            add(BurnEndOfTurnEffect)
        }

        return battle.updateMons(
            applicableEffects.fold(user) { pokemon, effect -> effect.apply(pokemon) },
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

