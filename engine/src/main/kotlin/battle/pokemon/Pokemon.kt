package com.drbrosdev.battle.pokemon

import com.drbrosdev.battle.Battle
import com.drbrosdev.battle.environment.TemporaryEffect
import com.drbrosdev.battle.item.Item
import com.drbrosdev.battle.move.Move
import com.drbrosdev.battle.move.MoveId
import com.drbrosdev.battle.pokemon.stats.*

data class Pokemon(
    // from static config - used w/ lookup
    val name: String,
    val id: PokemonId,

    // from static config
    val elements: Elements,
    val level: Level = Level(),
    val happiness: Happiness = Happiness(),

    // from input
    val nature: Nature,
    // from input
    val ability: Ability,

    // stats, static
    val baseStats: BaseStats,
    // from input
    val effortValues: EffortValues,
    // from input
    val individualValues: IndividualValues,

    val effectiveStats: EffectiveStats = EffectiveStats.from(
        baseStats = baseStats,
        effortValues = effortValues,
        individualValues = individualValues,
        level = level
    ),
    val statModifications: List<StatModification> = emptyList(),
    val inBattleHp: Stat = effectiveStats.hp,
    val majorStatus: MajorStatus = MajorStatus.Normal,
    val volatileStatus: Set<VolatileStatus> = emptySet(),

    // from input
    val moves: List<Move> = emptyList(),
    // from input
    val item: Item = Item.NoItem
) {
    init {
        require(moves.size <= 4) {
            "Pokemon $id cannot have more than 4 moves!"
        }
    }

    // consider all stat modification sources
    val allStatModifications = buildList {
        add(nature.statModification)
        addAll(ability.statModifications)
        add(majorStatus.statModifications())
        add(item.statModification)
        addAll(statModifications)
    }

    operator fun get(moveId: MoveId): Move = requireNotNull(moves.find { it.id == moveId }) {
        "Pokemon $id does not have $moveId assigned!"
    }

    // Mutation functions
    fun clearVolatileStatus() = copy(volatileStatus = emptySet())

    fun enableMoves() = copy(
        moves = moves.map { it.enable() }
    )

    fun choiceMove(moveId: MoveId) = copy(
        moves = moves.map {
            when {
                it.id == moveId -> it.enable()
                else -> it.disable()
            }
        }
    )

    fun modifyStats(modification: StatModification) = copy(
        statModifications = statModifications + modification
    )
}

fun Pokemon.hasFainted() = inBattleHp.value == 0
fun Pokemon.isBurned() = majorStatus is MajorStatus.Burned
fun Pokemon.isConfused() = volatileStatus.any { it is VolatileStatus.Confusion }
fun Pokemon.isInfatuated() = volatileStatus.any { it is VolatileStatus.Infatuation }

// NOTE: General purpose
fun Pokemon.computeInBattleStats(battle: Battle): EffectiveStats {
    val temporaryEffectStatModifications = battle.temporaryEffects(id)
        .filterIsInstance<TemporaryEffect.Tailwind>()
        .map { it.statModification }
    return (allStatModifications + temporaryEffectStatModifications)
        .map { it.compute(StatModificationContext(this, battle)) }
        .fold(effectiveStats) { stats, mod -> stats.resolve(mod) }
}