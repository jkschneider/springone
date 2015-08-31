package org.springframework.boot.netflix.spectator

import com.netflix.spectator.api.DefaultRegistry
import com.netflix.spectator.api.Registry
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.PropertyPlaceholderAutoConfiguration
import org.springframework.boot.autoconfigure.test.ImportAutoConfiguration
import org.springframework.boot.test.SpringApplicationContextLoader
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.web.WebAppConfiguration
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.context.WebApplicationContext
import org.springframework.web.servlet.config.annotation.EnableWebMvc
import spock.lang.Specification
import spock.lang.Unroll

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup

@ContextConfiguration(classes = SpectatorTestConfig, loader = SpringApplicationContextLoader.class)
@WebAppConfiguration
@TestPropertySource(properties = 'spring.application.name=test')
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class SpectatorMetricsHandlerInterceptorSpec extends Specification {
    @Autowired WebApplicationContext webAppContext
    @Autowired Registry registry

    def 'auto-configuration wires the metrics interceptor'() {
        expect:
        !webAppContext.getBeansOfType(SpectatorMetricsHandlerInterceptor).isEmpty()
    }

    @Unroll
    def 'metrics gathered when #name'() {
        given:
        def mvc = webAppContextSetup(webAppContext).build()

        when:
        try {
            mvc.perform(get(uri)).andExpect(status >= 400 ? status().is4xxClientError() : status().isOk())
        } catch(e) {}

        then:
        registry.timer('test', 'method', 'GET', 'uri', uriTag, 'caller', 'unknown',
                'exceptionType', exceptionType, 'status', status as String).count() == 1

        where:
        name                     | endpoint  | id     | exceptionType       | status
        'successful'             | 'request' | '10'   | 'none'              | 200
        'handled error occurs'   | 'request' | 'oops' | 'none'              | 400
        'unhandled error occurs' | 'error'   | '10'   | 'RuntimeException'  | 200

        uri = "/test/some/$endpoint/$id"
        uriTag = "test_some_${endpoint}_-id-"
    }
}

@Configuration
@EnableWebMvc
@ImportAutoConfiguration([SpectatorMetricsAutoConfiguration, PropertyPlaceholderAutoConfiguration])
class SpectatorTestConfig {
    @Bean
    SpectatorTestController testController() {
        new SpectatorTestController()
    }

    @Bean
    Registry spectatorRegistry() {
        new DefaultRegistry()
    }
}

@RestController
@RequestMapping('/test/some')
class SpectatorTestController {
    @RequestMapping('/request/{id}')
    public String testSomeRequest(@PathVariable Long id) { id as String }

    @RequestMapping('/error/{id}')
    public String testSomeHandledError(@PathVariable Long id) { throw new RuntimeException("Boom on $id!") }
}