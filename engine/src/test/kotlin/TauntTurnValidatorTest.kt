import com.drbrosdev.battle.Battle
import com.drbrosdev.battle.BattleState
import com.drbrosdev.battle.Team
import com.drbrosdev.battle.move.buildMove
import com.drbrosdev.battle.pokemon.PokemonId
import com.drbrosdev.battle.pokemon.VolatileStatus
import com.drbrosdev.battle.pokemon.buildPokemon
import com.drbrosdev.battle.turn.TauntTurnValidator
import com.drbrosdev.battle.turn.Turn
import com.drbrosdev.battle.turn.TurnAction
import com.drbrosdev.battle.turn.TurnValidity
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class TauntTurnValidatorTest {

    private fun makeTurn(a1: TurnAction, a2: TurnAction) = Turn(
        selection1 = buildPokemon { } to a1,
        selection2 = buildPokemon { } to a2
    )

    private val active1 = PokemonId("id")
    private val active2 = PokemonId("id")

    private val battle = Battle(
        Team(mapOf(active1 to buildPokemon { id = active1 })),
        Team(mapOf(active2 to buildPokemon { id = active2 })),
        active1,
        active2,
        BattleState.InProgress
    )

    @Test
    fun `taunted mon with status move and switch is invalid`() {
        val turn = Turn(
            selection1 = buildPokemon {
                volatileStatus = setOf(VolatileStatus.Taunt(3))
            } to TurnAction.MoveSelected(buildMove {
                status()
            }),
            selection2 = buildPokemon { } to TurnAction.Switch(buildPokemon { })
        )
        val result = with(TauntTurnValidator) { turn.validate(battle) }
        assertEquals(
            TurnValidity.Invalid(TurnValidity.InvalidReason.PokemonTaunted),
            result
        )
    }

    @Test
    fun `taunted mon with status move and another move is invalid`() {
        val turn = Turn(
            selection1 = buildPokemon {
                volatileStatus = setOf(VolatileStatus.Taunt(3))
            } to TurnAction.MoveSelected(buildMove {
                status()
            }),
            selection2 = buildPokemon { } to TurnAction.MoveSelected(buildMove { })
        )
        val result = with(TauntTurnValidator) { turn.validate(battle) }
        assertEquals(
            TurnValidity.Invalid(TurnValidity.InvalidReason.PokemonTaunted),
            result
        )
    }

    @Test
    fun `two taunted mon with status move is invalid`() {
        val turn = Turn(
            selection1 = buildPokemon {
                volatileStatus = setOf(VolatileStatus.Taunt(3))
            } to TurnAction.MoveSelected(buildMove {
                status()
            }),
            selection2 = buildPokemon {
                volatileStatus = setOf(VolatileStatus.Taunt(3))
            } to TurnAction.MoveSelected(buildMove {
                status()
            }),
        )
        val result = with(TauntTurnValidator) { turn.validate(battle) }
        assertEquals(
            TurnValidity.Invalid(TurnValidity.InvalidReason.PokemonTaunted),
            result
        )
    }

    @Test
    fun `switches are valid`() {
        val turn = Turn(
            selection1 = buildPokemon { } to TurnAction.Switch(buildPokemon { }),
            selection2 = buildPokemon { } to TurnAction.Switch(buildPokemon { })
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
            selection1 = buildPokemon { } to TurnAction.MoveSelected(buildMove { }),
            selection2 = buildPokemon { } to TurnAction.Switch(buildPokemon { })
        )
        val result = with(TauntTurnValidator) { turn.validate(battle) }
        assertEquals(
            TurnValidity.Valid,
            result
        )
    }

    @Test
    fun `taunted mon with non-status move is valid`() {
        val turn = Turn(
            selection1 = buildPokemon {
                volatileStatus = setOf(VolatileStatus.Taunt(2))
            } to TurnAction.MoveSelected(buildMove { physical() }),
            selection2 = buildPokemon { } to TurnAction.Switch(buildPokemon { })
        )
        val result = with(TauntTurnValidator) { turn.validate(battle) }
        assertEquals(
            TurnValidity.Valid,
            result
        )
    }
}