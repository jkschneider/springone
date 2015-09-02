package org.springframework.boot.netflix.atlas

import com.netflix.servo.publish.atlas.ServoAtlasConfig
import org.springframework.boot.context.properties.ConfigurationProperties

import javax.validation.constraints.NotNull

@ConfigurationProperties(prefix='netflix.atlas')
class SpringAtlasConfiguration implements ServoAtlasConfig {
    @NotNull String uri
    Integer pushQueueSize = 1000
    boolean enabled = true
    Integer batchSize = 10000

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