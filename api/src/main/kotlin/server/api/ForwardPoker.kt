package server.api

import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class ForwardPoker : WebMvcConfigurer {
    override fun addViewControllers(registry: ViewControllerRegistry) {
        registry.addViewController("/poker.html").setViewName("redirect:/poker/index.html")
        registry.addViewController("/poker").setViewName("redirect:/poker/index.html")
    }
}
