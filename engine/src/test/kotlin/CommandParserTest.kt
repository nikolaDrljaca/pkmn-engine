import com.drbrosdev.parser.command.Command
import com.drbrosdev.parser.command.TextCommandParser
import com.drbrosdev.parser.command.TurnSelection
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class CommandParserTest {

    @Test
    fun `test parse create battle command`() {
        val command = "create team-1 team-2"
        val parsed = TextCommandParser.parse(command)
        assert(parsed !is Command.Unknown) {
            "Command parsed as UNKNOWN"
        }
        assert(parsed is Command.CreateBattle)
        assertEquals((parsed as Command.CreateBattle).team1.id, "team-1")
        assertEquals(parsed.team2.id, "team-2")
    }

    @Test
    fun `test parse resolve turn command`() {
        val command = "turn battle-1 team-1 move flamethrower ; team-2 switch mudkip"
        val parsed = TextCommandParser.parse(command)
        assert(parsed !is Command.Unknown) {
            "Command parsed as UNKNOWN"
        }
        assert(parsed is Command.ResolveTurn)
        val resolveTurn = parsed as Command.ResolveTurn

        assertEquals(resolveTurn.battleId, "battle-1")

        assertEquals(resolveTurn.action1.first.id, "team-1")
        assert(resolveTurn.action1.second is TurnSelection.MoveSelected)
        assertEquals(
            (resolveTurn.action1.second as TurnSelection.MoveSelected).move,
            "flamethrower"
        )

        assertEquals(resolveTurn.action2.first.id, "team-2")
        assert(resolveTurn.action2.second is TurnSelection.Switch)
        assertEquals(
            (resolveTurn.action2.second as TurnSelection.Switch).incoming,
            "mudkip"
        )
    }

    @Test
    fun `test parse show team command`() {
        val command = "battle-1 team-1 show team"
        val parsed = TextCommandParser.parse(command)
        assert(parsed !is Command.Unknown) {
            "Command parsed as UNKNOWN"
        }
        assert(parsed is Command.ShowTeam)
        assertEquals((parsed as Command.ShowTeam).session.battleId, "battle-1")
        assertEquals(parsed.session.teamId, "team-1")
    }

    @Test
    fun `test parse show team opponent command`() {
        val command = "battle-1 team-1 show team op"
        val parsed = TextCommandParser.parse(command)
        assert(parsed !is Command.Unknown) {
            "Command parsed as UNKNOWN"
        }
        assert(parsed is Command.ShowOpponentTeam)
        assertEquals((parsed as Command.ShowOpponentTeam).session.battleId, "battle-1")
        assertEquals(parsed.session.teamId, "team-1")
    }

    @Test
    fun `test parse show battle command`() {
        val command = "battle-1 team-1 show battle"
        val parsed = TextCommandParser.parse(command)
        assert(parsed !is Command.Unknown) {
            "Command parsed as UNKNOWN"
        }
        assert(parsed is Command.ShowBattle)
        assertEquals((parsed as Command.ShowBattle).session.battleId, "battle-1")
        assertEquals(parsed.session.teamId, "team-1")
    }

    @Test
    fun `test parse show active command`() {
        val command = "battle-1 team-1 show active"
        val parsed = TextCommandParser.parse(command)
        assert(parsed !is Command.Unknown) {
            "Command parsed as UNKNOWN"
        }
        assert(parsed is Command.ShowActive)
        assertEquals((parsed as Command.ShowActive).session.battleId, "battle-1")
        assertEquals(parsed.session.teamId, "team-1")
    }

    @Test
    fun `test parse show active opponent command`() {
        val command = "battle-1 team-1 show active op"
        val parsed = TextCommandParser.parse(command)
        assert(parsed !is Command.Unknown) {
            "Command parsed as UNKNOWN"
        }
        assert(parsed is Command.ShowOpponentActive)
        assertEquals((parsed as Command.ShowOpponentActive).session.battleId, "battle-1")
        assertEquals(parsed.session.teamId, "team-1")
    }

    @Test
    fun `test parse show move command`() {
        val command = "battle-1 team-1 show move mudkip"
        val parsed = TextCommandParser.parse(command)
        assert(parsed !is Command.Unknown) {
            "Command parsed as UNKNOWN"
        }
        assert(parsed is Command.ShowMove)
        assertEquals("battle-1", (parsed as Command.ShowMove).session.battleId)
        assertEquals("team-1", parsed.session.teamId)
        assertEquals("mudkip", parsed.pokemonId)
    }
}