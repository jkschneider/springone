package org.springframework.cloud.netflix.atlas;

import com.netflix.servo.Metric;
import com.netflix.servo.monitor.CompositeMonitor;
import com.netflix.servo.monitor.Monitor;
import com.netflix.servo.publish.BaseMetricPoller;
import com.netflix.servo.publish.MetricPoller;
import com.netflix.servo.publish.atlas.AtlasMetricObserver;
import com.netflix.servo.publish.atlas.ServoAtlasConfig;
import com.netflix.servo.tag.BasicTagList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.util.*;
import java.util.stream.Collectors;

@Configuration
@Import(SpringAtlasConfiguration.class)
public class AtlasObserverConfiguration {
    @Value("${spring.application.name}")
    String appName;

    @Value("${netflix.atlas.tags.team:#{null}}")
    String team;

    @Autowired
    Monitor<?> monitor;

    @Bean
    AtlasMetricObserver atlasObserver(ServoAtlasConfig config) {
        Map<String, String> defaultTags = new HashMap<>();
        defaultTags.put("appName", appName);
        if(team != null)
            defaultTags.put("team", team);
        return new AtlasMetricObserver(config, BasicTagList.copyOf(defaultTags));
    }

    @Bean
    MetricPoller metricPoller() {
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
}