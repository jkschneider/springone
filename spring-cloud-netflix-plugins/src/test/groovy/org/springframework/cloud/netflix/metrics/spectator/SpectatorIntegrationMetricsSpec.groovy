package org.springframework.cloud.netflix.metrics.spectator

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.PropertyPlaceholderAutoConfiguration;
import org.springframework.boot.autoconfigure.test.ImportAutoConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.integration.annotation.Gateway
import org.springframework.integration.annotation.IntegrationComponentScan
import org.springframework.integration.annotation.MessageEndpoint
import org.springframework.integration.annotation.MessagingGateway
import org.springframework.integration.annotation.ServiceActivator
import org.springframework.integration.config.EnableIntegration
import org.springframework.integration.config.EnableIntegrationManagement
import org.springframework.integration.dsl.channel.MessageChannels
import org.springframework.integration.support.management.DefaultMetricsFactory
import org.springframework.integration.support.management.MessageChannelMetrics
import org.springframework.integration.support.management.MessageHandlerMetrics
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.test.context.ContextConfiguration

import spock.lang.Specification

import com.netflix.spectator.api.Registry

@ContextConfiguration(classes=[SpectatorIntegrationMetricsTestConfig])
class SpectatorIntegrationMetricsSpec extends Specification {
	
	@Autowired
	TestGateway gateway;
	
	@Autowired
	Registry registry;
	
	@Autowired
	List<MessageChannelMetrics> channelMetrics;
	
	@Autowired
	List<MessageHandlerMetrics> handlerMetrics;
	
	def 'test'(){
		
		when:
		gateway.sendMessage("test")
		gateway.sendMessage("error")
		Thread.sleep(500)
		
		then:
		thrown(RuntimeException)
		channelMetrics != null
		handlerMetrics != null
		registry != null
		2 == registry.get(registry.createId("springintegration", "metric", "HandleCount", "name", "spectatorIntegrationMetricsTestConfig.testActivator.serviceActivator.handler", "type", "service-activator")).measure().iterator().next().value
		1 == registry.get(registry.createId("springintegration", "metric", "ErrorCount", "name", "spectatorIntegrationMetricsTestConfig.testActivator.serviceActivator.handler", "type", "service-activator")).measure().iterator().next().value
		1 == registry.get(registry.createId("springintegration", "metric", "SendCount", "name", "output", "type", "channel")).measure().iterator().next().value
		0 == registry.get(registry.createId("springintegration", "metric", "SendErrorCount", "name", "output", "type", "channel")).measure().iterator().next().value	
	}
}

@Configuration
@EnableIntegration
@EnableIntegrationManagement(countsEnabled="*", statsEnabled="*", metricsFactory='metricsFactory')
@IntegrationComponentScan(basePackageClasses=SpectatorIntegrationMetricsTestConfig)
@MessageEndpoint
@ImportAutoConfiguration([PropertyPlaceholderAutoConfiguration, SpectatorAutoConfiguration, SpectatorIntegrationMetricsAutoConfiguration])
class SpectatorIntegrationMetricsTestConfig {
	
	@Bean
	public DefaultMetricsFactory metricsFactory()
	{
		return new DefaultMetricsFactory();
	}
	
	@Bean
	public MessageChannel input()
	{
		return MessageChannels.direct().get();
	}
	
	@Bean
	public MessageChannel output()
	{
		return MessageChannels.queue(10).get();
	}
	
	@ServiceActivator(inputChannel="input", outputChannel="output")
	public String testActivator(@Payload String payload)
	{
		if("error".equals(payload))
		{
			throw new RuntimeException("Boom");
		}
		else
		{
			return payload.toUpperCase();
		}
	}
}

@MessagingGateway
interface TestGateway {
	
	@Gateway(requestChannel="input")
	public void sendMessage(@Payload String payload);
}