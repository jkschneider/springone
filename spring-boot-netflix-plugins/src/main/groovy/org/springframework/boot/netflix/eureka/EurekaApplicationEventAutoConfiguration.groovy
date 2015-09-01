package org.springframework.boot.netflix.eureka;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.netflix.discovery.EurekaClient;

@Configuration
@ConditionalOnBean(EurekaClient)
@EnableScheduling
public class EurekaApplicationEventAutoConfiguration {
	
	@Bean
	public EurekaStatusApplicationEventPublisher eurekaStatusApplicationEventPublisher(){
		return new EurekaStatusApplicationEventPublisher();
	}
	
}



