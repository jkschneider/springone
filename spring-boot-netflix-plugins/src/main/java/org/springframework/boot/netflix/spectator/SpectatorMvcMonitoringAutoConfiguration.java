package org.springframework.boot.netflix.spectator;

import javax.servlet.Servlet;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import com.netflix.spectator.api.Registry;

@Configuration
@ConditionalOnClass({ Servlet.class, DispatcherServlet.class })
@ConditionalOnBean(Registry.class)
@ConditionalOnWebApplication
public class SpectatorMvcMonitoringAutoConfiguration extends WebMvcConfigurerAdapter {
	
	@Bean
	public SpectatorMonitoringWebResourceInterceptor spectatorMonitoringWebResourceInterceptor()
	{
		return new SpectatorMonitoringWebResourceInterceptor();
	}
	
	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(spectatorMonitoringWebResourceInterceptor());
	}

}
