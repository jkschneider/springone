package org.springframework.cloud.netflix.metrics.spectator;

import com.netflix.servo.Metric;
import com.netflix.servo.monitor.CompositeMonitor;
import com.netflix.servo.monitor.Monitor;
import com.netflix.servo.publish.BaseMetricPoller;
import com.netflix.servo.publish.MetricPoller;
import com.netflix.spectator.api.CompositeRegistry;
import com.netflix.spectator.api.Registry;
import com.netflix.spectator.api.Spectator;
import com.netflix.spectator.servo.ServoRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.actuate.autoconfigure.MetricRepositoryAutoConfiguration;
import org.springframework.boot.actuate.endpoint.MetricReaderPublicMetrics;
import org.springframework.boot.actuate.metrics.CounterService;
import org.springframework.boot.actuate.metrics.GaugeService;
import org.springframework.boot.actuate.metrics.dropwizard.DropwizardMetricServices;
import org.springframework.boot.actuate.metrics.reader.MetricRegistryMetricReader;
import org.springframework.boot.actuate.metrics.writer.CompositeMetricWriter;
import org.springframework.boot.actuate.metrics.writer.MetricWriter;
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

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Conditionally configures both an MVC interceptor and a RestTemplate interceptor
 * that records metrics for request handling timings.
 */
@Configuration
@AutoConfigureBefore(MetricRepositoryAutoConfiguration.class)
public class SpectatorAutoConfiguration extends WebMvcConfigurerAdapter {
    /**
     * We want to hide this type as much as possible because Spectator is intended
     * as a replacement for Servo.  We will expose this registry's Monitor only because
     * Atlas still reads its metrics from Servo Monitors, and not from any Spectator type.
     */
    ServoRegistry servoRegistry = new ServoRegistry();

    @Bean
    Monitor monitor() {
        return servoRegistry;
    }

    @Primary
    @Bean
    CompositeRegistry registry() {
        CompositeRegistry registry = Spectator.globalRegistry();
        registry.add(servoRegistry);
        return registry;
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
    MetricPoller metricPoller(Monitor monitor) {
        return new BaseMetricPoller() {
            @Override
            public List<Metric> pollImpl(boolean reset) {
                if(monitor instanceof CompositeMonitor) {
                    return ((CompositeMonitor<?>) monitor).getMonitors().stream()
                            .filter(m -> m.getValue() != null)
                            .map(m -> new Metric(m.getConfig(), System.currentTimeMillis(), m.getValue()))
                            .collect(Collectors.toList());
                }
                else if(monitor.getValue() != null) {
                    return Collections.singletonList(new Metric(monitor.getConfig(),
                            System.currentTimeMillis(), monitor.getValue()));
                }
                return Collections.emptyList();
            }
        };
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
