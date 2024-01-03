package engine.logger

import java.util.function.Supplier
import java.util.logging.Level

interface Logger {
    fun log(level: Level, depth: Int, string: String)

    fun <T> log(level: Level, depth: Int, supplier: Supplier<T>)
}

/**
 * A logger instance that does nothing
 */
object DefaultLogger : Logger {
    override fun log(level: Level, depth: Int, string: String) {
        // Do nothing
    }

    override fun <T> log(level: Level, depth: Int, supplier: Supplier<T>) {
        // Do nothing
    }
}
