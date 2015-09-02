package org.springframework.boot.netflix.atlas

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
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.PropertyPlaceholderAutoConfiguration
import org.springframework.boot.autoconfigure.context.ConfigurationPropertiesAutoConfiguration
import org.springframework.boot.autoconfigure.test.ImportAutoConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.web.WebAppConfiguration
import org.springframework.web.servlet.config.annotation.EnableWebMvc
import spock.lang.Specification

import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

@ContextConfiguration(classes = AtlasTestConfig)
@WebAppConfiguration
@TestPropertySource(properties = [
    'spring.application.name=appName',
    'netflix.atlas.uri=http://localhost:7102/api/v1/publish',
    'netflix.atlas.pollingInterval=1000'
])
class AtlasAutoConfigurationSpec extends Specification {
    @Autowired
    Registry registry

    def 'test metrics are sent to Atlas periodically'() {
        setup:
        def publishCount = new AtomicInteger(0)

        DefaultHttpProxyServer.bootstrap()
            .withPort(7102)
            .withFiltersSource(new HttpFiltersSourceAdapter() {
                @Override
                HttpFilters filterRequest(HttpRequest originalRequest) {
                    new HttpFiltersAdapter(originalRequest) {
                        @Override
                        HttpResponse clientToProxyRequest(HttpObject httpObject) {
                            assert ((HttpRequest) httpObject).uri == '/api/v1/publish'
                            publishCount.incrementAndGet()
                            new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK)
                        }
                    }
                }
            })
            .start()

        def t = registry.timer('t', 'appName', 'test')

        when:
        1000.times { t.record(100, TimeUnit.MILLISECONDS) }
        Thread.sleep(1000)

        then:
        publishCount.get() == 1
    }
}

@Configuration
@EnableWebMvc
@ImportAutoConfiguration([
    AtlasAutoConfiguration,
    PropertyPlaceholderAutoConfiguration,
    ConfigurationPropertiesAutoConfiguration
])
class AtlasTestConfig {
    @Bean
    ServoRegistry registry() {
        new ServoRegistry() // this is a Monitor implementation
    }
}