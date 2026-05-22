import com.drbrosdev.battle.Battle
import com.drbrosdev.battle.Team
import com.drbrosdev.battle.move.buildMove
import com.drbrosdev.battle.pokemon.PokemonId
import com.drbrosdev.battle.pokemon.VolatileStatus
import com.drbrosdev.battle.pokemon.buildPokemon
import com.drbrosdev.battle.turn.Turn
import com.drbrosdev.battle.turn.TurnAction
import com.drbrosdev.battle.turn.validation.TauntTurnValidator
import com.drbrosdev.battle.turn.validation.TurnInvalidReason
import com.drbrosdev.battle.turn.validation.TurnValidity
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class TauntTurnValidatorTest {

    private val active1 = PokemonId("id")
    private val active2 = PokemonId("id")

    val battle = Battle(
        Team(mapOf(active1 to buildPokemon { id = active1 })),
        Team(mapOf(active2 to buildPokemon { id = active2 })),
        active1,
        active2,
    )

    @Test
    fun `taunted mon with status move and switch is invalid`() {
        val pokemon1 = buildPokemon {
            id = active1
            addStatus(VolatileStatus.Taunt(3))
            addMove(buildMove { status() })
        }
        val pokemon2 = buildPokemon {
            id = active2
        }
        val turn = Turn(
            selection1 = pokemon1.id to TurnAction.MoveSelected(pokemon1.moves.first().id),
            selection2 = pokemon2.id to TurnAction.Switch(active2)
        )
        val battle = Battle(
            Team(mapOf(active1 to pokemon1)),
            Team(mapOf(active2 to pokemon2)),
            active1,
            active2,
        )
        val result = with(TauntTurnValidator) { turn.validate(battle) }
        assertEquals(
            TurnValidity.Invalid(TurnInvalidReason.PokemonTaunted),
            result
        )
    }

    @Test
    fun `taunted mon with status move and another move is invalid`() {
        val pokemon1 = buildPokemon {
            id = active1
            addStatus(VolatileStatus.Taunt(3))
            addMove(buildMove { status() })
        }
        val pokemon2 = buildPokemon {
            id = active2
            addMove(buildMove {  })
        }
        val turn = Turn(
            selection1 = pokemon1.id to TurnAction.MoveSelected(pokemon1.moves.first().id),
            selection2 = pokemon2.id to TurnAction.MoveSelected(pokemon2.moves.first().id)
        )
        val battle = Battle(
            Team(mapOf(active1 to pokemon1)),
            Team(mapOf(active2 to pokemon2)),
            active1,
            active2,
        )
        val result = with(TauntTurnValidator) { turn.validate(battle) }
        assertEquals(
            TurnValidity.Invalid(TurnInvalidReason.PokemonTaunted),
            result
        )
    }

    @Test
    fun `two taunted mon with status move is invalid`() {
        val pokemon1 = buildPokemon {
            id = active1
            addStatus(VolatileStatus.Taunt(3))
            addMove(buildMove { status() })
        }
        val pokemon2 = buildPokemon {
            id = active2
            addStatus(VolatileStatus.Taunt(3))
            addMove(buildMove { status() })
        }
        val turn = Turn(
            selection1 = pokemon1.id to TurnAction.MoveSelected(pokemon1.moves.first().id),
            selection2 = pokemon2.id to TurnAction.MoveSelected(pokemon2.moves.first().id)
        )
        val battle = Battle(
            Team(mapOf(active1 to pokemon1)),
            Team(mapOf(active2 to pokemon2)),
            active1,
            active2,
        )
        val result = with(TauntTurnValidator) { turn.validate(battle) }
        assertEquals(
            TurnValidity.Invalid(TurnInvalidReason.PokemonTaunted),
            result
        )
    }

    @Test
    fun `switches are valid`() {
        val turn = Turn(
            selection1 = buildPokemon { id = active1 }.id to TurnAction.Switch(buildPokemon {  }.id),
            selection2 = buildPokemon { id = active2 }.id to TurnAction.Switch(buildPokemon {  }.id)
        )
        val result = with(TauntTurnValidator) { turn.validate(battle) }
        assertEquals(
            TurnValidity.Valid,
            result
        )
    }

    @Test
    fun `normal status mons are valid`() {
        val turn = Turn(
            selection1 = active1 to TurnAction.MoveSelected(buildMove { }.id),
            selection2 = active2 to TurnAction.Switch(buildPokemon { }.id)
        )
        val result = with(TauntTurnValidator) { turn.validate(battle) }
        assertEquals(
            TurnValidity.Valid,
            result
        )
    }

    @Test
    fun `taunted mon with non-status move is valid`() {
        val pokemon1 = buildPokemon {
            id = active1
            addStatus(VolatileStatus.Taunt(3))
            addMove(buildMove { physical() })
        }
        val pokemon2 = buildPokemon {
            id = active2
            addMove(buildMove { status() })
        }
        val turn = Turn(
            selection1 = pokemon1.id to TurnAction.MoveSelected(pokemon1.moves.first().id),
            selection2 = pokemon2.id to TurnAction.MoveSelected(pokemon2.moves.first().id)
        )
        val battle = Battle(
            Team(mapOf(active1 to pokemon1)),
            Team(mapOf(active2 to pokemon2)),
            active1,
            active2,
        )
        val result = with(TauntTurnValidator) { turn.validate(battle) }
        assertEquals(
            TurnValidity.Valid,
            result
        )
    }
}