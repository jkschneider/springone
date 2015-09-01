package org.springframework.boot.netflix.atlas

import com.netflix.servo.monitor.Monitor
import com.netflix.servo.publish.BasicMetricFilter
import com.netflix.servo.publish.MetricPoller
import com.netflix.servo.publish.atlas.AtlasMetricObserver
import com.netflix.spectator.servo.ServoRegistry
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled

/**
 * Periodically polls registered Servo monitors
 */
@Configuration
@ConditionalOnClass(AtlasMetricObserver)
@ConditionalOnBean([Monitor])
@EnableScheduling
@Import(AtlasObserverConfiguration)
class AtlasAutoConfiguration {
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
