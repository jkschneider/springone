package org.springframework.cloud.netflix.metrics.atlas;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.metrics.export.Exporter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.netflix.metrics.spectator.SpectatorAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.netflix.appinfo.InstanceInfo;
import com.netflix.frigga.Names;
import com.netflix.servo.publish.MetricPoller;
import com.netflix.servo.publish.atlas.AtlasMetricObserver;
import com.netflix.servo.publish.atlas.ServoAtlasConfig;
import com.netflix.servo.tag.BasicTagList;

@Configuration
@ConditionalOnClass(AtlasMetricObserver.class)
@Import(SpectatorAutoConfiguration.class)
public class AtlasAutoConfiguration implements ServoAtlasConfig {
    @Autowired(required = false)
    private Collection<AtlasTagProvider> tagProviders;

    @NotNull
    @Value("${netflix.atlas.uri}")
    private String uri;

    @Value("${netflix.atlas.pushQueueSize:1000}")
    private Integer pushQueueSize;

    @Value("${netflix.atlas.enabled:true}")
    private boolean enabled;

    @Value("${netflix.atlas.batchSize:10000}")
    private Integer batchSize;

    @Bean
    @ConditionalOnMissingBean(AtlasMetricObserver.class)
    public AtlasMetricObserver atlasObserver() {
        BasicTagList tags = (BasicTagList) BasicTagList.EMPTY;
        if(tagProviders != null) {
            for (AtlasTagProvider tagProvider : tagProviders) {
                for (Map.Entry<String, String> tag : tagProvider.defaultTags().entrySet()) {
                    if(tag.getValue() != null)
                        tags = tags.copy(tag.getKey(), tag.getValue());
                }
            }
        }
        return new AtlasMetricObserver(this, tags);
    }

    @Bean
    @ConditionalOnMissingBean(Exporter.class)
    public AtlasExporter exporter(AtlasMetricObserver observer, MetricPoller poller) {
        return new AtlasExporter(observer, poller);
    }

    @Configuration
    @ConditionalOnBean(InstanceInfo.class)
    static class InstanceInfoTagProviderConfiguration {
        @Autowired
        InstanceInfo instanceInfo;

        @Bean
        public AtlasTagProvider instanceInfoTags() {
            return () -> {
                Map<String, String> tags = new HashMap<>();
                tags.put("appName", instanceInfo.getAppName());
                tags.put("cluster", Names.parseName(instanceInfo.getASGName()).getCluster());
                return tags;
            };
        }
    }

    @Override
    public String getAtlasUri() {
        return uri;
    }

    @Override
    public int getPushQueueSize() {
        return pushQueueSize;
    }

    @Override
    public boolean shouldSendMetrics() {
        return enabled;
    }

    @Override
    public int batchSize() {
        return batchSize;
    }
}
