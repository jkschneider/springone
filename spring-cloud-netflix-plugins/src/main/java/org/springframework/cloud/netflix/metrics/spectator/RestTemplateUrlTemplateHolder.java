package org.springframework.cloud.netflix.metrics.spectator;

import org.springframework.core.NamedThreadLocal;

public class RestTemplateUrlTemplateHolder {
	private static final ThreadLocal<String> restTemplateUrlTemplateHolder =
		new NamedThreadLocal<String>("Rest Template URL Template");
	
	public static void setRestTemplateUrlTemplate(String urlTemplate) {
		restTemplateUrlTemplateHolder.set(urlTemplate);
	}
	
	public static String getRestTemplateUrlTemplate() {
		return restTemplateUrlTemplateHolder.get();
	}
	
	public static void clear() {
		restTemplateUrlTemplateHolder.remove();
	}
}
