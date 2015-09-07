package org.springframework.cloud.netflix.metrics.spectator;

import com.netflix.spectator.api.Id;
import com.netflix.spectator.api.Measurement;
import com.netflix.spectator.api.Meter;
import com.netflix.spectator.api.Registry;
import org.springframework.boot.actuate.metrics.Metric;
import org.springframework.boot.actuate.metrics.reader.MetricReader;
import org.springframework.lang.UsesJava8;

import java.util.stream.Collectors;
import static java.util.stream.StreamSupport.*;

@UsesJava8
public class SpectatorMetricReader implements MetricReader {
    private Registry registry;

    public SpectatorMetricReader(Registry registry) {
        this.registry = registry;
    }

    @Override
    public Metric<?> findOne(String name) {
        throw new UnsupportedOperationException("cannot construct a tag-based Spectator id from a hierarchical name");
    }

    @Override
    public Iterable<Metric<?>> findAll() {
        return stream(registry.spliterator(), false)
                .map(SpectatorMetricReader::asMetric)
                .collect(Collectors.toList());
    }

    protected static Metric<?> asMetric(Meter meter) {
        Measurement lastMeasurement = stream(meter.measure().spliterator(), false)
                .reduce(null, (m1, m2) -> m2);
        return new Metric<>(asHierarchicalName(meter.id()), lastMeasurement.value());
    }

    protected static String asHierarchicalName(Id id) {
        return id.name() + stream(id.tags().spliterator(), false)
                .map(t -> t.key() + "=" + t.value())
                .reduce("", (acc, tag) -> acc + "." + tag);
    }

    @Override
    public long count() {
        return stream(registry.spliterator(), false).count();
    }
}
