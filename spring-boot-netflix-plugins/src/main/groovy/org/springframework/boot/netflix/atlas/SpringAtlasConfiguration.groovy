package org.springframework.boot.netflix.atlas

import com.netflix.servo.publish.atlas.ServoAtlasConfig
import org.springframework.boot.context.properties.ConfigurationProperties

import javax.inject.Named
import javax.validation.constraints.NotNull

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