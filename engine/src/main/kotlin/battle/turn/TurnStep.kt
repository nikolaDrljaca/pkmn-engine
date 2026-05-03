package com.drbrosdev.battle.turn

import com.drbrosdev.battle.*
import com.drbrosdev.battle.environment.SwitchInEffect
import com.drbrosdev.battle.environment.SwitchOutEffect
import com.drbrosdev.battle.pokemon.MajorStatus
import com.drbrosdev.battle.pokemon.Pokemon
import com.drbrosdev.battle.pokemon.PokemonId
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
            val user = battle[userId]
            val move = user[moveId]
            LOG.info { "${user.name} used ${move.name}!" }
            val applicableEffects = buildList {
                add(user.item.preMoveEffect)
                add(move.effect)
            }
            applicableEffects.fold(battle) { inner, effect ->
                effect.run { apply(inner) }
            }
        }

        // switch action, applies switch out and switch in effects
        is TurnAction.Switch -> {
            val incoming = battle[context.action.incoming]
            val outgoing = battle[context.user]
            LOG.info { "${context.user.id} is switching with ${incoming.id}" }
            // switch out effects are applied to outgoing
            val afterOutEffects = with(SwitchOutEffect) { apply(outgoing) }
            // switch in effects are applied to incoming
            val environment = battle.environment(incoming.id)
            val afterInEffects = with(SwitchInEffect(environment)) { apply(incoming) }
            val updatedBattle = battle.updateMons(afterOutEffects, afterInEffects)
            // perform switch
            updatedBattle.switch(
                user = context.user,
                target = context.action.incoming
            )
        }
    }

}

class ApplyStartOfTurnEffects(private val context: ActionContext) : TurnStep {
    override fun apply(battle: Battle): Battle = when (context.action) {
        // start of turn effects do not apply when switching
        is TurnAction.Switch -> battle

        is TurnAction.MoveSelected -> {
            battle
                // resolve pokemon volatile and self-healing status
                .let { resolveVolatileStatus(it[context.user], it.turnCount) }
                .let { battle.updateMons(it) }
                // resolve environment effect which can expire
                .let { resolveEnvironmentUnits(context.user, it) }
        }
    }

    private fun resolveEnvironmentUnits(
        pokemonId: PokemonId,
        battle: Battle
    ): Battle {
        val updatedEnv = battle.environment(pokemonId)
            .filter { it.expiresOnTurn != null }
            .filter { battle.turnCount < it.expiresOnTurn!! }
            .toSet()
        return battle.updateEnvironment(
            target = context.user,
            *updatedEnv.toTypedArray()
        )
    }

    private fun resolveVolatileStatus(
        pokemon: Pokemon,
        turnCount: Int
    ): Pokemon = with(pokemon) {
        // resolve self-healing major status
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
        // resolve volatile status
        afterMajor.copy(
            volatileStatus = volatileStatus
                .filter { turnCount < it.expiresOnTurn }
                .toSet()
        )
    }
}

class ApplyEndOfTurnEffects(private val context: ActionContext) : TurnStep {
    override fun apply(battle: Battle): Battle {
        val user = battle[context.user]
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
            add(user.item.endOfTurnEffect)
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

