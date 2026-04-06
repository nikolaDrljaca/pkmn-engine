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
    private val active1 = PokemonId("id")
    private val active2 = PokemonId("id")

    // === VALID cases
    @Test
    fun `moves with remaining PP are valid`() {
        val pokemon1 = buildPokemon {
            id = active1
            addMove(buildMove { powerPoints = 10 })
        }
        val pokemon2 = buildPokemon {
            id = active2
            addMove(buildMove { powerPoints = 10 })
        }
        val turn = Turn(
            pokemon1 to TurnAction.MoveSelected(pokemon1.moves.first().id),
            pokemon2 to TurnAction.MoveSelected(pokemon2.moves.first().id),
        )
        val battle = Battle(
            Team(mapOf(active1 to pokemon1)),
            Team(mapOf(active2 to pokemon2)),
            active1,
            active2,
        )
        val result = with(PowerPointsTurnValidator) { turn.validate(battle) }
        assertEquals(TurnValidity.Valid, result)
    }

    @Test
    fun `switch actions are always valid`() {
        val pokemon1 = buildPokemon {
            id = active1
            addMove(buildMove { powerPoints = 10 })
        }
        val pokemon2 = buildPokemon {
            id = active2
            addMove(buildMove { powerPoints = 10 })
        }
        val turn = Turn(
            pokemon1 to TurnAction.Switch(active1),
            pokemon2 to TurnAction.Switch(active2)
        )
        val battle = Battle(
            Team(mapOf(active1 to pokemon1)),
            Team(mapOf(active2 to pokemon2)),
            active1,
            active2,
        )
        val result = with(PowerPointsTurnValidator) { turn.validate(battle) }
        assertEquals(TurnValidity.Valid, result)
    }

    @Test
    fun `switch and move with PP are valid`() {
        val pokemon1 = buildPokemon {
            id = active1
            addMove(buildMove { powerPoints = 10 })
        }
        val pokemon2 = buildPokemon {
            id = active2
            addMove(buildMove { powerPoints = 10 })
        }
        val turn = Turn(
            pokemon1 to TurnAction.MoveSelected(pokemon1.moves.first().id),
            pokemon2 to TurnAction.Switch(pokemon2.id)
        )
        val battle = Battle(
            Team(mapOf(active1 to pokemon1)),
            Team(mapOf(active2 to pokemon2)),
            active1,
            active2,
        )
        val result = with(PowerPointsTurnValidator) { turn.validate(battle) }
        assertEquals(TurnValidity.Valid, result)
    }

    // === INVALID cases
    @Test
    fun `both moves with no PP are invalid`() {
        val pokemon1 = buildPokemon {
            id = active1
            addMove(buildMove { powerPoints = 0 })
        }
        val pokemon2 = buildPokemon {
            id = active2
            addMove(buildMove { powerPoints = 0 })
        }
        val turn = Turn(
            pokemon1 to TurnAction.MoveSelected(pokemon1.moves.first().id),
            pokemon2 to TurnAction.MoveSelected(pokemon2.moves.first().id)
        )
        val battle = Battle(
            Team(mapOf(active1 to pokemon1)),
            Team(mapOf(active2 to pokemon2)),
            active1,
            active2,
        )
        val result = with(PowerPointsTurnValidator) { turn.validate(battle) }
        assertEquals(
            TurnValidity.Invalid(TurnValidity.InvalidReason.NoPowerPoints),
            result
        )
    }

    @Test
    fun `moves with no PP are invalid`() {
        val pokemon1 = buildPokemon {
            id = active1
            addMove(buildMove { powerPoints = 10 })
        }
        val pokemon2 = buildPokemon {
            id = active2
            addMove(buildMove { powerPoints = 0 })
        }
        val turn = Turn(
            pokemon1 to TurnAction.MoveSelected(pokemon1.moves.first().id),
            pokemon2 to TurnAction.MoveSelected(pokemon2.moves.first().id)
        )
        val battle = Battle(
            Team(mapOf(active1 to pokemon1)),
            Team(mapOf(active2 to pokemon2)),
            active1,
            active2,
        )
        val result = with(PowerPointsTurnValidator) { turn.validate(battle) }
        assertEquals(
            TurnValidity.Invalid(TurnValidity.InvalidReason.NoPowerPoints),
            result
        )
    }

    @Test
    fun `switch and move with no PP is invalid`() {
        val pokemon1 = buildPokemon {
            id = active1
            addMove(buildMove { powerPoints = 10 })
        }
        val pokemon2 = buildPokemon {
            id = active2
            addMove(buildMove { powerPoints = 0 })
        }
        val turn = Turn(
            pokemon1 to TurnAction.Switch(pokemon1.id),
            pokemon2 to TurnAction.MoveSelected(pokemon2.moves.first().id)
        )
        val battle = Battle(
            Team(mapOf(active1 to pokemon1)),
            Team(mapOf(active2 to pokemon2)),
            active1,
            active2,
        )
        val result = with(PowerPointsTurnValidator) { turn.validate(battle) }
        assertEquals(
            TurnValidity.Invalid(TurnValidity.InvalidReason.NoPowerPoints),
            result
        )
    }
}