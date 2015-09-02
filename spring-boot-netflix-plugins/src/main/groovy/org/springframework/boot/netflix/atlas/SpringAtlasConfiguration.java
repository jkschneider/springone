package org.springframework.boot.netflix.atlas;

import com.netflix.servo.publish.atlas.ServoAtlasConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.validation.constraints.NotNull;

@ConfigurationProperties(prefix="netflix.atlas")
public class SpringAtlasConfiguration implements ServoAtlasConfig {
    @NotNull String uri;
    Integer pushQueueSize = 1000;
    boolean enabled = true;
    Integer batchSize = 10000;

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

    public void setUri(String uri) {
        this.uri = uri;
    }

    public void setPushQueueSize(Integer pushQueueSize) {
        this.pushQueueSize = pushQueueSize;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setBatchSize(Integer batchSize) {
        this.batchSize = batchSize;
    }
}