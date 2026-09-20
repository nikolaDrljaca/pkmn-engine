package battle.turn

import com.drbrosdev.battle.item.Item
import com.drbrosdev.battle.item.ItemId
import com.drbrosdev.battle.move.buildMove
import com.drbrosdev.battle.pokemon.PokemonId
import com.drbrosdev.battle.pokemon.buildPokemon
import com.drbrosdev.battle.turn.QuickClawRuleComparator
import com.drbrosdev.battle.turn.TurnAction
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertEquals

class QuickClawRuleComparatorTest {
    private val move = buildMove {}

    private fun quickClawPokemon(id: String) = buildPokemon {
        this.id = PokemonId(id)
        item = Item(ItemId("quick-claw"), "Quick Claw")
    }

    @Test
    fun `(quick-claw-rule) both holders quick-claw roll true, coinFlip true returns -1`() {
        val pokemonA = quickClawPokemon("a")
        val pokemonB = quickClawPokemon("b")
        val actionA = TurnAction.MoveSelected.of(move, pokemonA)
        val actionB = TurnAction.MoveSelected.of(move, pokemonB)
        val comparator = QuickClawRuleComparator(coinFlip = true, roll = true)
        assertEquals(-1, comparator.compare(actionA, actionB))
    }

    @Test
    fun `(quick-claw-rule) both holders quick-claw roll true, coinFlip false returns 1`() {
        val pokemonA = quickClawPokemon("a")
        val pokemonB = quickClawPokemon("b")
        val actionA = TurnAction.MoveSelected.of(move, pokemonA)
        val actionB = TurnAction.MoveSelected.of(move, pokemonB)
        val comparator = QuickClawRuleComparator(coinFlip = false, roll = true)
        assertEquals(1, comparator.compare(actionA, actionB))
    }

    @Test
    fun `(quick-claw-rule) only one holder quick-claw roll true returns -1`() {
        val pokemonA = quickClawPokemon("a")
        val pokemonB = buildPokemon { id = PokemonId("b") }
        val actionA = TurnAction.MoveSelected.of(move, pokemonA)
        val actionB = TurnAction.MoveSelected.of(move, pokemonB)
        val comparator = QuickClawRuleComparator(coinFlip = true, roll = true)
        assertEquals(-1, comparator.compare(actionA, actionB))
    }

    @Test
    fun `(quick-claw-rule) neither holder quick-claw roll true returns 0`() {
        val pokemonA = buildPokemon { id = PokemonId("a") }
        val pokemonB = buildPokemon { id = PokemonId("b") }
        val actionA = TurnAction.MoveSelected.of(move, pokemonA)
        val actionB = TurnAction.MoveSelected.of(move, pokemonB)
        val comparator = QuickClawRuleComparator(coinFlip = true, roll = true)
        assertEquals(0, comparator.compare(actionA, actionB))
    }

    @Test
    fun `(quick-claw-rule) both holders quick-claw roll false returns 0`() {
        val pokemonA = quickClawPokemon("a")
        val pokemonB = quickClawPokemon("b")
        val actionA = TurnAction.MoveSelected.of(move, pokemonA)
        val actionB = TurnAction.MoveSelected.of(move, pokemonB)
        val comparator = QuickClawRuleComparator(coinFlip = true, roll = false)
        assertEquals(0, comparator.compare(actionA, actionB))
    }
}
