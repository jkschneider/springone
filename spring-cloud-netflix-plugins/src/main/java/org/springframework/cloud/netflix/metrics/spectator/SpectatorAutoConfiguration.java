package org.springframework.cloud.netflix.metrics.spectator;

import com.netflix.servo.publish.MetricPoller;
import com.netflix.servo.publish.MonitorRegistryMetricPoller;
import com.netflix.spectator.api.Registry;
import com.netflix.spectator.servo.ServoRegistry;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.actuate.autoconfigure.MetricRepositoryAutoConfiguration;
import org.springframework.boot.actuate.endpoint.MetricReaderPublicMetrics;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 * Conditionally configures both an MVC interceptor and a RestTemplate interceptor
 * that records metrics for request handling timings.
 */
@Configuration
@AutoConfigureBefore(MetricRepositoryAutoConfiguration.class)
public class SpectatorAutoConfiguration extends WebMvcConfigurerAdapter {
    @Bean
    @ConditionalOnMissingBean(Registry.class)
    Registry registry() {
        return new ServoRegistry();
    }

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

    @Bean
    MetricPoller metricPoller() {
        return new MonitorRegistryMetricPoller();
    }

    // TODO why is @AutoConfigureBefore not preventing the default CounterService from registering like it does for MetricsDropwizardAutoConfiguration?
    @Primary
    @Bean
    @ConditionalOnMissingBean({ SpectatorMetricServices.class })
    public SpectatorMetricServices spectatorMetricServices(Registry metricRegistry) {
        return new SpectatorMetricServices(metricRegistry);
    }

    @Bean
    public MetricReaderPublicMetrics spectatorPublicMetrics(Registry metricRegistry) {
        SpectatorMetricReader reader = new SpectatorMetricReader(metricRegistry);
        return new MetricReaderPublicMetrics(reader);
    }
}
