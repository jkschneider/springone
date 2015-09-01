package org.springframework.boot.netflix.spectator;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.web.client.RestTemplate;

public class SpectatorRestTemplateInterceptorPostProcessor implements BeanPostProcessor {
	
	private SpectatorLoggingClientHttpRequestInterceptor interceptor;

	public SpectatorRestTemplateInterceptorPostProcessor(SpectatorLoggingClientHttpRequestInterceptor interceptor) {
		this.interceptor = interceptor;
	}

	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		return bean;
	}

	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		if(bean instanceof RestTemplate)
		{
			((RestTemplate)bean).getInterceptors().add(this.interceptor);
			return bean;
		}
		else
		{
			return bean;
		}

	}

}
