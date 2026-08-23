import com.drbrosdev.battle.Battle
import com.drbrosdev.battle.BattleState
import com.drbrosdev.battle.Team
import com.drbrosdev.battle.move.registry.MoveIndex.Growl
import com.drbrosdev.battle.move.registry.MoveIndex.Leer
import com.drbrosdev.battle.move.registry.MoveIndex.Scratch
import com.drbrosdev.battle.pokemon.*
import com.drbrosdev.battle.turn.Turn
import com.drbrosdev.battle.turn.TurnAction
import com.drbrosdev.battle.turn.resolveTurn
import org.junit.jupiter.api.Test

class BattleTest {

    val chimchar = buildPokemon {
        pokemonId("chimchar")
        elements(Element.FIRE)
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
        pokemonId("charmander")
        elements(Element.FIRE)
        name = "Charmander"
        nature = Quirky
        ability = Pressure
        baseStats {
            baseHp = 39
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
        addMoves(Scratch, Growl)
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
            listOf(
                TurnAction.MoveSelected.of(Scratch, chimchar),
                TurnAction.MoveSelected.of(Growl, charmander)
            )
        )
        battle.resolveTurn(turn)
    }

    @Test
    fun `run battle test`() {
        val turn = Turn(
            listOf(
                TurnAction.MoveSelected.of(chimchar.moves.first(), chimchar),
                TurnAction.MoveSelected.of(charmander.moves.first(), charmander)
            )
        )
        var updatedBattle = battle.resolveTurn(turn)
        while (updatedBattle.state is BattleState.InProgress) {
            updatedBattle = updatedBattle.resolveTurn(
                Turn(
                    listOf(
                        TurnAction.MoveSelected.of(chimchar.moves.random(), chimchar),
                        TurnAction.MoveSelected.of(charmander.moves.random(), charmander)
                    )
                )
            )
            println(updatedBattle.turnLog.joinToString(separator = "\n") { it })
        }
    }
}
