package org.springframework.boot.netflix.test.util;

import org.kubek2k.springockito.annotations.internal.Loader;
import org.springframework.test.context.ContextConfigurationAttributes;
import org.springframework.test.context.web.AnnotationConfigWebContextLoader;
import org.springframework.test.context.web.WebMergedContextConfiguration;
import org.springframework.web.context.support.GenericWebApplicationContext;

public class SpringockitoWebContextLoader extends AnnotationConfigWebContextLoader {

	private Loader loader = new Loader();

	@Override
	protected void customizeContext(GenericWebApplicationContext context, WebMergedContextConfiguration webMergedConfig) {
		super.customizeContext(context, webMergedConfig);
		loader.registerMocksAndSpies(context);
	}

	@Override
	protected Class<?>[] detectDefaultConfigurationClasses(Class<?> declaringClass) {
		Class<?>[] clazz = super.detectDefaultConfigurationClasses(declaringClass);
		loader.defineMocksAndSpies(declaringClass);
		return clazz;
	}

	@Override
	public void processContextConfiguration(ContextConfigurationAttributes configAttributes) {
		super.processContextConfiguration(configAttributes);
		loader.defineMocksAndSpies(configAttributes.getDeclaringClass());
	}
}