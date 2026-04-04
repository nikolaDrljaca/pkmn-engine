package com.drbrosdev


class BattleEngine {
    // TODO
    // the engine will construct the Turn and pass it to TurnResolution

    /*
    1. Command parsing - parse strings to create executable engine commands (TurnActions)
    2. Battle session management ko

    Battles can have a unique ID in {BattleId-PlayerId} or similar
    All static for now

    Public API
    createBattle(id: String/UUID): BattleId
    createBattle(team1, team2): BattleId // using IDs - this is optional now
    executeCommand(command: String)

    supported commands:
    view / display active mons w/ health status and list teams
    p1 move {move-id}
    p1 switch {pokemon-id}
    p1 view pkmn {pokemon-id}
    p1 view move {move-id}
    p1 view p2 (to list opponents current active mon w/health status)
    p1 surrender
     */
}