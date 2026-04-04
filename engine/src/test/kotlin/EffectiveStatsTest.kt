import com.drbrosdev.battle.Battle
import com.drbrosdev.battle.Team
import com.drbrosdev.battle.pokemon.*
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class EffectiveStatsTest {

    val gliscor = buildPokemon {
        pokemonId("gliscor")
        name = "Gliscor"
        nature = Quirky
        ability = RunAway
        level = Level(50)
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
    fun `verify effective stat computation no nature`() {
        val effectiveStats = gliscor.effectiveStats
        assertEquals(182, effectiveStats.hp.value, "Effective HP incorrect")
        assertEquals(147, effectiveStats.attack.value, "Effective Attack incorrect")
        assertEquals(145, effectiveStats.defence.value, "Effective Defence incorrect")
        assertEquals(65, effectiveStats.specialAttack.value, "Effective SpecialAttack incorrect")
        assertEquals(95, effectiveStats.specialDefence.value, "Effective SpecialDefence incorrect")
        assertEquals(115, effectiveStats.speed.value, "Effective Speed incorrect")
    }

    @Test
    fun `verify effective stat computation with nature`() {
        val appliedNature = gliscor.copy(nature = Naughty)
        val battle = Battle(
            team1 = Team(mapOf(gliscor.id to gliscor)),
            team2 = Team(mapOf(gliscor.id to gliscor)),
            active1 = gliscor.id,
            active2 = gliscor.id
        )
        val effectiveStats = appliedNature.computeInBattleStats(battle)
        assertEquals(182, effectiveStats.hp.value, "Effective HP w/ Nature incorrect")
        assertEquals(161, effectiveStats.attack.value, "Effective Attack w/ Nature incorrect")
        assertEquals(145, effectiveStats.defence.value, "Effective Defence w/ Nature incorrect")
        assertEquals(65, effectiveStats.specialAttack.value, "Effective SpecialAttack w/ Nature incorrect")
        assertEquals(85, effectiveStats.specialDefence.value, "Effective SpecialDefence w/ Nature incorrect")
        assertEquals(115, effectiveStats.speed.value, "Effective Speed w/ Nature incorrect")
    }

}