package org.springframework.cloud.netflix.spectator

import static org.springframework.test.web.client.match.MockRestRequestMatchers.*
import static org.springframework.test.web.client.response.MockRestResponseCreators.*

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.PropertyPlaceholderAutoConfiguration
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.autoconfigure.test.ImportAutoConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.web.client.RestTemplate

import spock.lang.Specification
import spock.lang.Unroll

import com.netflix.spectator.api.DefaultRegistry
import com.netflix.spectator.api.Registry

@ContextConfiguration(classes = SpectatorRestTemplateTestConfig)
@TestPropertySource(properties = [
    'netflix.spectator.restClient.metricName=metricName',
    'spring.aop.proxy-target-class=true'
])
class SpectatorClientHttpRequestInterceptorSpec extends Specification {
    @Autowired Registry registry
	@Autowired RestTemplate restTemplate

    @Unroll
    def 'metrics gathered when successful'() {
        given:
        def mockServer = MockRestServiceServer.createServer(restTemplate)

        when:
		mockServer.expect(requestTo('/test/123')).andExpect(method(HttpMethod.GET))
		    .andRespond(withSuccess('{"status" : "OK"}', MediaType.APPLICATION_JSON))
		
		restTemplate.getForObject('/test/{id}', String, 123)
		
        then:
		registry.timer('metricName', 'method', 'GET', 'uri', '_test_-id-', 'status', '200', 'clientName', 'none')
                .count() == 1
		mockServer.verify()
    }
}

@Configuration
@ImportAutoConfiguration([SpectatorAutoConfiguration, PropertyPlaceholderAutoConfiguration, AopAutoConfiguration])
class SpectatorRestTemplateTestConfig {
	@Bean
	RestTemplate restTemplate() {
		new RestTemplate()
	}
	
    @Bean
    Registry spectatorRegistry() {
        new DefaultRegistry()
    }
}
