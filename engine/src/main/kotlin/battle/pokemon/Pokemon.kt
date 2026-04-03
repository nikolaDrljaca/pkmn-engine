package com.drbrosdev.battle.pokemon

import com.drbrosdev.battle.Battle
import com.drbrosdev.battle.move.Move
import com.drbrosdev.battle.pokemon.stats.BaseStats
import com.drbrosdev.battle.pokemon.stats.BaseStatsBuilder
import com.drbrosdev.battle.pokemon.stats.Stat
import com.drbrosdev.battle.pokemon.stats.StatModification
import com.drbrosdev.battle.pokemon.stats.StatModificationContext
import com.drbrosdev.battle.pokemon.stats.resolve
import java.util.UUID

data class Pokemon(
    // from static config - used w/ lookup
    val name: String,
    // from input - derived
    val id: String,

    // from static config
    val elements: Elements,
    val level: Level = Level(50),
    val happiness: Happiness = Happiness(),

    // from input
    val nature: Nature,
    // from input
    val ability: Ability,

    // from static config
    val baseStats: BaseStats,
    val statModifications: List<StatModification> = emptyList(),

    val inBattleHp: Stat = baseStats.hp,

    val majorStatus: MajorStatus = MajorStatus.Normal,
    val volatileStatus: Set<VolatileStatus> = emptySet(),

    // from input
    val moves: List<Move> = emptyList()
) {
    init {
        require(id.contains("-")) {
            "Pokemon with no ownership! ID: $id"
        }
    }
    // consider all stat modification sources
    val allStatModifications = buildList {
        add(StatModification { nature.changes })
        addAll(ability.statModifications)
        if (majorStatus is MajorStatus.Paralyzed) {
            add(majorStatus.modification)
        }
        addAll(statModifications)
    }
}

fun Pokemon.hasFainted() = inBattleHp.value == 0

fun Pokemon.computeInBattleStats(battle: Battle): BaseStats =
    allStatModifications
        .map { it.compute(StatModificationContext(this, battle)) }
        .fold(baseStats) { stats, mod -> stats.resolve(mod) }


@JvmInline
value class Happiness(val value: Int = BASE_VALUE) {
    init {
        require(value in RANGE)
    }

    companion object {
        const val BASE_VALUE = 50
        val RANGE = 0..255
    }
}

@JvmInline
value class Level(val value: Int) {
    init {
        require(value in 1..100)
    }
}

@PokemonDsl
class PokemonBuilder {

    var id: String = "test-pokemon"
    var name: String = "Test Pokemon"
    var level: Level = Level(50)
    var happiness: Happiness = Happiness()
    var nature: Nature = Quirky
    var ability: Ability = RunAway
    var majorStatus: MajorStatus = MajorStatus.Normal
    var volatileStatus: Set<VolatileStatus> = emptySet()
    var statModifications: List<StatModification> = emptyList()

    private var elements: Elements = Elements(setOf(Element.NORMAL))
    private var baseStats: BaseStats = BaseStats()
    var inBattleHp: Int? = null

    private var moves: MutableList<Move> = mutableListOf()

    fun elements(vararg elements: Element) {
        this.elements = Elements(elements.toSet())
    }

    fun baseStats(block: BaseStatsBuilder.() -> Unit) {
        val built = BaseStatsBuilder().apply(block).build()
        this.baseStats = built
    }

    fun addMove(move: Move) {
        this.moves.add(move)
    }

    fun addMoves(vararg moves: Move) {
        this.moves = moves.toMutableList()
    }

    fun build() = Pokemon(
        id = id,
        name = name,
        elements = elements,
        level = level,
        happiness = happiness,
        nature = nature,
        ability = ability,
        baseStats = baseStats,
        statModifications = statModifications,
        majorStatus = majorStatus,
        volatileStatus = volatileStatus,
        moves = moves.toList()
    ).let {
        when {
            inBattleHp != null -> it.copy(inBattleHp = Stat(inBattleHp!!))
            else -> it
        }
    }
}

fun buildPokemon(block: PokemonBuilder.() -> Unit): Pokemon =
    PokemonBuilder().apply(block).build()