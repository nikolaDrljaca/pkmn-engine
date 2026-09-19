package com.drbrosdev.battle.environment

import com.drbrosdev.battle.TeamId
import com.drbrosdev.battle.environment.hazard.EntryHazard


data class BattleEnvironment(
    private val entryHazards: Map<TeamId, Set<EntryHazard>> = emptyMap(),
    private val temporaryEffects: Map<TeamId, Set<TemporaryEffect>> = emptyMap()
) {
    fun entryHazards(teamId: TeamId): Set<EntryHazard> =
        entryHazards[teamId].orEmpty()

    fun withEntryHazard(teamId: TeamId, hazard: EntryHazard) = copy(
        entryHazards = entryHazards + (teamId to setOf(hazard))
    )

    fun temporaryEffects(teamId: TeamId) =
        temporaryEffects[teamId].orEmpty()

    fun withTemporaryEffect(teamId: TeamId, effect: TemporaryEffect) = copy(
        temporaryEffects = temporaryEffects + (teamId to setOf(effect))
    )

    fun withTemporaryEffects(teamId: TeamId, effect: Set<TemporaryEffect>) = copy(
        temporaryEffects = temporaryEffects + (teamId to effect)
    )
}

