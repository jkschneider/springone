package org.springframework.boot.netflix.spectator

import com.netflix.servo.Metric
import com.netflix.servo.monitor.CompositeMonitor
import com.netflix.servo.monitor.Monitor
import com.netflix.servo.publish.BaseMetricPoller
import com.netflix.servo.publish.BasicMetricFilter
import com.netflix.servo.publish.MetricPoller
import com.netflix.servo.publish.atlas.AtlasMetricObserver
import com.netflix.servo.publish.atlas.ServoAtlasConfig
import com.netflix.servo.tag.BasicTagList
import com.netflix.spectator.servo.ServoRegistry
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled

import javax.inject.Named
import javax.validation.constraints.NotNull

@Configuration
@ConditionalOnClass(AtlasMetricObserver)
@ConditionalOnBean(Monitor)
class AtlasAutoConfiguration {
    @Value('${spring.application.name}')
    String appName

    @Value('${netflix.atlas.tags.team:#{null}}')
    String team

    @Autowired
    Monitor monitor

    @Bean
    AtlasMetricObserver atlasObserver(ServoAtlasConfig config) {
        def defaultTags = [appName: appName]
        if(team) defaultTags[team] = team
        new AtlasMetricObserver(config, BasicTagList.copyOf(defaultTags))
    }

    @Bean
    MetricPoller metricPoller() {
        new BaseMetricPoller() {
            @Override
            List<Metric> pollImpl(boolean reset) {
                if(monitor instanceof CompositeMonitor) {
                    def monitors = ((CompositeMonitor) monitor).monitors
                    monitors.inject(new ArrayList<Metric>(monitors.size())) { ArrayList<Metric> metrics, monitor ->
                        def v = monitor.value
                        if(v) metrics.add(new Metric(monitor.config, System.currentTimeMillis(), v))
                        metrics
                    }
                }
                else {
                    def v = monitor.value
                    v ? [new Metric(monitor.config, System.currentTimeMillis(), v)] : []
                }
            }
        }
    }
}

/**
 * Periodically polls registered Servo monitors
 */
@Configuration
@ConditionalOnBean([ServoRegistry, MetricPoller, AtlasMetricObserver])
@EnableScheduling
class AtlasMetricPollerConfiguration {
    @Autowired
    ServoRegistry registry

    @Autowired
    MetricPoller poller

    @Autowired
    AtlasMetricObserver observer

    @Scheduled(fixedRateString = '${netflix.atlas.pollingInterval:5000}')
    void pollMetrics() {
        observer.update(poller.poll(BasicMetricFilter.MATCH_ALL))
    }
}

@Named
@ConfigurationProperties(prefix='netflix.atlas')
class SpringAtlasConfiguration implements ServoAtlasConfig {
    @NotNull String uri
    Integer pushQueueSize = 100
    boolean enabled = true
    Integer batchSize = 1000

    @Override
    String getAtlasUri() {
        return uri
    }

    @Override
    int getPushQueueSize() {
        return pushQueueSize
    }

    @Override
    boolean shouldSendMetrics() {
        return enabled
    }

    @Override
    int batchSize() {
        return batchSize
    }
}