package org.springframework.cloud.netflix.metrics.spectator;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.support.management.MetricsFactory;

import com.netflix.spectator.api.Registry;

@Configuration
@ConditionalOnBean({MetricsFactory.class, Registry.class})
public class SpectatorIntegrationMetricsAutoConfiguration {
	
	@Bean
	public SpectatorIntegrationMetricsPostProcessor spectatorIntegrationMetricsExporter()
	{
		return new SpectatorIntegrationMetricsPostProcessor();
	}

}
