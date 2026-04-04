import com.drbrosdev.battle.Battle
import com.drbrosdev.battle.Team
import com.drbrosdev.battle.move.Flamethrower
import com.drbrosdev.battle.move.Growl
import com.drbrosdev.battle.move.Leer
import com.drbrosdev.battle.move.Pursuit
import com.drbrosdev.battle.move.SandAttack
import com.drbrosdev.battle.move.Scratch
import com.drbrosdev.battle.pokemon.*
import com.drbrosdev.battle.turn.Turn
import com.drbrosdev.battle.turn.TurnAction
import com.drbrosdev.battle.turn.resolveTurn
import org.junit.jupiter.api.Test

class BattleTest {
    val chimchar = buildPokemon {
        id = "chimchar-1"
        elements(Element.WATER)
        name = "Chimchar"
        nature = Quirky
        ability = RunAway
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

    val charmander = buildPokemon {
        id = "charmander-2"
        elements(Element.FIRE)
        name = "Charmander"
        nature = Quirky
        ability = Pressure
        baseStats {
            baseHp = 11
            attack = 52
            defence = 43
            specialAttack = 60
            specialDefence = 50
            speed = 65
        }
        individualValues { allMax() }
        effortValues {
            maxHp()
            maxAttack()
        }
        addMoves(Scratch, Growl, Flamethrower, SandAttack)
    }

    val battle = Battle(
        team1 = Team(mapOf(chimchar.id to chimchar)),
        team2 = Team(mapOf(charmander.id to charmander)),
        active1 = chimchar.id,
        active2 = charmander.id
    )

    @Test
    fun `first single turn test`() {
        val turn = Turn(
            selection1 = chimchar to TurnAction.MoveSelected(Scratch),
            selection2 = charmander to TurnAction.MoveSelected(SandAttack)
        )
        val updatedBattle = battle.resolveTurn(turn)
        val foo = updatedBattle.team1[chimchar.id].computeInBattleStats(updatedBattle)
        val bar = updatedBattle.team2[charmander.id].computeInBattleStats(updatedBattle)
        println(updatedBattle)
    }

}