package org.springframework.cloud.netflix.metrics.atlas;

import java.util.Map;

public interface AtlasTagProvider {
    Map<String, String> defaultTags();
}
