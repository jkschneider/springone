package org.springframework.boot.netflix.spectator

import org.springframework.core.NamedThreadLocal

class RestTemplateUrlTemplateHolder {
	
	private static final ThreadLocal<String> restTemplateUrlTemplateHolder =
	new NamedThreadLocal<String>("Rest Template URL Template");
	
	public static void setRestTemplateUrlTemplate(String urlTemplate) {
		restTemplateUrlTemplateHolder.set(urlTemplate);
	}
	
	public static String getRestTempalteUrlTemplate() {
		return restTemplateUrlTemplateHolder.get();
	}
	
	public static void clear() {
		restTemplateUrlTemplateHolder.remove();
	}
}
