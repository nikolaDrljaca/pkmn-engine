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
    fun `disabled move is invalid`() {
        val turn = makeTurn(
            TurnAction.MoveSelected(buildMove { status = MoveStatus.DISABLED }.id),
            TurnAction.MoveSelected(buildMove { }.id)
        )
        val result = with(MoveDisabledTurnValidator) { turn.validate(battle) }
        assertEquals(
            TurnValidity.Invalid(TurnValidity.InvalidReason.MoveDisabled),
            result
        )
    }

    @Test
    fun `two disabled moves are invalid`() {
        val turn = makeTurn(
            TurnAction.MoveSelected(buildMove { status = MoveStatus.DISABLED }.id),
            TurnAction.MoveSelected(buildMove { status = MoveStatus.DISABLED }.id)
        )
        val result = with(MoveDisabledTurnValidator) { turn.validate(battle) }
        assertEquals(
            TurnValidity.Invalid(TurnValidity.InvalidReason.MoveDisabled),
            result
        )
    }

    @Test
    fun `disabled move and switch is invalid`() {
        val turn = makeTurn(
            TurnAction.MoveSelected(buildMove { status = MoveStatus.DISABLED }.id),
            TurnAction.Switch(buildPokemon { }.id)
        )
        val result = with(MoveDisabledTurnValidator) { turn.validate(battle) }
        assertEquals(
            TurnValidity.Invalid(TurnValidity.InvalidReason.MoveDisabled),
            result
        )
    }

    @Test
    fun `non-disabled move and switch is valid`() {
        val turn = makeTurn(
            TurnAction.MoveSelected(buildMove { status = MoveStatus.NORMAL }.id),
            TurnAction.Switch(buildPokemon { }.id)
        )
        val result = with(MoveDisabledTurnValidator) { turn.validate(battle) }
        assertEquals(
            TurnValidity.Valid,
            result
        )
    }

    @Test
    fun `two switches are valid`() {
        val turn = makeTurn(
            TurnAction.Switch(buildPokemon { }.id),
            TurnAction.Switch(buildPokemon { }.id)
        )
        val result = with(MoveDisabledTurnValidator) { turn.validate(battle) }
        assertEquals(
            TurnValidity.Valid,
            result
        )
    }
}