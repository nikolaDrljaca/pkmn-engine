import com.drbrosdev.battle.move.MoveStatus
import com.drbrosdev.battle.move.buildMove
import com.drbrosdev.battle.pokemon.PokemonId
import com.drbrosdev.battle.pokemon.VolatileStatus
import com.drbrosdev.battle.pokemon.buildPokemon
import com.drbrosdev.battle.turn.TurnAction
import org.junit.jupiter.api.Test

class TurnActionTest {

    @Test
    fun `fainted mon cannot use move`() {
        val move = buildMove { status = MoveStatus.DISABLED }
        val pokemon = buildPokemon {
            id = PokemonId("id")
            addMove(move)
            inBattleHp = 0
        }
        val action = TurnAction(move, pokemon)
        assert(action.isFailure)
    }

    @Test
    fun `move with no PP cannot be used`() {
        val move = buildMove { powerPoints = 0 }
        val pokemon = buildPokemon {
            id = PokemonId("id")
            addMove(move)
        }
        val action = TurnAction(move, pokemon)
        assert(action.isFailure)
    }

    @Test
    fun `disabled move cannot be used`() {
        val move = buildMove { status = MoveStatus.DISABLED }
        val pokemon = buildPokemon {
            id = PokemonId("id")
            addMove(move)
        }
        val action = TurnAction(move, pokemon)
        assert(action.isFailure)
    }

    @Test
    fun `taunted pokemon cannot use status move`() {
        val move = buildMove {
            status()
        }
        val pokemon = buildPokemon {
            id = PokemonId("id")
            addMove(move)
            addStatus(VolatileStatus.Taunt(4))
        }
        val action = TurnAction(move, pokemon)
        assert(action.isFailure)
    }

    @Test
    fun `cannot switch with the same pokemon`() {
        val incoming = buildPokemon {
            id = PokemonId.of("id-1")
        }
        val active = buildPokemon {
            id = PokemonId.of("id-1")
        }
        val action = TurnAction(incoming, active)
        assert(action.isFailure)
    }

}