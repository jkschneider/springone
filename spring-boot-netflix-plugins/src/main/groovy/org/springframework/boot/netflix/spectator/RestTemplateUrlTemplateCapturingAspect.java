package org.springframework.boot.netflix.spectator;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

@Aspect
public class RestTemplateUrlTemplateCapturingAspect {

	@Around(value="execution(* org.springframework.web.client.RestTemplate.*(String, ..))")
	public void captureUrlTemplate(ProceedingJoinPoint joinPoint) throws Throwable
	{
		try
		{
			String urlTemplate =  (String) joinPoint.getArgs()[0];
			RestTemplateUrlTemplateHolder.setRestTemplateUrlTemplate(urlTemplate);
			joinPoint.proceed();
		}
		finally
		{
			RestTemplateUrlTemplateHolder.clear();
		}
	}
}
