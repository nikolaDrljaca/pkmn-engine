package com.drbrosdev.battle.pokemon

import com.drbrosdev.battle.item.Item
import com.drbrosdev.battle.move.Move
import com.drbrosdev.battle.pokemon.stats.BaseStats
import com.drbrosdev.battle.pokemon.stats.BaseStatsBuilder
import com.drbrosdev.battle.pokemon.stats.EffortValues
import com.drbrosdev.battle.pokemon.stats.EffortValuesBuilder
import com.drbrosdev.battle.pokemon.stats.IndividualValues
import com.drbrosdev.battle.pokemon.stats.IndividualValuesBuilder
import com.drbrosdev.battle.pokemon.stats.Stat
import com.drbrosdev.battle.pokemon.stats.StatKey
import com.drbrosdev.battle.pokemon.stats.StatModification

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

    fun effortValue(value: Int, key: StatKey) {
        this.effortValues = EffortValues(this.effortValues.stats + (key to Stat(value)))
    }

    fun individualValues(block: IndividualValuesBuilder.() -> Unit) {
        this.individualValues = IndividualValuesBuilder().apply(block).build()
    }

    fun individualValue(value: Int, key: StatKey) {
        this.individualValues = IndividualValues(this.individualValues.stats + (key to Stat(value)))
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

    fun staticConfig(pokemon: Pokemon) {
        id = pokemon.id
        elements = pokemon.elements
        name = pokemon.name
        baseStats = pokemon.baseStats
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
