package battle.turn

import com.drbrosdev.battle.move.buildMove
import com.drbrosdev.battle.pokemon.buildPokemon
import com.drbrosdev.battle.turn.PursuitRuleComparator
import com.drbrosdev.battle.turn.TurnAction
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertEquals

class PursuitRuleComparatorTest {
    private val pokemonA = buildPokemon { pokemonId("a") }
    private val pokemonB = buildPokemon { pokemonId("b") }
    private val pursuitMove = buildMove { id = "pursuit" }
    private val otherMove = buildMove { id = "tackle" }

    @Test
    fun `(pursuit-rule) pursuit vs switch returns -1`() {
        val pursuitAction = TurnAction.MoveSelected.of(pursuitMove, pokemonA)
        val switchAction = TurnAction.Switch.of(pokemonB, pokemonA)
        assertEquals(-1, PursuitRuleComparator.compare(pursuitAction, switchAction))
    }

    @Test
    fun `(pursuit-rule) switch vs pursuit returns 1`() {
        val pursuitAction = TurnAction.MoveSelected.of(pursuitMove, pokemonA)
        val switchAction = TurnAction.Switch.of(pokemonB, pokemonA)
        assertEquals(1, PursuitRuleComparator.compare(switchAction, pursuitAction))
    }

    @Test
    fun `(pursuit-rule) non-pursuit move vs switch returns 0`() {
        val moveAction = TurnAction.MoveSelected.of(otherMove, pokemonA)
        val switchAction = TurnAction.Switch.of(pokemonB, pokemonA)
        assertEquals(0, PursuitRuleComparator.compare(moveAction, switchAction))
    }

    @Test
    fun `(pursuit-rule) move vs move returns 0`() {
        val moveAction1 = TurnAction.MoveSelected.of(otherMove, pokemonA)
        val moveAction2 = TurnAction.MoveSelected.of(otherMove, pokemonB)
        assertEquals(0, PursuitRuleComparator.compare(moveAction1, moveAction2))
    }
}
