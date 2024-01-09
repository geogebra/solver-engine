/*
 * Copyright (c) 2023 GeoGebra GmbH, office@geogebra.org
 * This file is part of GeoGebra
 *
 * The GeoGebra source code is licensed to you under the terms of the
 * GNU General Public License (version 3 or later)
 * as published by the Free Software Foundation,
 * the current text of which can be found via this link:
 * https://www.gnu.org/licenses/gpl.html ("GPL")
 * Attribution (as required by the GPL) should take the form of (at least)
 * a mention of our name, an appropriate copyright notice
 * and a link to our website located at https://www.geogebra.org
 *
 * For further details, please see https://www.geogebra.org/license
 *
 */

package server.api

import jakarta.servlet.http.HttpServletRequest
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping

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
    @RequestMapping("\${api.base-path:/api/v1.0-alpha0/**}")
    fun redirect(request: HttpServletRequest): String {
        val forwardPath = request.servletPath.replace("/v1.0-alpha0/", "/v1/")
        return "forward:$forwardPath"
    }
}
