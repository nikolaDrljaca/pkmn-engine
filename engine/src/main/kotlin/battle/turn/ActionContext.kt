package com.drbrosdev.battle.turn

import com.drbrosdev.battle.Battle
import com.drbrosdev.battle.TeamId
import com.drbrosdev.battle.move.MoveContext

data class ActionContext(
    val user: TeamId,
    val target: TeamId,
    val action: TurnAction
)

fun ActionContext.toMoveContext(battle: Battle): MoveContext = when (action) {
    is TurnAction.MoveSelected -> MoveContext(
        userId = battle[user].id,
        targetId = battle[target].id,
        moveId = action.move.id
    )

    else -> error("Cannot create MoveContext when action is ${action.javaClass.simpleName}!")
}
