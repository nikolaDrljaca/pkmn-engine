import com.drbrosdev.battle.environment.StealthRockEnvUnit
import com.drbrosdev.battle.pokemon.Element
import com.drbrosdev.battle.pokemon.Elements
import com.drbrosdev.battle.pokemon.Level
import com.drbrosdev.battle.pokemon.Quirky
import com.drbrosdev.battle.pokemon.RunAway
import com.drbrosdev.battle.pokemon.buildPokemon
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.assertNotEquals

@ExtendWith(LoggingExtension::class)
class EnvironmentUnitTest {
    val gliscor = buildPokemon {
        pokemonId("gliscor")
        name = "Gliscor"
        nature = Quirky
        ability = RunAway
        level = Level(50)
        elements(Element.GROUND, Element.FLYING)
        baseStats {
            baseHp = 75
            attack = 95
            defence = 125
            specialAttack = 45
            specialDefence = 75
            speed = 95
        }
        individualValues { allMax() }
        effortValues {
            maxHp()
            maxAttack()
        }
    }

    @Test
    fun `test stealth rock effect damage`() {
        val afterEffect = with(StealthRockEnvUnit.effect) { apply(gliscor) }
        assertNotEquals(gliscor.inBattleHp.value, afterEffect.inBattleHp.value)
    }

    @Test
    fun `test stealth rock effect damage 4x effectiveness`() {
        val mon = gliscor.copy(elements = Elements(setOf(Element.FIRE, Element.FLYING)))
        val afterEffect = with(StealthRockEnvUnit.effect) { apply(mon) }
        assertNotEquals(gliscor.inBattleHp.value, afterEffect.inBattleHp.value)
    }

}