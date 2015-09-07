package org.springframework.cloud.netflix.metrics.atlas;

import com.netflix.servo.publish.BasicMetricFilter;
import com.netflix.servo.publish.MetricPoller;
import com.netflix.servo.publish.atlas.AtlasMetricObserver;
import org.springframework.boot.actuate.metrics.export.Exporter;

public class AtlasExporter implements Exporter {
    private AtlasMetricObserver observer;
    private MetricPoller poller;

    public AtlasExporter(AtlasMetricObserver observer, MetricPoller poller) {
        this.observer = observer;
        this.poller = poller;
    }

    @Override
    public void export() {
        observer.update(poller.poll(BasicMetricFilter.MATCH_ALL));
    }
}
