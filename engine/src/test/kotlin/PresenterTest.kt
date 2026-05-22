import com.drbrosdev.battle.Battle
import com.drbrosdev.battle.Team
import com.drbrosdev.battle.environment.Weather
import com.drbrosdev.battle.item.ItemIndex
import com.drbrosdev.battle.move.registry.MoveIndex.Growl
import com.drbrosdev.battle.move.registry.MoveIndex.Leer
import com.drbrosdev.battle.move.registry.MoveIndex.Scratch
import com.drbrosdev.battle.pokemon.*
import com.drbrosdev.battle.presentation.TextBattlePresenter
import com.drbrosdev.battle.presentation.TextMovePresenter
import com.drbrosdev.battle.presentation.TextPokemonPresenter
import com.drbrosdev.battle.presentation.TextTeamPresenter
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.assertNotNull

@ExtendWith(LoggingExtension::class)
class PresenterTest {
    private val chimchar = buildPokemon {
        pokemonId("chimchar")
        elements(Element.FIRE)
        name = "Chimchar"
        nature = Quirky
        ability = RunAway
        item = ItemIndex.lookup["Leftovers"]!!
        baseStats {
            baseHp = 44
            attack = 58
            defence = 44
            specialAttack = 58
            specialDefence = 44
            speed = 61
        }
        individualValues { allMax() }
        effortValues {
            maxHp()
            maxAttack()
        }
        addMoves(Scratch, Leer)
    }

    private val charmander = buildPokemon {
        pokemonId("charmander")
        elements(Element.FIRE)
        name = "Charmander"
        nature = Quirky
        ability = Pressure
        item = ItemIndex.lookup["Black Sludge"]!!
        baseStats {
            baseHp = 39
            attack = 52
            defence = 43
            specialAttack = 60
            specialDefence = 50
            speed = 65
        }
        individualValues { allMax() }
        majorStatus = MajorStatus.Poisoned
        addStatus(VolatileStatus.confusion(5))
        effortValues {
            maxHp()
            maxAttack()
        }
        inBattleHp = 50
        addMoves(Scratch, Growl)
    }

    private val battle = Battle(
        team1 = Team(mapOf(chimchar.id to chimchar)),
        team2 = Team(mapOf(charmander.id to charmander)),
        active1 = chimchar.id,
        active2 = charmander.id
    )

    private val presenter = TextPokemonPresenter

    @Test
    fun `show output of TEXT pokemon presenter`() {
        val result = presenter.present(charmander.id, battle)
        assertNotNull(result)
        assert(result.isNotBlank())
        println(result)
        println("---")
    }

    @Test
    fun `show output of TEXT CENSORED pokemon presenter`() {
        val result = presenter.present(charmander.id, battle, true)
        assertNotNull(result)
        assert(result.isNotBlank())
        println(result)
        println("---")
    }

    @Test
    fun `show output of TEXT battle presenter`() {
        val result = TextBattlePresenter.present(battle.copy(weather = Weather.hail(5)))
        assertNotNull(result)
        assert(result.isNotBlank())
        println(result)
        println("---")
    }

    @Test
    fun `show output of TEXT team presenter`() {
        val result = TextTeamPresenter.present(battle, battle.team1.id)
        assertNotNull(result)
        assert(result.isNotBlank())
        println(result)
        println("---")
    }

    @Test
    fun `show output of TEXT team censored presenter`() {
        val result = TextTeamPresenter.present(battle, battle.team1.id, true)
        assertNotNull(result)
        assert(result.isNotBlank())
        println(result)
        println("---")
    }

    @Test
    fun `show output of TEXT move presenter`() {
        val result = TextMovePresenter.present(charmander)
        assertNotNull(result)
        assert(result.isNotBlank())
        println(result)
        println("---")
    }
}