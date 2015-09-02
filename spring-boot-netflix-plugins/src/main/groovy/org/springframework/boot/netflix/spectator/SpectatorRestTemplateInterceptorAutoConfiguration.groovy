package org.springframework.boot.netflix.spectator

import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration.CglibAutoProxyConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.context.annotation.Bean
import org.springframework.web.client.RestTemplate

import com.netflix.spectator.api.Registry

@ConditionalOnBean([RestTemplate, Registry])
@AutoConfigureAfter(AopAutoConfiguration)
class SpectatorRestTemplateInterceptorAutoConfiguration {
	
	@Bean
	@ConditionalOnBean(CglibAutoProxyConfiguration)
	public RestTemplateUrlTemplateCapturingAspect restTemplateUrlTemplateCapturingAspect()
	{
		return new RestTemplateUrlTemplateCapturingAspect();
	}
	
	@Bean
	public SpectatorLoggingClientHttpRequestInterceptor spectatorLoggingClientHttpRequestInterceptor()
	{
		return new SpectatorLoggingClientHttpRequestInterceptor();
	}
	
	@Bean
	public SpectatorRestTemplateInterceptorPostProcessor spectatorRestTemplateInterceptorPostProcessor(SpectatorLoggingClientHttpRequestInterceptor interceptor)
	{
		return new SpectatorRestTemplateInterceptorPostProcessor(interceptor);
	}
	
}
