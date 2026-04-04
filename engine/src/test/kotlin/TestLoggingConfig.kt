import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.ExtensionContext
import java.util.logging.Level
import java.util.logging.Logger
import java.util.logging.SimpleFormatter

class LoggingExtension : BeforeAllCallback {
    override fun beforeAll(p0: ExtensionContext?) {
        val format = $$"[%1$tH:%1$tM:%1$tS] [%4$s] %3$s - %5$s%n"
        System.setProperty("java.util.logging.SimpleFormatter.format", format)

        val root = Logger.getLogger("")
        root.level = Level.FINE
        root.handlers.forEach { handler ->
            handler.level = Level.FINE
            handler.formatter = SimpleFormatter()
        }
    }
}