package org.springframework.cloud.netflix.metrics.spectator

import spock.lang.Specification

class SpectatorMetricServicesSpec extends Specification {
    def 'de-hierarchicalize metric names'() {
        expect:
        SpectatorMetricServices.stripMetricName(metric) == stripped

        where:
        metric              | stripped
        'timer.foo'         | 'foo'
        'histogram.foo'     | 'foo'
        'meter.foo'         | 'foo'
        'bar.timer.foo'     | 'bar.timer.foo'
        'foo'               | 'foo'
    }
}
