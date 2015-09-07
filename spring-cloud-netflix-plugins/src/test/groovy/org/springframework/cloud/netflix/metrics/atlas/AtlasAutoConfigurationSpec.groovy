package org.springframework.cloud.netflix.metrics.atlas

import com.netflix.spectator.api.Registry
import com.netflix.spectator.servo.ServoRegistry
import io.netty.handler.codec.http.DefaultFullHttpResponse
import io.netty.handler.codec.http.HttpObject
import io.netty.handler.codec.http.HttpRequest
import io.netty.handler.codec.http.HttpResponse
import io.netty.handler.codec.http.HttpResponseStatus
import io.netty.handler.codec.http.HttpVersion
import org.littleshoot.proxy.HttpFilters
import org.littleshoot.proxy.HttpFiltersAdapter
import org.littleshoot.proxy.HttpFiltersSourceAdapter
import org.littleshoot.proxy.impl.DefaultHttpProxyServer
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Configurable
import org.springframework.boot.actuate.metrics.export.Exporter
import org.springframework.boot.autoconfigure.PropertyPlaceholderAutoConfiguration
import org.springframework.boot.autoconfigure.context.ConfigurationPropertiesAutoConfiguration
import org.springframework.boot.autoconfigure.test.ImportAutoConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.web.WebAppConfiguration
import org.springframework.web.servlet.config.annotation.EnableWebMvc
import spock.lang.Specification

import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

@ContextConfiguration(classes = [AtlasTestConfig])
@WebAppConfiguration
@TestPropertySource(properties = ['netflix.atlas.uri=http://localhost:7102/api/v1/publish'])
class AtlasAutoConfigurationSpec extends Specification {
    @Autowired
    Registry registry

    @Autowired
    Exporter exporter

    def 'test metrics are sent to Atlas periodically'() {
        setup:
        def t = registry.timer('t')

        when:
        1000.times { t.record(100, TimeUnit.MILLISECONDS) }
        Thread.sleep(1500)

        then:
        Mockito.verify(exporter, Mockito.atLeastOnce()).export()
    }
}

@Configuration
@EnableWebMvc
@EnableScheduling
@ImportAutoConfiguration([
    AtlasAutoConfiguration,
    PropertyPlaceholderAutoConfiguration,
    ConfigurationPropertiesAutoConfiguration
])
class AtlasTestConfig {
    @Bean
    Exporter exporter() {
        Mockito.mock(Exporter)
    }

    @Autowired
    Exporter exporter

    @Scheduled(fixedRate = 1000L)
    void pushMetricsToAtlas() {
        exporter.export()
    }
}