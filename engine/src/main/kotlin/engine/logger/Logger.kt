package engine.logger

import java.util.function.Supplier
import java.util.logging.Level

interface Logger {
    fun log(level: Level, string: String)

    fun <T> log(level: Level, supplier: Supplier<T>)
}

/**
 * A logger instance that does nothing
 */
object DefaultLogger : Logger {

    override fun log(level: Level, string: String) {
        // Do nothing
    }

    override fun <T>log(level: Level, supplier: Supplier<T>) {
        // Do nothing
    }
}
