package org.springframework.boot.netflix.atlas

import com.netflix.servo.Metric
import com.netflix.servo.monitor.CompositeMonitor
import com.netflix.servo.monitor.Monitor
import com.netflix.servo.publish.BaseMetricPoller
import com.netflix.servo.publish.MetricPoller
import com.netflix.servo.publish.atlas.AtlasMetricObserver
import com.netflix.servo.publish.atlas.ServoAtlasConfig
import com.netflix.servo.tag.BasicTagList
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import

@Configuration
@Import(SpringAtlasConfiguration)
class AtlasObserverConfiguration {
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
                        if(v != null) metrics.add(new Metric(monitor.config, System.currentTimeMillis(), v))
                        metrics
                    }
                }
                else {
                    def v = monitor.value
                    v != null ? [new Metric(monitor.config, System.currentTimeMillis(), v)] : []
                }
            }
        }
    }
}