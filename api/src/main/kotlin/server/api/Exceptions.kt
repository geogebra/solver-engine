package server.api

import com.fasterxml.jackson.databind.exc.InvalidFormatException
import jakarta.servlet.http.HttpServletResponse
import org.antlr.v4.runtime.misc.ParseCancellationException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import server.models.Format

class InvalidExpressionException(expression: String, val parseException: ParseCancellationException? = null) :
    ApiException("Invalid expression: $expression", HttpStatus.BAD_REQUEST.value())

class InvalidPresetException(preset: String) :
    ApiException("Invalid preset: $preset", HttpStatus.BAD_REQUEST.value())

class InvalidSettingException(setting: String, value: String) :
    ApiException("Invalid setting: $setting = $value", HttpStatus.BAD_REQUEST.value())

class InvalidStrategyException(category: String, strategy: String) :
    ApiException("Invalid strategy $strategy for category $category", HttpStatus.BAD_REQUEST.value())

class PlanNotApplicableException(planId: String) :
    ApiException("Plan not applicable: $planId", HttpStatus.BAD_REQUEST.value())

class ExpressionNotGraphableException(expression: String) :
    ApiException("Expression not graphable: $expression", HttpStatus.BAD_REQUEST.value())

@ControllerAdvice
class FallbackExceptionHandler {
    /**
     * On otherwise unhandled exceptions, fall back to logging the exception stack trace
     * and returning a generic internal server error to the client.
     */
    @ExceptionHandler(value = [Exception::class])
    fun onException(ex: Exception, response: HttpServletResponse) {
        logger.error(ex.stackTraceToString())
        response.sendError(HttpStatus.INTERNAL_SERVER_ERROR.value(), "internal error")
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleInvalidRequestFormat(response: HttpServletResponse, ex: HttpMessageNotReadableException) {
        val rootCause = ex.rootCause
        if (rootCause is InvalidFormatException) {
            if (rootCause.targetType.isAssignableFrom(Format::class.java)) {
                response.sendError(
                    HttpStatus.BAD_REQUEST.value(),
                    "Invalid value \"${rootCause.value}\" specified for Format. " +
                        "Valid inputs are: \"solver\", \"latex\", \"json2\".",
                )
                return
            }
        }

        response.sendError(HttpStatus.BAD_REQUEST.value(), ex.message)
    }

    companion object {
        private val logger = LoggerFactory.getLogger(FallbackExceptionHandler::class.java)
    }
}
