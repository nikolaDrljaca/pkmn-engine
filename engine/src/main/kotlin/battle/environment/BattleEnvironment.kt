package com.drbrosdev.battle.environment

import com.drbrosdev.battle.TeamId
import com.drbrosdev.battle.environment.hazard.EntryHazard


data class BattleEnvironment(
    private val entryHazards: Map<TeamId, Set<EntryHazard>> = emptyMap(),
) {
    fun entryHazards(teamId: TeamId): Set<EntryHazard> =
        entryHazards[teamId].orEmpty()

    fun withEntryHazard(teamId: TeamId, hazard: EntryHazard) = copy(
        entryHazards = entryHazards + (teamId to setOf(hazard))
    )
}

