import com.drbrosdev.battle.Battle
import com.drbrosdev.battle.BattleState
import com.drbrosdev.battle.Team
import com.drbrosdev.battle.move.MoveStatus
import com.drbrosdev.battle.move.buildMove
import com.drbrosdev.battle.pokemon.PokemonId
import com.drbrosdev.battle.pokemon.buildPokemon
import com.drbrosdev.battle.turn.MoveDisabledTurnValidator
import com.drbrosdev.battle.turn.Turn
import com.drbrosdev.battle.turn.TurnAction
import com.drbrosdev.battle.turn.TurnValidity
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class MoveDisabledTurnValidatorTest {

    private val active1 = PokemonId("id")
    private val active2 = PokemonId("id")

    @Test
    fun `disabled move is invalid`() {
        val pokemon1 = buildPokemon {
            id = active1
            addMove(buildMove { status = MoveStatus.DISABLED })
        }
        val pokemon2 = buildPokemon {
            id = active2
            addMove(buildMove {  })
        }
        val turn = Turn(
            selection1 = pokemon1 to TurnAction.MoveSelected(pokemon1.moves.first().id),
            selection2 = pokemon2 to TurnAction.MoveSelected(pokemon2.moves.first().id)
        )
        val battle = Battle(
            Team(mapOf(active1 to pokemon1)),
            Team(mapOf(active2 to pokemon2)),
            active1,
            active2,
        )

        val result = with(MoveDisabledTurnValidator) { turn.validate(battle) }
        assertEquals(
            TurnValidity.Invalid(TurnValidity.InvalidReason.MoveDisabled),
            result
        )
    }

    @Test
    fun `two disabled moves are invalid`() {
        val pokemon1 = buildPokemon {
            id = active1
            addMove(buildMove { status = MoveStatus.DISABLED })
        }
        val pokemon2 = buildPokemon {
            id = active2
            addMove(buildMove { status = MoveStatus.DISABLED })
        }
        val turn = Turn(
            selection1 = pokemon1 to TurnAction.MoveSelected(pokemon1.moves.first().id),
            selection2 = pokemon2 to TurnAction.MoveSelected(pokemon2.moves.first().id)
        )
        val battle = Battle(
            Team(mapOf(active1 to pokemon1)),
            Team(mapOf(active2 to pokemon2)),
            active1,
            active2,
        )
        val result = with(MoveDisabledTurnValidator) { turn.validate(battle) }
        assertEquals(
            TurnValidity.Invalid(TurnValidity.InvalidReason.MoveDisabled),
            result
        )
    }

    @Test
    fun `disabled move and switch is invalid`() {
        val pokemon1 = buildPokemon {
            id = active1
            addMove(buildMove { status = MoveStatus.DISABLED })
        }
        val pokemon2 = buildPokemon {
            id = active2
            addMove(buildMove { status = MoveStatus.DISABLED })
        }
        val turn = Turn(
            selection1 = pokemon1 to TurnAction.MoveSelected(pokemon1.moves.first().id),
            selection2 = pokemon2 to TurnAction.Switch(pokemon2.id)
        )
        val battle = Battle(
            Team(mapOf(active1 to pokemon1)),
            Team(mapOf(active2 to pokemon2)),
            active1,
            active2,
        )
        val result = with(MoveDisabledTurnValidator) { turn.validate(battle) }
        assertEquals(
            TurnValidity.Invalid(TurnValidity.InvalidReason.MoveDisabled),
            result
        )
    }

    @Test
    fun `non-disabled move and switch is valid`() {
        val pokemon1 = buildPokemon {
            id = active1
            addMove(buildMove {  })
        }
        val pokemon2 = buildPokemon {
            id = active2
            addMove(buildMove {  })
        }
        val turn = Turn(
            selection1 = pokemon1 to TurnAction.MoveSelected(pokemon1.moves.first().id),
            selection2 = pokemon2 to TurnAction.Switch(pokemon2.id)
        )
        val battle = Battle(
            Team(mapOf(active1 to pokemon1)),
            Team(mapOf(active2 to pokemon2)),
            active1,
            active2,
        )
        val result = with(MoveDisabledTurnValidator) { turn.validate(battle) }
        assertEquals(
            TurnValidity.Valid,
            result
        )
    }

    @Test
    fun `two switches are valid`() {
        val pokemon1 = buildPokemon {
            id = active1
            addMove(buildMove {  })
        }
        val pokemon2 = buildPokemon {
            id = active2
            addMove(buildMove {  })
        }
        val turn = Turn(
            selection1 = pokemon1 to TurnAction.Switch(pokemon1.id),
            selection2 = pokemon2 to TurnAction.Switch(pokemon2.id)
        )
        val battle = Battle(
            Team(mapOf(active1 to pokemon1)),
            Team(mapOf(active2 to pokemon2)),
            active1,
            active2,
        )
        val result = with(MoveDisabledTurnValidator) { turn.validate(battle) }
        assertEquals(
            TurnValidity.Valid,
            result
        )
    }
}