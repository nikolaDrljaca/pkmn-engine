package com.drbrosdev.battle.turn

import com.drbrosdev.battle.*
import com.drbrosdev.battle.environment.SwitchInEffect
import com.drbrosdev.battle.environment.SwitchOutEffect
import com.drbrosdev.battle.environment.Weather
import com.drbrosdev.battle.environment.exitNarrativeMessage
import com.drbrosdev.battle.pokemon.MajorStatus
import com.drbrosdev.battle.pokemon.Pokemon
import com.drbrosdev.battle.pokemon.PokemonId
import com.drbrosdev.battle.pokemon.exitNarrativeMessage
import com.drbrosdev.battle.pokemon.hasFainted
import java.util.logging.Logger

private val LOG = Logger.getLogger(TurnStep::class.qualifiedName)

fun interface TurnStep {
    fun apply(battle: Battle): Battle
}

class ExecuteActionStep(private val context: ActionContext) : TurnStep {

    override fun apply(battle: Battle): Battle = when (context.action) {
        // move execution
        is TurnAction.MoveSelected -> with(context.toMoveContext(battle)) {
            val user = battle[userId]
            val target = battle[targetId]
            val move = user[moveId]
            LOG.fine { "${user.name} used ${move.name}!" }
            val applicableEffects = buildList {
                add(user.item.preMoveEffect)
                add(move.effect)
                // NOTE: support for abilities like Rough Skin and Iron Barbs
                addAll(target.ability.moveEffects)
            }

            battle
                // log narrative message
                .narrative("${user.name} used ${move.name}!")
                // resolve move
                .let {
                    applicableEffects.fold(it) { inner, effect ->
                        effect.run { apply(inner) }
                    }
                }
        }

        // switch action, applies switch out and switch in effects
        is TurnAction.Switch -> {
            val incoming = battle[context.action.incoming]
            val outgoing = battle[context.user]
            LOG.fine { "${outgoing.id} is switching with ${incoming.id}" }
            // TODO: group these so they can be applied for moves like U-Turn
            // switch out effects are applied to outgoing
            val afterOutEffects = with(SwitchOutEffect) { apply(outgoing) }
            // switch in effects are applied to incoming
            val environment = battle.environment(incoming.id)
            val afterInEffects = with(SwitchInEffect(environment)) { apply(incoming) }

            battle
                .narrative("${outgoing.name} is switching with ${incoming.name}.")
                .updateMons(afterOutEffects, afterInEffects)
                .switch(outgoing.id, incoming.id)
        }
    }

}

class ResolveStartOfTurnWeather: TurnStep {
    override fun apply(battle: Battle): Battle {
        // resolve weather
        val expiresOnTurn = battle.weather.expiresOnTurn ?: return battle
        return when {
            battle.turnCount > expiresOnTurn -> battle
                .narrative(battle.weather.exitNarrativeMessage)
                .copy(weather = Weather.None)

            else -> battle
        }
    }
}


class ApplyStartOfTurnEffects(private val context: ActionContext) : TurnStep {
    override fun apply(battle: Battle): Battle = when (context.action) {
        // start of turn effects do not apply when switching
        is TurnAction.Switch -> battle

        is TurnAction.MoveSelected -> {
            val pokemon = battle[context.user]
            battle
                // resolve pokemon volatile and self-healing status
                .let { resolveUserStatus(pokemon.id, it) }
                // resolve environment effect which can expire
                .let { resolveEnvironmentUnits(pokemon.id, it) }
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
            target = pokemonId,
            *updatedEnv.toTypedArray()
        )
    }

    private fun resolveUserStatus(userId: PokemonId, battle: Battle): Battle {
        // handle self-healing major status
        val user = battle[userId]
        val afterMajor = when (val status = user.majorStatus) {
            is MajorStatus.Asleep -> {
                val recovered = battle.turnCount > status.expiresOnTurn
                val newStatus = if (recovered) MajorStatus.Normal else status
                val updatedBattle = if (recovered) battle.narrative(status.exitNarrativeMessage(user.name)) else battle
                updatedBattle.updateMons(user.copy(majorStatus = newStatus))
            }

            is MajorStatus.Frozen -> {
                val thawed = MajorStatus.shouldThaw()
                val newStatus = if (thawed) MajorStatus.Normal else status
                val updatedBattle = if (thawed) battle.narrative(status.exitNarrativeMessage(user.name)) else battle
                updatedBattle.updateMons(user.copy(majorStatus = newStatus))
            }

            else -> battle
        }
        // handle volatile status
        return user.volatileStatus.fold(afterMajor) { current, volatile ->
            when {
                current.turnCount < volatile.expiresOnTurn -> {
                    val updatedMon = user.copy(
                        volatileStatus = user.volatileStatus
                            .filter { it != volatile }
                            .toSet()
                    )
                    current
                        .narrative(volatile.exitNarrativeMessage(user.name))
                        .updateMons(updatedMon)
                }

                else -> current
            }
        }
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
            add(battle.weather.endOfTurnEffect)
            // held item support for Leftovers etc
            add(user.item.endOfTurnEffect)
            // major status condition
            add(PoisonEndOfTurnEffect)
            add(BadPoisonEndOfTurnEffect)
            add(BurnEndOfTurnEffect)
        }

        return applicableEffects
            .fold(battle to user) { (currentBattle, currentPokemon), effect ->
                val result = effect.apply(currentPokemon)
                val updatedBattle = currentBattle.narrative(result.narrativeMessage)
                updatedBattle to result.pokemon
            }
            .let { (finalBattle, finalPokemon) ->
                finalBattle.updateMons(finalPokemon)
            }
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

