package server.api

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.web.SecurityFilterChain
import org.springframework.web.cors.CorsConfiguration

@Configuration
class WebSecurityConfig {

    companion object {
        val allowedOrigins = listOf(
            "http://localhost:[*]",
            "http://*.geogebra.net",
            "https://*.geogebra.net",
            "http://*.geogebra.org",
            "https://*.geogebra.net"
        )
    }

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http.csrf().disable()
        http
            .authorizeHttpRequests { it.antMatchers("/**").permitAll() }
            .cors().configurationSource {
                CorsConfiguration()
                    .setAllowedOriginPatterns(allowedOrigins)
                    .applyPermitDefaultValues()
            }
        return http.build()
    }
}
