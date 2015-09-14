package org.springframework.xd.netflix.config;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.http.outbound.HttpRequestExecutingMessageHandler;
import org.springframework.messaging.MessageChannel;
import org.springframework.web.client.RestTemplate;

@Configuration
@EnableAutoConfiguration
@EnableEurekaClient
@PropertySource("classpath:config/ribbon-client.properties")
public class RibbonModuleConfig { 
	
	@PostConstruct
	public void init()
	{
		System.out.println("YAAAAYYYYYYYYYY!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
	}
	
	@Bean
	public MessageChannel input() {
		return new DirectChannel();
	}

	@Bean
	MessageChannel output() {
		return new DirectChannel();
	}

	@Bean
	public IntegrationFlow myFlow(HttpRequestExecutingMessageHandler ribbonHandler) {
		return IntegrationFlows.from("input")
				.handle(ribbonHandler)
				.channel("output")
				.get();
	}
	
	@Bean 
	public HttpRequestExecutingMessageHandler ribbonHandler(@Value("${url}") String url, RestTemplate restTemplate) {
		HttpRequestExecutingMessageHandler httpRequestExecutingMessageHandler = new HttpRequestExecutingMessageHandler(url, restTemplate);
		httpRequestExecutingMessageHandler.setOutputChannel(output());
		return httpRequestExecutingMessageHandler;
	}
	
	
	
}
