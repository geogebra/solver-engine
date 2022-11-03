package engine.logger

import java.util.Date
import java.util.logging.ConsoleHandler
import java.util.logging.Handler
import java.util.logging.Level
import java.util.logging.LogRecord
import java.util.logging.Logger
import java.util.logging.SimpleFormatter

private object SolverLogFormatter : SimpleFormatter() {
    private const val white = "\u001B[37m"
    private const val red = "\u001B[31m"
    private const val yellow = "\u001B[33m"
    private const val blue = "\u001B[34m"

    private const val logRecordTemplate = "%1\$s[%2\$tF %2\$tT] [%3\$s] %4\$s %n"

    @Synchronized
    override fun format(lr: LogRecord): String {
        return logRecordTemplate.format(
            getLevelColor(lr.level),
            Date(lr.millis),
            lr.loggerName,
            lr.message,
        )
    }

    fun getLevelColor(level: Level) = when (level) {
        Level.SEVERE -> red
        Level.WARNING -> yellow
        Level.INFO, Level.CONFIG -> blue
        else -> white
    }
}

private val systemLogLevel = run {
    try {
        Level.parse(System.getenv("LOG_LEVEL") ?: "INFO")
    } catch (_: IllegalArgumentException) {
        Level.INFO
    }
}

private val LogHandler: Handler = run {
    val handler = ConsoleHandler()

    handler.level = systemLogLevel
    handler.formatter = SolverLogFormatter

    handler
}

fun createNewLogger(obj: Any): Logger {
    val logger = Logger.getLogger("solver.%1$08X".format(System.identityHashCode(obj)))
    logger.level = Level.ALL
    logger.addHandler(LogHandler)

    return logger
}
