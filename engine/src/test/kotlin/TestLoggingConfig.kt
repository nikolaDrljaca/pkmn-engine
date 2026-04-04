import java.util.logging.Level
import java.util.logging.Logger
import java.util.logging.SimpleFormatter

object TestLoggingConfig {
    init {
        val format = $$"[%1$tH:%1$tM:%1$tS] [%4$s] %3$s - %5$s%n"
        System.setProperty("java.util.logging.SimpleFormatter.format", format)

        val root = Logger.getLogger("")
        root.level = Level.ALL
        root.handlers.forEach { handler ->
            handler.level = Level.ALL
            handler.formatter = SimpleFormatter()
        }
    }
}