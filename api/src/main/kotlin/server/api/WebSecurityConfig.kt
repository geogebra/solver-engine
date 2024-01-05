package server.api

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.web.SecurityFilterChain
import org.springframework.web.cors.CorsConfiguration

@Configuration
@ConfigurationProperties("api.web-security")
data class WebSecurityConfig(var allowedOrigins: List<String> = emptyList()) {
    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http.csrf { it.disable() }
        http
            .authorizeHttpRequests { it.requestMatchers("/**").permitAll() }
            .cors {
                it.configurationSource {
                    CorsConfiguration()
                        .setAllowedOriginPatterns(allowedOrigins)
                        .applyPermitDefaultValues()
                }
            }
        return http.build()
    }
}
