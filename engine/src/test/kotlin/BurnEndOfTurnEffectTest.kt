import com.drbrosdev.battle.pokemon.MajorStatus
import com.drbrosdev.battle.pokemon.buildPokemon
import com.drbrosdev.battle.turn.BurnEndOfTurnEffect
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class BurnEndOfTurnEffectTest {

    @Test
    fun `no effect on non-burned pokemon`() {
        val pokemon = buildPokemon {
            baseStats { baseHp = 100 }
        }
        val result = with(BurnEndOfTurnEffect) { apply(pokemon) }
        assertEquals(pokemon.inBattleHp, result.inBattleHp)
    }

    @Test
    fun `burned pokemon takes damage`() {
        val pokemon = buildPokemon {
            baseStats { baseHp = 160 }
            majorStatus = MajorStatus.Burned
        }
        val result = with(BurnEndOfTurnEffect) { apply(pokemon) }
        assertEquals(
            207,
            result.inBattleHp.value
        )
    }

}