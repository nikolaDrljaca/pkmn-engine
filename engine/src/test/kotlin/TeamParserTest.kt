import com.drbrosdev.parser.team.TextTeamParser
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow

class TeamParserTest {

    @Test
    fun `parse pokepaste format`() {
        val paste = """
        Ferrothorn @ Leftovers
        Ability: Iron Barbs
        EVs: 252 HP / 8 Atk / 124 Def / 124 SpD
        IVs: 0 Atk
        Impish Nature
        - Growl
        - Flamethrower
        - Leer
        - Stealth Rock
        
        Tyranitar @ Black Sludge
        Ability: Sand Stream
        EVs: 252 HP / 252 Atk / 4 Def
        Adamant Nature
        - Earthquake
        - Crunch
        - Leer
        - Stealth Rock
    """.trimIndent()
        val parsed = assertDoesNotThrow {
            TextTeamParser.parse(paste)
        }
    }

}