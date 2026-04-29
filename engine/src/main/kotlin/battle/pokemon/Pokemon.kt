package com.drbrosdev.battle.pokemon

import com.drbrosdev.battle.Battle
import com.drbrosdev.battle.item.Item
import com.drbrosdev.battle.move.Move
import com.drbrosdev.battle.move.MoveId
import com.drbrosdev.battle.pokemon.stats.*
import java.util.*

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

    // stats
    val baseStats: BaseStats,
    val effortValues: EffortValues,
    val individualValues: IndividualValues,

    // in battle stats
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
}

fun Pokemon.hasFainted() = inBattleHp.value == 0
fun Pokemon.isBurned() = majorStatus is MajorStatus.Burned
fun Pokemon.isConfused() = volatileStatus.any { it is VolatileStatus.Confusion }
fun Pokemon.isInfatuated() = volatileStatus.any { it is VolatileStatus.Infatuation }

// NOTE: General purpose
fun Pokemon.computeInBattleStats(battle: Battle): EffectiveStats =
    allStatModifications
        .map { it.compute(StatModificationContext(this, battle)) }
        .fold(effectiveStats) { stats, mod -> stats.resolve(mod) }

@JvmInline
value class PokemonId private constructor(val id: String) {
    init {
        require(id.contains("-")) {
            "Pokemon $id has no ownership!"
        }
    }

    override fun toString(): String = id

    companion object {
        operator fun invoke(value: CharSequence): PokemonId {
            val discriminator = UUID.randomUUID()
                .toString()
                .take(4)
            return PokemonId("$value-$discriminator")
        }
    }
}

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
value class Level(val value: Int = CURRENT) {
    init {
        require(value in 1..MAX)
    }

    companion object {
        const val CURRENT = 50
        const val MAX = 100
    }
}

@PokemonDsl
class PokemonBuilder {

    var id: PokemonId = PokemonId("test-pokemon")
    var name: String = "Test Pokemon"
    var level: Level = Level()
    var happiness: Happiness = Happiness()
    var nature: Nature = Quirky
    var ability: Ability = RunAway
    var statModifications: List<StatModification> = emptyList()

    var majorStatus: MajorStatus = MajorStatus.Normal
    var volatileStatus: MutableSet<VolatileStatus> = mutableSetOf()

    private var elements: Elements = Elements(setOf(Element.NORMAL))
    private var baseStats: BaseStats = BaseStats()
    var inBattleHp: Int? = null

    private var effortValues = EffortValues()
    private var individualValues = IndividualValues()

    private var moves: MutableList<Move> = mutableListOf()
    var item: Item = Item.NoItem

    fun elements(vararg elements: Element) {
        this.elements = Elements(elements.toSet())
    }

    fun effortValues(block: EffortValuesBuilder.() -> Unit) {
        val built = EffortValuesBuilder().apply(block).build()
        this.effortValues = built
    }

    fun individualValues(block: IndividualValuesBuilder.() -> Unit) {
        this.individualValues = IndividualValuesBuilder().apply(block).build()
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

    fun pokemonId(value: String) {
        this.id = PokemonId(value)
    }

    fun addStatus(volatileStatus: VolatileStatus) {
        this.volatileStatus.add(volatileStatus)
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
        moves = moves.toList(),
        effortValues = effortValues,
        individualValues = individualValues,
        item = this.item
    ).let {
        when {
            inBattleHp != null -> it.copy(inBattleHp = Stat(inBattleHp!!))
            else -> it
        }
    }
}

fun buildPokemon(block: PokemonBuilder.() -> Unit): Pokemon =
    PokemonBuilder().apply(block).build()
