package org.springframework.boot.netflix.spectator

import com.netflix.spectator.api.Registry
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.DispatcherServlet
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter

import javax.servlet.Servlet

@Configuration
@ConditionalOnClass([Servlet, DispatcherServlet])
@ConditionalOnBean(Registry)
@ConditionalOnWebApplication
class SpectatorMetricsAutoConfiguration extends WebMvcConfigurerAdapter {
    @Bean
    SpectatorMetricsHandlerInterceptor spectatorMonitoringWebResourceInterceptor() {
        new SpectatorMetricsHandlerInterceptor()
    }

    @Override
    void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(spectatorMonitoringWebResourceInterceptor())
    }
}