package battle.turn

import com.drbrosdev.battle.move.buildMove
import com.drbrosdev.battle.pokemon.buildPokemon
import com.drbrosdev.battle.turn.SwitchRuleComparator
import com.drbrosdev.battle.turn.TurnAction
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertEquals

class SwitchRuleComparatorTest {
    private val pokemonA = buildPokemon { pokemonId("a") }
    private val pokemonB = buildPokemon { pokemonId("b") }
    private val move = buildMove {}

    @Test
    fun `(switch-rule) switch vs switch returns 0, delegates decision`() {
        val switch1 = TurnAction.Switch.of(pokemonB, pokemonA)
        val switch2 = TurnAction.Switch.of(pokemonA, pokemonB)
        assertEquals(0, SwitchRuleComparator.compare(switch1, switch2))
    }

    @Test
    fun `(switch-rule) switch vs move selected returns -1, switching goes first`() {
        val moveAction = TurnAction.MoveSelected.of(move, pokemonA)
        val switchAction = TurnAction.Switch.of(pokemonB, pokemonA)
        assertEquals(-1, SwitchRuleComparator.compare(switchAction, moveAction))
    }

    @Test
    fun `(switch-rule) move selected vs switch returns 1, switching goes first`() {
        val moveAction = TurnAction.MoveSelected.of(move, pokemonA)
        val switchAction = TurnAction.Switch.of(pokemonB, pokemonA)
        assertEquals(1, SwitchRuleComparator.compare(moveAction, switchAction))
    }

    @Test
    fun `(switch-rule) move selected vs move selected returns zero, not applicable`() {
        val moveAction1 = TurnAction.MoveSelected.of(move, pokemonA)
        val moveAction2 = TurnAction.MoveSelected.of(move, pokemonB)
        assertEquals(0, SwitchRuleComparator.compare(moveAction1, moveAction2))
    }
}
