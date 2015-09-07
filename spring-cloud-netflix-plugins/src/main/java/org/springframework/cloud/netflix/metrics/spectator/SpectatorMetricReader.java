package org.springframework.cloud.netflix.metrics.spectator;

import com.netflix.spectator.api.Id;
import com.netflix.spectator.api.Registry;
import org.springframework.boot.actuate.metrics.Metric;
import org.springframework.boot.actuate.metrics.reader.MetricReader;
import org.springframework.lang.UsesJava8;

import java.util.stream.Collectors;

import static java.util.stream.StreamSupport.stream;

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
                .flatMap(metric -> stream(metric.measure().spliterator(), false)
                        .map(measure -> new Metric<>(asHierarchicalName(measure.id()), measure.value())))
                .sorted((m1, m2) -> m1.getName().compareTo(m2.getName()))
                .collect(Collectors.toList());
    }

    protected static String asHierarchicalName(Id id) {
        return id.name() + "(" + String.join(",", stream(id.tags().spliterator(), false)
                    .map(t -> t.key() + "=" + t.value())
                    .collect(Collectors.toList())) + ")";
    }

    @Override
    public long count() {
        return stream(registry.spliterator(), false).count();
    }
}
