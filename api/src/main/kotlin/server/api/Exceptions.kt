package server.api

import org.antlr.v4.runtime.misc.ParseCancellationException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import javax.servlet.http.HttpServletResponse

class InvalidExpressionException(expression: String, val parseException: ParseCancellationException? = null) :
    ApiException("invalid expression: $expression", HttpStatus.BAD_REQUEST.value())

class PlanNotApplicableException(planId: String) :
    ApiException("plan not applicable: $planId", HttpStatus.BAD_REQUEST.value())

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

    companion object {
        private val logger = LoggerFactory.getLogger(FallbackExceptionHandler::class.java)
    }
}
