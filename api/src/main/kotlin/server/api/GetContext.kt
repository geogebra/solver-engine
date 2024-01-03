package server.api

import engine.context.Context
import engine.context.Preset
import engine.context.Setting
import engine.context.SettingValue
import engine.methods.Strategy
import methods.strategyRegistry
import org.apache.logging.log4j.Logger
import org.apache.logging.log4j.message.ObjectMessage
import org.apache.logging.log4j.message.SimpleMessage
import java.util.function.Supplier
import java.util.logging.Level
import kotlin.reflect.KClass

internal fun getContext(apiCtx: server.models.Context?, variables: Set<String>, logger: Logger) =
    apiCtx?.let {
        Context(
            settings = getSettings(apiCtx.presets, apiCtx.settings),
            precision = apiCtx.precision?.toInt(),
            solutionVariables = getSolutionVariables(variables, apiCtx.solutionVariable),
            preferredStrategies = apiCtx.preferredStrategies?.let { getStrategies(it) } ?: emptyMap(),
            logger = ContextLogger(logger),
        )
    } ?: Context(
        solutionVariables = listOfNotNull(variables.firstOrNull()),
        logger = ContextLogger(logger),
    )

private fun getSolutionVariables(expressionVariables: Set<String>, contextVariables: String?): List<String> {
    val apiVariables = contextVariables?.split(",")?.map { it.trim() } ?: emptyList()
    val intersectionVariables = apiVariables.intersect(expressionVariables)
    return when {
        intersectionVariables.isNotEmpty() -> intersectionVariables.toList()
        expressionVariables.size == 1 -> expressionVariables.toList()
        else -> emptyList()
    }
}

private fun getStrategies(contextStrategies: Map<String, String>): Map<KClass<out Strategy>, Strategy> {
    return contextStrategies.entries.associate { (category, strategy) ->
        strategyRegistry.getStrategyChoice(category, strategy)
            ?: throw InvalidStrategyException(category, strategy)
    }
}

private fun getSettings(
    contextPresets: List<String>?,
    contextSettings: Map<String, String>?,
): Map<Setting, SettingValue> {
    val settings = mutableMapOf<Setting, SettingValue>()

    contextPresets?.forEach {
        val preset = Preset.entries.firstOrNull { preset -> preset.name == it }
            ?: throw InvalidPresetException(it)
        settings += preset.settings
    }

    contextSettings?.forEach { (key, value) ->
        val setting = Setting.entries.firstOrNull { it.name == key }
        val settingValue = setting?.kind?.fromName(value)
        if (setting == null || settingValue == null) {
            throw InvalidSettingException(key, value)
        }

        settings[setting] = settingValue
    }

    return settings
}

private class ContextLogger(val logger: Logger) : engine.logger.Logger {
    override fun log(level: Level, depth: Int, string: String) {
        val indent = ". ".repeat(depth)
        logger.log(convertLevel(level), indent + string)
    }

    override fun <T> log(level: Level, depth: Int, supplier: Supplier<T>) {
        logger.log(convertLevel(level)) {
            val msg = supplier.get()
            val indent = ". ".repeat(depth)
            if (msg is String) {
                SimpleMessage(indent + msg)
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
