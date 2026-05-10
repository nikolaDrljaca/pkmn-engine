import com.drbrosdev.battle.pokemon.Effectiveness
import com.drbrosdev.battle.pokemon.Element
import com.drbrosdev.battle.pokemon.Elements
import com.drbrosdev.battle.pokemon.effectiveness
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class TypeEffectivenessTest {

    @Test
    fun `test super effectiveness`() {
        val result = effectiveness(
            attacker = Element.GROUND,
            defenders = Elements.of(Element.ROCK, Element.DARK)
        )
        assertEquals(Effectiveness.SUPER, result)
    }
}