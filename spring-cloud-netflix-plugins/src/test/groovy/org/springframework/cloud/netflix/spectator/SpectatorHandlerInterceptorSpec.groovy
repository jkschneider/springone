package org.springframework.cloud.netflix.spectator

import com.netflix.spectator.api.DefaultRegistry
import com.netflix.spectator.api.Registry
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.PropertyPlaceholderAutoConfiguration
import org.springframework.boot.autoconfigure.test.ImportAutoConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.web.WebAppConfiguration
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.context.WebApplicationContext
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.servlet.config.annotation.EnableWebMvc
import spock.lang.Specification
import spock.lang.Unroll

import javax.servlet.http.HttpServletRequest

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup

@ContextConfiguration(classes = SpectatorTestConfig)
@WebAppConfiguration
@TestPropertySource(properties = 'netflix.spectator.rest.metricName=metricName')
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class SpectatorMetricsHandlerInterceptorSpec extends Specification {
    @Autowired WebApplicationContext webAppContext
    @Autowired Registry registry

    def 'auto-configuration wires the metrics interceptor'() {
        expect:
        !webAppContext.getBeansOfType(SpectatorHandlerInterceptor).isEmpty()
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
        registry.timer('metricName', 'method', 'GET', 'uri', uriTag, 'caller', 'unknown',
                'exceptionType', exceptionType, 'status', status as String).count() == 1

        where:
        name                     | endpoint             | id     | exceptionType       | status
        'successful'             | 'request'            | '10'   | 'none'              | 200
        'client request bad'     | 'request'            | 'oops' | 'none'              | 400
        'handled error occurs'   | 'error'              | '10'   | 'none'              | 422
        'unhandled error occurs' | 'unhandledError'     | '10'   | 'RuntimeException'  | 200

        uri = "/test/some/$endpoint/$id"
        uriTag = "test_some_${endpoint}_-id-"
    }
}

@Configuration
@EnableWebMvc
@ImportAutoConfiguration([SpectatorAutoConfiguration, PropertyPlaceholderAutoConfiguration])
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
@ControllerAdvice
class SpectatorTestController {
    @RequestMapping('/request/{id}')
    public String testSomeRequest(@PathVariable Long id) { id as String }

    @RequestMapping('/error/{id}')
    public String testSomeHandledError(@PathVariable Long id) { throw new IllegalStateException("Boom on $id!") }

    @RequestMapping('/unhandledError/{id}')
    public String testSomeUnhandledError(@PathVariable Long id) { throw new RuntimeException("Boom on $id!") }

    @ExceptionHandler(value = IllegalStateException)
    @ResponseStatus(code = HttpStatus.UNPROCESSABLE_ENTITY)
    ModelAndView defaultErrorHandler(HttpServletRequest request, Exception e) {
        new ModelAndView("error")
    }
}