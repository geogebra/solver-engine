package server.api

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import javax.servlet.http.HttpServletRequest

/**
 * This controller is temporary, so that existing clients do not break immediately when the api URL moves from
 * /api/v1-alpha1 to /api/v1
 *
 * We will remove it once things have settled down.
 */
@Controller
class ForwardAlpha {
    /**
     * Forward all requests to /api/v1-alpha1 to /api/v1
     */
    @RequestMapping("\${api.base-path:/api/v1-alpha1/**}")
    fun redirect(request: HttpServletRequest): String {
        val forwardPath = request.servletPath.replace("/v1.0-alpha0/", "/v1/")
        return "forward:$forwardPath"
    }
}
