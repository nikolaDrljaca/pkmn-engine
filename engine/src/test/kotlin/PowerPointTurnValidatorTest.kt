import com.drbrosdev.battle.Battle
import com.drbrosdev.battle.BattleState
import com.drbrosdev.battle.Team
import com.drbrosdev.battle.move.*
import com.drbrosdev.battle.pokemon.*
import com.drbrosdev.battle.pokemon.stats.BaseStats
import com.drbrosdev.battle.pokemon.stats.EffortValues
import com.drbrosdev.battle.pokemon.stats.IndividualValues
import com.drbrosdev.battle.turn.PowerPointsTurnValidator
import com.drbrosdev.battle.turn.Turn
import com.drbrosdev.battle.turn.TurnAction
import com.drbrosdev.battle.turn.TurnValidity
import org.junit.jupiter.api.Test
import java.util.*
import kotlin.test.assertEquals


class PowerPointTurnValidatorTest {
    private fun makeMove(
        pp: Int = 10,
        status: MoveStatus = MoveStatus.NORMAL
    ) = Move(
        id = "test $pp",
        name = "Test Move",
        element = Element.NORMAL,
        power = 100,
        powerPoints = pp,
        accuracy = MoveAccuracy.AlwaysHit,
        type = MoveType.SPECIAL,
        effect = NoEffect,
        status = status
    )

    private fun makePokemon() = Pokemon(
        id = "test-pokemon-${UUID.randomUUID()}",
        name = "test Pokemon",
        elements = Elements.of(Element.NORMAL),
        nature = Quirky,
        ability = Overgrow,
        effortValues = EffortValues(),
        individualValues = IndividualValues(),
        baseStats = BaseStats(),
    )

    private fun makeTurn(a1: TurnAction, a2: TurnAction) = Turn(
        selection1 = makePokemon() to a1,
        selection2 = makePokemon() to a2
    )

    private val battle = Battle(
        Team(mapOf("id-1" to buildPokemon { id = "id-1" })),
        Team(mapOf("id-2" to buildPokemon { id = "id-2"})),
        "id-1",
        "id-2",
        BattleState.InProgress
    )

    // === VALID cases
    @Test
    fun `moves with remaining PP are valid`() {
        val turn = makeTurn(
            TurnAction.MoveSelected(makeMove(10)),
            TurnAction.MoveSelected(makeMove(10)),
        )
        val result = with(PowerPointsTurnValidator) { turn.validate(battle) }
        assertEquals(TurnValidity.Valid, result)
    }

    @Test
    fun `switch actions are always valid`() {
        val turn = makeTurn(
            TurnAction.Switch(makePokemon()),
            TurnAction.Switch(makePokemon())
        )
        val result = with(PowerPointsTurnValidator) { turn.validate(battle) }
        assertEquals(TurnValidity.Valid, result)
    }

    @Test
    fun `switch and move with PP are valid`() {
        val turn = makeTurn(
            TurnAction.Switch(makePokemon()),
            TurnAction.MoveSelected(makeMove(1))
        )
        val result = with(PowerPointsTurnValidator) { turn.validate(battle) }
        assertEquals(TurnValidity.Valid, result)
    }

    // === INVALID cases
    @Test
    fun `both moves with no PP are invalid`() {
        val turn = makeTurn(
            TurnAction.MoveSelected(makeMove(0)),
            TurnAction.MoveSelected(makeMove(0)),
        )
        val result = with(PowerPointsTurnValidator) { turn.validate(battle) }
        assertEquals(
            TurnValidity.Invalid(TurnValidity.InvalidReason.NoPowerPoints),
            result
        )
    }

    @Test
    fun `moves with no PP are invalid`() {
        val turn = makeTurn(
            TurnAction.MoveSelected(makeMove(10)),
            TurnAction.MoveSelected(makeMove(0)),
        )
        val result = with(PowerPointsTurnValidator) { turn.validate(battle) }
        assertEquals(
            TurnValidity.Invalid(TurnValidity.InvalidReason.NoPowerPoints),
            result
        )
    }

    @Test
    fun `switch and move with no PP is invalid`() {
        val turn = makeTurn(
            TurnAction.MoveSelected(makeMove(0)),
            TurnAction.Switch(makePokemon()),
        )
        val result = with(PowerPointsTurnValidator) { turn.validate(battle) }
        assertEquals(
            TurnValidity.Invalid(TurnValidity.InvalidReason.NoPowerPoints),
            result
        )
    }
}