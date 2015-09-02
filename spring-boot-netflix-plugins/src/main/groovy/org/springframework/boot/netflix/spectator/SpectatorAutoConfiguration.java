package org.springframework.boot.netflix.spectator;

import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 * Conditionally configures both an MVC interceptor and a RestTemplate interceptor
 * that records metrics for request handling timings.
 */
@Configuration
public class SpectatorAutoConfiguration extends WebMvcConfigurerAdapter {
    @Bean
    @ConditionalOnWebApplication
    SpectatorHandlerInterceptor spectatorMonitoringWebResourceInterceptor() {
        return new SpectatorHandlerInterceptor();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(spectatorMonitoringWebResourceInterceptor());
    }

    @Bean
    @ConditionalOnBean({RestTemplate.class, AopAutoConfiguration.CglibAutoProxyConfiguration.class})
    RestTemplateUrlTemplateCapturingAspect restTemplateUrlTemplateCapturingAspect() {
        return new RestTemplateUrlTemplateCapturingAspect();
    }

    @Bean
    @ConditionalOnBean(RestTemplate.class)
    SpectatorClientHttpRequestInterceptor spectatorLoggingClientHttpRequestInterceptor() {
        return new SpectatorClientHttpRequestInterceptor();
    }

    @Bean
    @ConditionalOnBean(SpectatorClientHttpRequestInterceptor.class)
    BeanPostProcessor spectatorRestTemplateInterceptorPostProcessor(SpectatorClientHttpRequestInterceptor interceptor) {
        return new BeanPostProcessor() {
            @Override
            public Object postProcessBeforeInitialization(Object bean, String beanName) {
                return bean;
            }

            @Override
            public Object postProcessAfterInitialization(Object bean, String beanName) {
                if (bean instanceof RestTemplate)
                    ((RestTemplate) bean).getInterceptors().add(interceptor);
                return bean;
            }
        };
    }
}
