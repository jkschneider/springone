package org.springframework.cloud.netflix.eureka;

import java.util.Map;

import org.springframework.boot.actuate.health.CompositeHealthIndicator;
import org.springframework.boot.actuate.health.HealthAggregator;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.netflix.discovery.EurekaClient;

@Configuration
@ConditionalOnBean({EurekaClient.class, HealthAggregator.class})
public class EurekaHealthNotifierAutoConfiguration {

	@Bean
	public EurekaHealthNotifier discoveryHealthNotifier(HealthAggregator healthAggregator,
														   Map<String, HealthIndicator> healthIndicators,
														   EurekaClient eurekaClient) {
		HealthIndicator healthIndicator = new CompositeHealthIndicator(healthAggregator, healthIndicators);
		return new EurekaHealthNotifier(healthIndicator, eurekaClient);
	}
}
