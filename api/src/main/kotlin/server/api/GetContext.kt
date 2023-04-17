package server.api

import engine.context.Context
import engine.context.Curriculum
import engine.context.emptyContext
import org.apache.logging.log4j.Logger
import org.apache.logging.log4j.message.FormattedMessage
import org.apache.logging.log4j.message.ObjectMessage
import java.util.function.Supplier
import java.util.logging.Level

internal fun getContext(
    apiCtx: server.models.Context?,
    defaultSolutionVariable: String?,
    logger: Logger,
) = apiCtx?.let {
    val curriculum = when (apiCtx.curriculum) {
        null, "" -> null
        else -> try {
            Curriculum.valueOf(apiCtx.curriculum)
        } catch (_: IllegalArgumentException) {
            throw InvalidCurriculumException(apiCtx.curriculum)
        }
    }

    Context(
        curriculum = curriculum,
        gmFriendly = apiCtx.gmFriendly == true,
        precision = apiCtx.precision?.toInt(),
        preferDecimals = apiCtx.preferDecimals,
        solutionVariables = listOfNotNull(apiCtx.solutionVariable ?: defaultSolutionVariable),
        logger = ContextLogger(logger),
    )
} ?: emptyContext.copy(
    solutionVariables = listOfNotNull(defaultSolutionVariable),
    logger = ContextLogger(logger),
)

private class ContextLogger(val logger: Logger) : engine.logger.Logger {
    override fun log(level: Level, string: String) {
        logger.log(convertLevel(level), string)
    }

    override fun <T>log(level: Level, supplier: Supplier<T>) {
        logger.log(convertLevel(level)) {
            val msg = supplier.get()
            if (msg is String) {
                FormattedMessage(msg)
            } else {
                ObjectMessage(msg)
            }
        }
    }

    private fun convertLevel(level: Level): org.apache.logging.log4j.Level {
        return when (level) {
            Level.OFF -> org.apache.logging.log4j.Level.OFF
            Level.SEVERE -> org.apache.logging.log4j.Level.FATAL
            Level.WARNING -> org.apache.logging.log4j.Level.ERROR
            Level.INFO -> org.apache.logging.log4j.Level.INFO
            Level.CONFIG -> org.apache.logging.log4j.Level.INFO
            Level.FINE -> org.apache.logging.log4j.Level.DEBUG
            Level.FINER, Level.FINEST -> org.apache.logging.log4j.Level.TRACE
            Level.ALL -> org.apache.logging.log4j.Level.ALL
            else -> org.apache.logging.log4j.Level.forName(level.name, level.intValue())
        }
    }
}
